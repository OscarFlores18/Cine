package com.app.service;

import com.app.dto.SalaAltaDTO;
import com.app.dto.SalaEditarDTO;
import com.app.enums.EstadoButaca;
import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Butaca;
import com.app.model.Sala;
import com.app.repository.ButacaRepository;
import com.app.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaServiceImpl implements SalaService {

    private final SalaRepository salaRepository;
    private final ButacaRepository butacaRepository;

    @Override
    @Transactional
    public Sala altaSalaConMapa(SalaAltaDTO dto) {
        int capacidadCalculada = dto.filas() * dto.asientosPorFila();

        Sala sala = new Sala();
        sala.setNombre(dto.nombre());
        sala.setTipo(dto.tipo());
        sala.setCapacidad(capacidadCalculada);

        Sala salaGuardada = salaRepository.save(sala);

        List<Butaca> butacas = new ArrayList<>();
        for (int i = 0; i < dto.filas(); i++) {
            String letraFila = String.valueOf((char) ('A' + i));
            for (int j = 1; j <= dto.asientosPorFila(); j++) {
                Butaca butaca = new Butaca();
                butaca.setSala(salaGuardada);
                butaca.setFila(letraFila);
                butaca.setNumero(j);
                butaca.setEstado(EstadoButaca.DISPONIBLE);
                butacas.add(butaca);
            }
        }
        butacaRepository.saveAll(butacas);
        return salaGuardada;
    }

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
    @Transactional
    public Sala editarSala(Long id, SalaEditarDTO dto) {
        Sala sala = obtenerPorId(id);
        sala.setNombre(dto.nombre());
        sala.setTipo(dto.tipo());
        return salaRepository.save(sala);
    }

    @Override
    @Transactional
    public void eliminarSala(Long id) {
        obtenerPorId(id);
        salaRepository.deleteById(id);
    }
}