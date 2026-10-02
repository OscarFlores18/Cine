package com.app.service;

import java.util.List;

import com.app.model.Venta;

public interface VentaService {
    Venta altaVenta(Venta venta);
    List<Venta> listarVentas();
    List<Venta> listarVentasDeUsuario(String username);
    Venta obtenerPorId(Long id);
}
