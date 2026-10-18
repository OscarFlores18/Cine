package com.app.controller;

import com.app.dto.FuncionAltaDTO;
import com.app.dto.FuncionEditarDTO;
import com.app.dto.FuncionResponseDTO;
import com.app.service.FuncionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/funciones")
@RequiredArgsConstructor
@Tag(name = "Funciones", description = "Endpoints para la cartelera y programación de funciones")
public class FuncionController {

    private final FuncionService funcionService;

    @PostMapping
    @Operation(summary = "Programar una nueva función de cine")
    public ResponseEntity<FuncionResponseDTO> crear(@Valid @RequestBody FuncionAltaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionService.crearFuncion(dto));
    }

    @GetMapping("/cartelera")
    @Operation(summary = "Consultar cartelera de funciones activas (Público)")
    public ResponseEntity<List<FuncionResponseDTO>> listarCartelera() {
        return ResponseEntity.ok(funcionService.listarCartelera());
    }

    @GetMapping("/sala/{salaId}")
    @Operation(summary = "Listar funciones asignadas a una sala")
    public ResponseEntity<List<FuncionResponseDTO>> listarPorSala(@PathVariable Long salaId) {
        return ResponseEntity.ok(funcionService.listarPorSala(salaId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de función por ID")
    public ResponseEntity<FuncionResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(funcionService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar película/fecha/precio de una función (no se puede cambiar de sala)")
    public ResponseEntity<FuncionResponseDTO> editar(@PathVariable Long id, @Valid @RequestBody FuncionEditarDTO dto) {
        return ResponseEntity.ok(funcionService.editarFuncion(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar/Eliminar función")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        funcionService.eliminarFuncion(id);
        return ResponseEntity.noContent().build();
    }
}