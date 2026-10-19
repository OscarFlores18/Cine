package com.app.dto;

import com.app.model.Usuario;

// A diferencia de UsuarioDTO (pensado para que OTROS microservicios
// resuelvan un username via Feign, sin rol ni estado), este DTO es para
// el panel de administracion: lo arma AuthController#listarUsuarios,
// protegido con hasRole("ADMIN") tanto en Auth como en el Gateway. Expone
// id, rol y habilitado -nunca el password- porque el admin los necesita
// para decidir a quien dar de alta/baja o cambiar de rol.
public record AdminUsuarioDTO(
        Long id,
        String username,
        String nombre,
        String apellido,
        String email,
        String rol,
        boolean habilitado
) {
    public static AdminUsuarioDTO from(Usuario usuario) {
        return new AdminUsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().name(),
                // Mismo criterio que AuthController#estaHabilitado: NULL
                // (fila vieja, de antes de esta columna) se muestra como
                // habilitado, no como deshabilitado.
                !Boolean.FALSE.equals(usuario.getHabilitado())
        );
    }
}
