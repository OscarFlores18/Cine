package com.app.controller;

import com.app.dto.SalaAltaDTO;
import com.app.dto.SalaEditarDTO;
import com.app.model.Sala;
import com.app.service.SalaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salas")
@RequiredArgsConstructor
@Tag(name = "Salas", description = "Endpoints para la gestión de salas de cine")
public class SalaController {

    private final SalaService salaService;

    @PostMapping
    @Operation(summary = "Crear sala y autogenerar grilla de butacas")
    public ResponseEntity<Sala> altaSala(@Valid @RequestBody SalaAltaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaService.altaSalaConMapa(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todas las salas")
    public ResponseEntity<List<Sala>> listarSalas() {
        return ResponseEntity.ok(salaService.listarSalas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de sala por ID")
    public ResponseEntity<Sala> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(salaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar nombre/tipo de una sala (no toca el mapa de butacas ya generado)")
    public ResponseEntity<Sala> editarSala(@PathVariable Long id, @Valid @RequestBody SalaEditarDTO dto) {
        return ResponseEntity.ok(salaService.editarSala(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sala por ID")
    public ResponseEntity<Void> eliminarSala(@PathVariable Long id) {
        salaService.eliminarSala(id);
        return ResponseEntity.noContent().build();
    }
}