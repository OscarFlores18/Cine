package com.app.service;

import com.app.dto.PeliculaAltaDTO;
import com.app.dto.PeliculaModificarDTO;
import com.app.dto.PeliculaSalidaDTO;

import java.util.List;

public interface PeliculaService {
    PeliculaSalidaDTO crearPelicula(PeliculaAltaDTO dto);
    PeliculaSalidaDTO modificarPelicula(Long id, PeliculaModificarDTO dto);
    List<PeliculaSalidaDTO> listarCartelera();
    List<PeliculaSalidaDTO> listarTodas();
    PeliculaSalidaDTO obtenerPorId(Long id);
    void eliminarPelicula(Long id);
}
