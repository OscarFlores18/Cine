package com.app.senders;

/**
 * Copia (lado EMISOR) del contrato real que expone/consume MS Sala via
 * RabbitMQ: exchange "exchange.ventas" (direct), routing key
 * "routing.butacas", cola "queue.butacas.actualizar" (la cola y el binding
 * los declara y es duena Sala — Ventas solo necesita el nombre del exchange
 * y la routing key para publicar).
 *
 * nuevoEstado viaja como texto ("DISPONIBLE" | "OCUPADO") para no acoplar a
 * Ventas con el enum EstadoButaca de Sala; Jackson lo deserializa del lado
 * de Sala contra su propio enum porque el nombre coincide exactamente.
 */
public record EventoEntradaCompradaDTO(Long butacaId, String nuevoEstado) {}
