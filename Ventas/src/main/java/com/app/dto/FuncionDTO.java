package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Los datos de la funcion (pelicula + sala + horario) que Ventas necesita de
 * MS Sala: sobre todo el precio base de la entrada.
 *
 * OJO PARA EL EQUIPO: igual que ButacaDTO, MS Sala todavia no expone
 * Funcion — ver CONTRATOS-REQUERIDOS.md.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FuncionDTO(
    Long id,
    Long salaId,
    String nombreSala,
    Long peliculaId,
    String fechaHora,
    BigDecimal precioBase
) {}
