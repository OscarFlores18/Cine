package com.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Registro de cada mail que se intento enviar (historial / auditoria). */
@Entity
@Table(name = "emails_enviados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailEnviado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String destinatario;
    private String asunto;
    private LocalDateTime fecha;

    // "ENVIADO" o "ERROR"
    private String estado;

    @Column(length = 1000)
    private String detalleError;
}
