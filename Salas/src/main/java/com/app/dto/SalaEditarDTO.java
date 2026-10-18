package com.app.dto;

import com.app.enums.TipoSala;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Edicion de una sala YA creada: a proposito solo nombre y tipo. Cambiar
 * filas/asientosPorFila despues de creada implicaria regenerar el mapa de
 * butacas (altaSalaConMapa), lo que rompe cualquier venta/funcion que ya
 * referencie butacaIds existentes — eso no es "editar", es otra sala.
 */
public record SalaEditarDTO(
    @NotBlank(message = "El nombre de la sala es obligatorio")
    String nombre,

    @NotNull(message = "El tipo de sala es obligatorio")
    TipoSala tipo
) {}
