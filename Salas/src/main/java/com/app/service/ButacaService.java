package com.app.service;

import com.app.dto.ButacaDTO;
import com.app.enums.EstadoButaca;

import java.util.List;

public interface ButacaService {
    List<ButacaDTO> obtenerButacasPorSala(Long salaId);
    ButacaDTO cambiarEstadoButaca(Long butacaId, EstadoButaca nuevoEstado);
}