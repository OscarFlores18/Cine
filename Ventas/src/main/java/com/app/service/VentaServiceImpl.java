package com.app.service;

import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Venta;
import com.app.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;

    @Override
    public Venta altaVenta(Venta venta) {
        venta.setFecha(LocalDateTime.now());
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public List<Venta> listarVentasDeUsuario(String username) {
        return ventaRepository.findByUsername(username);
    }

    @Override
    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con id: " + id));
    }
}
