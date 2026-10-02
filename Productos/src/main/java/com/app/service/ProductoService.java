package com.app.service;

import java.util.List;

import com.app.model.Producto;

public interface ProductoService {
    Producto altaProducto(Producto producto);
    List<Producto> listarProductos();
    Producto obtenerPorId(Long id);
    void eliminarProducto(Long id);
}
