package com.app.dto;

import com.app.model.VentaButaca;

public record VentaButacaDTO(Long butacaId, String fila, Integer numero) {
    public static VentaButacaDTO from(VentaButaca vb) {
        return new VentaButacaDTO(vb.getButacaId(), vb.getFila(), vb.getNumero());
    }
}
