package com.app.controller;

import com.app.dto.FacturaDTO;
import com.app.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Factura es de SOLO LECTURA desde afuera: la unica forma de crear una es
 * el evento "venta.confirmada" que consume VentaConfirmadaConsumer. No hay
 * POST /facturas (no tendria sentido inventar una factura sin una venta
 * real detras).
 */
@RestController
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    // X-User-Username lo agrega el Gateway (UserHeadersFilter). Mismo
    // criterio que /ventas/mias: cualquier usuario logueado ve las suyas.
    @GetMapping("/mias")
    public ResponseEntity<List<FacturaDTO>> misFacturas(@RequestHeader("X-User-Username") String username) {
        return ResponseEntity.ok(facturaService.listarDeUsuario(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.obtenerPorId(id));
    }

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<FacturaDTO> obtenerPorVentaId(@PathVariable Long ventaId) {
        return ResponseEntity.ok(facturaService.obtenerPorVentaId(ventaId));
    }
}
