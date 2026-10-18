package com.app.dto;

import com.app.enums.EstadoButaca;

public record EventoEntradaCompradaDTO(
    Long butacaId,
    EstadoButaca nuevoEstado
) {}