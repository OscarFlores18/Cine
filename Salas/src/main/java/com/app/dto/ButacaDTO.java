package com.app.dto;

import com.app.enums.EstadoButaca;

public record ButacaDTO(
    Long id,
    Long salaId,
    String fila,
    Integer numero,
    EstadoButaca estado
) {}