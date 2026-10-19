package com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.EmailEnviado;

public interface EmailEnviadoRepository extends JpaRepository<EmailEnviado, Long> {

    // Idempotencia: RabbitMQ es at-least-once, el mismo evento puede
    // reentregarse (reinicio del consumer antes del ack, retry del
    // publicador, etc.) — asi los listeners no mandan el mismo correo dos
    // veces para la misma venta. Por tipo (CONFIRMACION/CANCELACION) y no
    // solo por ventaId: una venta cancelada ya tiene un correo de
    // confirmacion previo, y eso no tiene que impedir el de cancelacion.
    boolean existsByVentaIdAndTipo(Long ventaId, String tipo);
}
