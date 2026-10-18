package com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.EmailEnviado;

public interface EmailEnviadoRepository extends JpaRepository<EmailEnviado, Long> {

    // Idempotencia: RabbitMQ es at-least-once, el mismo evento puede
    // reentregarse (reinicio del consumer antes del ack, retry del
    // publicador, etc.) — asi VentaEventListener no manda el mismo correo
    // dos veces para la misma venta.
    boolean existsByVentaId(Long ventaId);
}
