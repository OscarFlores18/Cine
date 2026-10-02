package com.app.controller;

import com.app.dto.SalaAltaDTO;
import com.app.model.Sala;
import com.app.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    public ResponseEntity<Sala> altaSala(@Valid @RequestBody SalaAltaDTO dto) {
        Sala nueva = new Sala();
        nueva.setNombre(dto.nombre());
        nueva.setCapacidad(dto.capacidad());
        nueva.setTipo(dto.tipo());
        return ResponseEntity.status(HttpStatus.CREATED).body(salaService.altaSala(nueva));
    }

    @GetMapping
    public ResponseEntity<List<Sala>> listarSalas() {
        return ResponseEntity.ok(salaService.listarSalas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sala> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(salaService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSala(@PathVariable Long id) {
        salaService.eliminarSala(id);
        return ResponseEntity.noContent().build();
    }
}
