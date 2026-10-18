package com.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FuncionResponseDTO(
    Long id,
    Long salaId,
    String nombreSala,
    Long peliculaId,
    LocalDateTime fechaHora,
    BigDecimal precioBase
) {}