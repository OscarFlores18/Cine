package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Los datos de la sala (nombre, tipo) que Ventas necesita solo para armar
 * la factura que viaja en el evento de venta confirmada — no para validar
 * la compra (eso ya se resuelve con FuncionDTO/ButacaDTO).
 *
 * A diferencia de Funcion/Butaca, "GET /salas/{id}" YA existe hoy en el
 * Sala real (es el CRUD base que ya tienen funcionando), asi que este
 * cliente no depende de nada nuevo del lado de Sala.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SalaDTO(
    Long id,
    String nombre,
    Integer capacidad,
    String tipo
) {}
