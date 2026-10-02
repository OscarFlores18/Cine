package com.app.controller;

import com.app.dto.LoginRequest;
import com.app.dto.LoginResponse;
import com.app.dto.RefreshTokenRequest;
import com.app.dto.RegisterRequest;
import com.app.model.RefreshToken;
import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.repository.UsuarioRepository;
import com.app.security.JwtService;
import com.app.security.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername()).orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos");
        }

        return ResponseEntity.ok(armarRespuesta(usuario));
    }

    // El refresh token es la prueba acá — no hace falta un access token
    // vigente para pedir uno nuevo (de hecho, el caso de uso típico es
    // justo cuando el access token YA venció).
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        Usuario usuario = refreshTokenService.validarYObtenerUsuario(request.getRefreshToken())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token inválido, vencido o revocado. Iniciá sesión de nuevo.");
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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario ya existe");
        }

        Rol rol;
        try {
            rol = request.getRol() != null ? Rol.valueOf(request.getRol().toUpperCase()) : Rol.CLIENTE;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Rol inválido. Usá ADMIN, EMPLEADO o CLIENTE.");
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(request.getUsername());
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setRol(rol);
        usuarioRepository.save(nuevo);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado con rol " + rol);
    }

    private LoginResponse armarRespuesta(Usuario usuario) {
        String accessToken = jwtService.generarToken(usuario);
        RefreshToken refreshToken = refreshTokenService.crear(usuario);

        return new LoginResponse(
                accessToken,
                "Bearer",
                refreshToken.getToken(),
                usuario.getUsername(),
                usuario.getRol().name(),
                jwtService.getExpirationMs() / 1000
        );
    }
}
