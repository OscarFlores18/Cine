package com.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String email;

    // Opcional: "ADMIN", "EMPLEADO" o "CLIENTE". Si no se manda, se crea como CLIENTE.
    // Solo se usa en /auth/register (ADMIN). En /auth/registro-cliente se ignora:
    // siempre se crea CLIENTE.
    private String rol;
}
