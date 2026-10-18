package com.app.service;

import com.app.dto.SalaAltaDTO;
import com.app.dto.SalaEditarDTO;
import com.app.model.Sala;
import java.util.List;

public interface SalaService {
    Sala altaSalaConMapa(SalaAltaDTO dto);
    Sala altaSala(Sala sala);
    List<Sala> listarSalas();
    Sala obtenerPorId(Long id);
    Sala editarSala(Long id, SalaEditarDTO dto);
    void eliminarSala(Long id);
}