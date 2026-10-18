package com.app.config;

import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            crearUsuario("admin", "admin123", "Admin", "Cine", "admin@cine.local", Rol.ADMIN);
            crearUsuario("empleado", "empleado123", "Empleado", "De Prueba", "empleado@cine.local", Rol.EMPLEADO);
            crearUsuario("cliente", "cliente123", "Cliente", "De Prueba", "cliente@cine.local", Rol.CLIENTE);

            log.info("Usuarios de prueba creados -> admin/admin123 (ADMIN), empleado/empleado123 (EMPLEADO), cliente/cliente123 (CLIENTE)");
        }
    }

    private void crearUsuario(String username, String password, String nombre, String apellido, String email, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
    }
}
