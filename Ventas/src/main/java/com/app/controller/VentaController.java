package com.app.controller;

import com.app.dto.VentaAltaDTO;
import com.app.model.Venta;
import com.app.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // X-User-Username lo agrega el Gateway (UserHeadersFilter) con el subject del JWT.
    @PostMapping
    public ResponseEntity<Venta> altaVenta(@RequestHeader("X-User-Username") String username,
                                           @Valid @RequestBody VentaAltaDTO dto) {
        Venta nueva = new Venta();
        nueva.setUsername(username);
        nueva.setProductoId(dto.productoId());
        nueva.setCantidad(dto.cantidad());
        nueva.setTotal(dto.total());
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.altaVenta(nueva));
    }

    // Listado completo: el Gateway lo deja solo para ADMIN y EMPLEADO
    @GetMapping
    public ResponseEntity<List<Venta>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    // Las compras del usuario logueado
    @GetMapping("/mias")
    public ResponseEntity<List<Venta>> misVentas(@RequestHeader("X-User-Username") String username) {
        return ResponseEntity.ok(ventaService.listarVentasDeUsuario(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }
}
