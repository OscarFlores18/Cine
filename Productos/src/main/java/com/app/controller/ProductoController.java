package com.app.controller;

import com.app.dto.ProductoAltaDTO;
import com.app.model.Producto;
import com.app.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<Producto> altaProducto(@Valid @RequestBody ProductoAltaDTO dto) {
        Producto nuevo = new Producto();
        nuevo.setNombre(dto.nombre());
        nuevo.setDescripcion(dto.descripcion());
        nuevo.setPrecio(dto.precio());
        nuevo.setStock(dto.stock());
        nuevo.setCategoria(dto.categoria());
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.altaProducto(nuevo));
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.listarProductos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
