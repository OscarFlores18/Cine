package com.app.service;

import java.util.List;

import com.app.model.Sala;

public interface SalaService {
    Sala altaSala(Sala sala);
    List<Sala> listarSalas();
    Sala obtenerPorId(Long id);
    void eliminarSala(Long id);
}
