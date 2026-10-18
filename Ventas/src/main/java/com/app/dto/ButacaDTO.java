package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Los datos de la butaca que Ventas necesita de MS Sala para validar la
 * compra y, despues, para ocuparla/liberarla.
 *
 * OJO PARA EL EQUIPO: MS Sala todavia NO expone esto (hoy "Sala" es solo
 * id/nombre/capacidad/tipo, sin butacas). Este DTO y el contrato de
 * SalaClient son los que se acordaron en los diagramas de clases y de
 * secuencia — ver CONTRATOS-REQUERIDOS.md para el detalle de que falta
 * agregar del lado de Sala.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ButacaDTO(
    Long id,
    Long salaId,
    String fila,
    Integer numero,
    String estado // "DISPONIBLE" | "OCUPADA"
) {
    public boolean disponible() {
        return "DISPONIBLE".equalsIgnoreCase(estado);
    }
}
