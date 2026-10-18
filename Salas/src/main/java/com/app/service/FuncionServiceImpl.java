package com.app.service;

import com.app.dto.FuncionAltaDTO;
import com.app.dto.FuncionEditarDTO;
import com.app.dto.FuncionResponseDTO;
import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Funcion;
import com.app.model.Sala;
import com.app.repository.FuncionRepository;
import com.app.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionServiceImpl implements FuncionService {

    private final FuncionRepository funcionRepository;
    private final SalaRepository salaRepository;

    @Override
    @Transactional
    public FuncionResponseDTO crearFuncion(FuncionAltaDTO dto) {
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala no encontrada id: " + dto.salaId()));

        Funcion f = new Funcion();
        f.setSala(sala);
        f.setPeliculaId(dto.peliculaId());
        f.setFechaHora(dto.fechaHora());
        f.setPrecioBase(dto.precioBase());

        Funcion guardada = funcionRepository.save(f);
        return mapToDTO(guardada);
    }

    @Override
    public List<FuncionResponseDTO> listarCartelera() {
        return funcionRepository.findByFechaHoraAfter(LocalDateTime.now())
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<FuncionResponseDTO> listarPorSala(Long salaId) {
        return funcionRepository.findBySalaId(salaId)
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public FuncionResponseDTO obtenerPorId(Long id) {
        Funcion f = funcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Función no encontrada con id: " + id));
        return mapToDTO(f);
    }

    @Override
    @Transactional
    public FuncionResponseDTO editarFuncion(Long id, FuncionEditarDTO dto) {
        Funcion f = funcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Función no encontrada con id: " + id));
        f.setPeliculaId(dto.peliculaId());
        f.setFechaHora(dto.fechaHora());
        f.setPrecioBase(dto.precioBase());
        return mapToDTO(funcionRepository.save(f));
    }

    @Override
    @Transactional
    public void eliminarFuncion(Long id) {
        obtenerPorId(id);
        funcionRepository.deleteById(id);
    }

    private FuncionResponseDTO mapToDTO(Funcion f) {
        return new FuncionResponseDTO(
                f.getId(),
                f.getSala().getId(),
                f.getSala().getNombre(),
                f.getPeliculaId(),
                f.getFechaHora(),
                f.getPrecioBase()
        );
    }
}