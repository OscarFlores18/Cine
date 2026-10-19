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

    // username/roles son del usuario logueado (headers que arma el Gateway):
    // hace falta para chequear que la venta sea suya, salvo que sea
    // ADMIN/EMPLEADO (ver VentaServiceImpl.verificarPropietarioOAdmin).
    VentaDTO cancelarVenta(Long id, String username, String rolesCsv);

    // Vuelve a generar el link de pago de Mercado Pago para una venta
    // propia que quedo PENDIENTE sin link real (ej: Mercado Pago estaba
    // caido o rechazo la preferencia la primera vez) — antes esa venta
    // quedaba para siempre en "Mis compras" sin forma de pagarla ni
    // cancelarla desde la UI.
    VentaDTO reintentarPago(Long id, String username, String rolesCsv);

    // Lo llama el webhook de Mercado Pago (POST /ventas/webhook/mercadopago).
    // No devuelve nada: a Mercado Pago solo le importa el 200 OK.
    void procesarWebhookMercadoPago(MercadoPagoWebhookDTO webhook);
}
