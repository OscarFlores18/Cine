package com.app.service;

import com.app.dto.ButacaDTO;
import com.app.enums.EstadoButaca;
import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Butaca;
import com.app.repository.ButacaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ButacaServiceImpl implements ButacaService {

    private final ButacaRepository butacaRepository;

    @Override
    public List<ButacaDTO> obtenerButacasPorSala(Long salaId) {
        return butacaRepository.findBySalaId(salaId).stream()
                .map(b -> new ButacaDTO(b.getId(), b.getSala().getId(), b.getFila(), b.getNumero(), b.getEstado()))
                .toList();
    }

    @Override
    @Transactional
    public ButacaDTO cambiarEstadoButaca(Long butacaId, EstadoButaca nuevoEstado) {
        Butaca b = butacaRepository.findById(butacaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Butaca no encontrada con id: " + butacaId));
        b.setEstado(nuevoEstado);
        Butaca guardada = butacaRepository.save(b);
        return new ButacaDTO(guardada.getId(), guardada.getSala().getId(), guardada.getFila(), guardada.getNumero(), guardada.getEstado());
    }
}