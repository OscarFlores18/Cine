package com.app.controller;

import com.app.dto.ActualizarEstadoButacaDTO;
import com.app.dto.ButacaDTO;
import com.app.service.ButacaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/butacas")
@RequiredArgsConstructor
@Tag(name = "Butacas", description = "Endpoints para mapa de asientos y reservas")
public class ButacaController {

    private final ButacaService butacaService;

    @GetMapping("/sala/{salaId}")
    @Operation(summary = "Obtener el mapa de asientos de una sala")
    public ResponseEntity<List<ButacaDTO>> obtenerPorSala(@PathVariable Long salaId) {
        return ResponseEntity.ok(butacaService.obtenerButacasPorSala(salaId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de una butaca (DISPONIBLE / OCUPADO)")
    public ResponseEntity<ButacaDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoButacaDTO dto) {
        return ResponseEntity.ok(butacaService.cambiarEstadoButaca(id, dto.estado()));
    }
}