package com.app.service;

import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Producto;
import com.app.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    public Producto altaProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @Override
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + id));
    }

    @Override
    public void eliminarProducto(Long id) {
        // Si no existe, tira 404 en vez de fallar en silencio
        obtenerPorId(id);
        productoRepository.deleteById(id);
    }
}
