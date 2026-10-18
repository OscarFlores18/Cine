package com.app.service;

import com.app.dto.MercadoPagoWebhookDTO;
import com.app.dto.VentaDTO;
import com.app.mapper.IMapper;
import com.app.model.Venta;

import java.util.List;

public interface VentaService {

    // Devuelve la venta recien creada junto con el initPoint de Mercado Pago
    // (VentaDTO.initPoint): el front manda al cliente a esa URL a pagar.
    VentaDTO altaVenta(IMapper<Venta> mapper, String username);

    List<VentaDTO> listarVentas();

    List<VentaDTO> listarVentasDeUsuario(String username);

    VentaDTO obtenerPorId(Long id);

    VentaDTO cancelarVenta(Long id);

    // Lo llama el webhook de Mercado Pago (POST /ventas/webhook/mercadopago).
    // No devuelve nada: a Mercado Pago solo le importa el 200 OK.
    void procesarWebhookMercadoPago(MercadoPagoWebhookDTO webhook);
}
