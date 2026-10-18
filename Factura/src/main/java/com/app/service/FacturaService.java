package com.app.service;

import com.app.dto.FacturaDTO;
import com.app.dto.VentaConfirmadaEventDTO;

import java.util.List;

public interface FacturaService {

    // Lo llama el consumer de RabbitMQ cuando llega "venta.confirmada":
    // persiste la factura. El mail de confirmacion lo manda MS Gmail por su
    // cuenta, escuchando el mismo evento con su propia cola — Factura no
    // depende de Gmail ni lo llama.
    void procesarVentaConfirmada(VentaConfirmadaEventDTO evento);

    List<FacturaDTO> listarDeUsuario(String username);

    FacturaDTO obtenerPorId(Long id);

    FacturaDTO obtenerPorVentaId(Long ventaId);
}
