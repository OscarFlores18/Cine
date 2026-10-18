package com.app.dto;

import com.app.enums.EstadoButaca;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoButacaDTO(
    @NotNull(message = "El estado es obligatorio") EstadoButaca estado
) {}