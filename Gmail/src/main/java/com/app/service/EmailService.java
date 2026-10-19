package com.app.service;

import java.util.List;

import com.app.dto.EnviarEmailDTO;
import com.app.model.EmailEnviado;

public interface EmailService {

    /** Envia un mail disparado a mano (sin venta asociada). */
    EmailEnviado enviar(EnviarEmailDTO dto);

    /**
     * Envia un mail y lo asocia a una venta como correo de CONFIRMACION.
     * {@code ventaId} puede ser null cuando el origen no es una venta.
     */
    EmailEnviado enviar(EnviarEmailDTO dto, Long ventaId);

    /**
     * Igual que el de arriba, pero permite elegir el tipo ("CONFIRMACION" o
     * "CANCELACION") — necesario porque una misma venta puede generar los
     * dos correos a lo largo de su vida (ver EmailEnviado, constraint
     * (venta_id, tipo)).
     */
    EmailEnviado enviar(EnviarEmailDTO dto, Long ventaId, String tipo);

    List<EmailEnviado> historial();
}
