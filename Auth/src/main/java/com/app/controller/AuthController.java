package com.app.controller;

import com.app.dto.ActualizarEstadoUsuarioRequest;
import com.app.dto.ActualizarUsuarioRequest;
import com.app.dto.AdminUsuarioDTO;
import com.app.dto.LoginRequest;
import com.app.dto.LoginResponse;
import com.app.dto.RefreshTokenRequest;
import com.app.dto.RegisterRequest;
import com.app.dto.UsuarioDTO;
import com.app.exception.ApiError;
import com.app.model.RefreshToken;
import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.repository.UsuarioRepository;
import com.app.security.JwtService;
import com.app.security.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest req) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername()).orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req);
        }

        // No se distingue el mensaje de "no existe"/"contraseña mal" (arriba)
        // del de "deshabilitado": si fueran distintos, alguien podría usar
        // el login para averiguar si un username existe y fue dado de baja.
        if (!estaHabilitado(usuario)) {
            return error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req);
        }

        return ResponseEntity.ok(armarRespuesta(usuario));
    }

    // El refresh token es la prueba acá — no hace falta un access token
    // vigente para pedir uno nuevo (de hecho, el caso de uso típico es
    // justo cuando el access token YA venció).
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest req) {
        Usuario usuario = refreshTokenService.validarYObtenerUsuario(request.getRefreshToken())
                .orElse(null);

        if (usuario == null) {
            return error(HttpStatus.UNAUTHORIZED,
                    "Refresh token inválido, vencido o revocado. Iniciá sesión de nuevo.", req);
        }

        // Si lo deshabilitaron DESPUES de loguearse, el access token que ya
        // tiene sigue siendo valido hasta que expira solo (limitacion
        // conocida de JWT stateless, ver nota en logout mas abajo) pero no
        // puede renovarlo: se le revoca este refresh token y se lo corta aca.
        if (!estaHabilitado(usuario)) {
            refreshTokenService.revocar(request.getRefreshToken());
            return error(HttpStatus.UNAUTHORIZED,
                    "Usuario deshabilitado. Iniciá sesión de nuevo o contactá a un administrador.", req);
        }

        // Rotación: el refresh token usado queda revocado y se emite uno
        // nuevo junto con el access token. Si alguien roba un refresh
        // token viejo y lo reutiliza, deja de servir apenas el dueño
        // legítimo pidió uno nuevo primero.
        refreshTokenService.revocar(request.getRefreshToken());

        return ResponseEntity.ok(armarRespuesta(usuario));
    }

    // Esto es el logout "real" del lado del servidor: revoca el refresh
    // token para que no se puedan seguir pidiendo access tokens nuevos.
    // El último access token emitido sigue siendo válido hasta que
    // expira solo (limitación conocida de JWT stateless) — por eso su
    // duración es corta.
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revocar(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    // Protegido en SecurityConfig con hasRole("ADMIN"): solo alguien ya
    // logueado como ADMIN puede crear usuarios nuevos.
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest req) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return error(HttpStatus.CONFLICT, "El usuario ya existe", req);
        }

        Rol rol;
        try {
            rol = request.getRol() != null ? Rol.valueOf(request.getRol().toUpperCase()) : Rol.CLIENTE;
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "Rol inválido. Usá ADMIN, EMPLEADO o CLIENTE.", req);
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(request.getUsername());
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setNombre(request.getNombre());
        nuevo.setApellido(request.getApellido());
        nuevo.setEmail(request.getEmail());
        nuevo.setRol(rol);
        usuarioRepository.save(nuevo);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado con rol " + rol);
    }

    // Alta libre (sin login previo) para que un cliente nuevo se registre
    // solo desde el front público — a diferencia de /auth/register (que
    // sigue reservado a ADMIN, para crear EMPLEADO/ADMIN), esta siempre
    // crea CLIENTE: nadie puede autoasignarse un rol más alto por acá.
    @PostMapping("/registro-cliente")
    public ResponseEntity<?> registroCliente(@Valid @RequestBody RegisterRequest request, HttpServletRequest req) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return error(HttpStatus.CONFLICT, "El usuario ya existe", req);
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(request.getUsername());
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setNombre(request.getNombre());
        nuevo.setApellido(request.getApellido());
        nuevo.setEmail(request.getEmail());
        nuevo.setRol(Rol.CLIENTE);
        usuarioRepository.save(nuevo);

        return ResponseEntity.ok(armarRespuesta(nuevo));
    }

    // Lookup liviano usado por OTROS microservicios via Feign (hoy: Ventas,
    // para poner el nombre/apellido reales en la factura). No pasa por el
    // Gateway en el flujo normal de Ventas->Auth (Feign llama directo a
    // esta instancia vía Eureka); queda público a nivel de Auth a propósito
    // porque es tráfico de servicio a servicio, no de un usuario final.
    @GetMapping("/usuarios/{username}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable String username) {
        return usuarioRepository.findByUsername(username)
                .map(u -> ResponseEntity.ok(UsuarioDTO.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Listado para el panel de administracion de usuarios. Protegido con
    // hasRole("ADMIN") en SecurityConfig -a diferencia de /usuarios/{username}
    // de arriba, este SI expone rol y estado, asi que no puede quedar publico.
    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll().stream()
                .map(AdminUsuarioDTO::from)
                .toList());
    }

    // Baja/alta logica (ver nota en Usuario.habilitado): no borra nada,
    // solo le corta (o le devuelve) el acceso. Protegido con hasRole("ADMIN").
    @PatchMapping("/usuarios/{username}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String username,
                                            @Valid @RequestBody ActualizarEstadoUsuarioRequest request,
                                            HttpServletRequest req) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return error(HttpStatus.NOT_FOUND, "No existe un usuario con username '" + username + "'", req);
        }

        usuario.setHabilitado(request.habilitado());
        usuarioRepository.save(usuario);

        // Al deshabilitar, se revocan TODOS sus refresh tokens vigentes de
        // una: si no, alguien con sesion abierta en otro dispositivo podria
        // seguir renovando el access token y el alta/baja no cortaria nada
        // hasta que ese access token expirara solo por su cuenta.
        if (!request.habilitado()) {
            refreshTokenService.revocarTodosDe(usuario);
        }

        return ResponseEntity.ok(AdminUsuarioDTO.from(usuario));
    }

    // Edicion de datos (nombre/apellido/email/rol). Protegido con hasRole("ADMIN").
    @PutMapping("/usuarios/{username}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable String username,
                                                @Valid @RequestBody ActualizarUsuarioRequest request,
                                                HttpServletRequest req) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return error(HttpStatus.NOT_FOUND, "No existe un usuario con username '" + username + "'", req);
        }

        if (request.getRol() != null && !request.getRol().isBlank()) {
            try {
                usuario.setRol(Rol.valueOf(request.getRol().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return error(HttpStatus.BAD_REQUEST, "Rol inválido. Usá ADMIN, EMPLEADO o CLIENTE.", req);
            }
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(AdminUsuarioDTO.from(usuario));
    }

    // NULL (filas creadas antes de que existiera esta columna) cuenta como
    // habilitado -ver nota en Usuario.habilitado.
    private boolean estaHabilitado(Usuario usuario) {
        return !Boolean.FALSE.equals(usuario.getHabilitado());
    }

    // Antes estos errores de negocio se devolvian como String plano en el
    // body (ResponseEntity.status(..).body("texto")): el front no podia
    // leerlos con el mismo criterio que al resto de los modulos (ver
    // core/utils/error.util.ts), que esperan un objeto con "message". Se
    // unifica aca al mismo ApiError que usa el resto del sistema.
    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest req) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    private LoginResponse armarRespuesta(Usuario usuario) {
        String accessToken = jwtService.generarToken(usuario);
        RefreshToken refreshToken = refreshTokenService.crear(usuario);

        return new LoginResponse(
                accessToken,
                "Bearer",
                refreshToken.getToken(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol().name(),
                jwtService.getExpirationMs() / 1000
        );
    }
}
