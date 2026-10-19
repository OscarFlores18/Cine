package com.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Body de PUT /auth/usuarios/{username} (ADMIN): edicion de los datos de
// un usuario ya existente. No incluye username (va en la URL, no se puede
// renombrar) ni password (cambiar contraseña es un flujo aparte, fuera de
// este alcance) -el rol es opcional porque no todas las ediciones cambian
// el rol, pero si se manda tiene que ser uno valido (se valida en el
// controller, igual que en /auth/register).
@Data
public class ActualizarUsuarioRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String email;

    // "ADMIN", "EMPLEADO" o "CLIENTE". Si no se manda, se deja el rol
    // que ya tenia.
    private String rol;
}
