package com.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // Opcional: "ADMIN", "EMPLEADO" o "CLIENTE". Si no se manda, se crea como CLIENTE.
    private String rol;
}
