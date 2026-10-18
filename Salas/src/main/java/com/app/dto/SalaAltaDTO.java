package com.app.dto;

import com.app.enums.TipoSala;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SalaAltaDTO(
    @NotBlank(message = "El nombre de la sala es obligatorio") 
    String nombre,

    @NotNull(message = "El tipo de sala es obligatorio") 
    TipoSala tipo,

    @NotNull(message = "Debe indicar la cantidad de filas")
    @Min(value = 1, message = "Debe haber al menos 1 fila")
    @Max(value = 26, message = "Máximo 26 filas (A-Z)")
    Integer filas,

    @NotNull(message = "Debe indicar la cantidad de asientos por fila")
    @Min(value = 1, message = "Debe haber al menos 1 asiento por fila")
    Integer asientosPorFila
) {}