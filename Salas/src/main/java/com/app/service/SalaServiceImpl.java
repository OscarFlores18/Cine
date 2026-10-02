package com.app.service;

import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Sala;
import com.app.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaServiceImpl implements SalaService {

    private final SalaRepository salaRepository;

    @Override
    public Sala altaSala(Sala sala) {
        return salaRepository.save(sala);
    }

    @Override
    public List<Sala> listarSalas() {
        return salaRepository.findAll();
    }

    @Override
    public Sala obtenerPorId(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala no encontrada con id: " + id));
    }

    @Override
    public void eliminarSala(Long id) {
        obtenerPorId(id);
        salaRepository.deleteById(id);
    }
}
