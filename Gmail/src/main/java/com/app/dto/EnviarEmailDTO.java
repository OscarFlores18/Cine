package com.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EnviarEmailDTO(
    @NotBlank @Email String destinatario,
    @NotBlank String asunto,
    @NotBlank String mensaje
) {}
