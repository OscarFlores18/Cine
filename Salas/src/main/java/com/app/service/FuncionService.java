package com.app.service;

import com.app.dto.FuncionAltaDTO;
import com.app.dto.FuncionEditarDTO;
import com.app.dto.FuncionResponseDTO;

import java.util.List;

public interface FuncionService {
    FuncionResponseDTO crearFuncion(FuncionAltaDTO dto);
    List<FuncionResponseDTO> listarCartelera();
    List<FuncionResponseDTO> listarPorSala(Long salaId);
    FuncionResponseDTO obtenerPorId(Long id);
    FuncionResponseDTO editarFuncion(Long id, FuncionEditarDTO dto);
    void eliminarFuncion(Long id);
}