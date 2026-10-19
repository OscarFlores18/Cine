package com.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Registro de cada mail que se intento enviar (historial / auditoria). */
@Entity
@Table(name = "emails_enviados",
        // (venta_id, tipo) en vez de venta_id solo: una misma venta ahora
        // puede generar DOS correos distintos a lo largo de su vida (el de
        // confirmacion de compra y, si se cancela, el de cancelacion) -con
        // la constraint vieja (venta_id solo) el segundo chocaba contra el
        // primero y jamas se guardaba/mandaba. Null en cualquiera de las
        // dos columnas (envios manuales, sin venta ni tipo) sigue sin
        // contar como choque: SQL no considera NULL igual a NULL.
        uniqueConstraints = @UniqueConstraint(columnNames = {"venta_id", "tipo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailEnviado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id de la venta en MS Ventas que origino el correo. Queda null cuando el
    // mail se dispara a mano por REST (POST /gmail/enviar). Sirve tanto para
    // auditoria (de que compra salio cada correo) como para idempotencia
    // (ver EmailEnviadoRepository.existsByVentaIdAndTipo).
    @Column(name = "venta_id")
    private Long ventaId;

    // "CONFIRMACION" o "CANCELACION" (null en envios manuales por REST, que
    // no vienen de ningun evento de venta). Ver nota de la unique constraint
    // arriba: esto es lo que permite que una misma venta tenga sus dos
    // correos sin que el segundo choque contra el primero.
    private String tipo;

    private String destinatario;
    private String asunto;
    private LocalDateTime fecha;

    // "ENVIADO" o "ERROR"
    private String estado;

    @Column(length = 1000)
    private String detalleError;
}
