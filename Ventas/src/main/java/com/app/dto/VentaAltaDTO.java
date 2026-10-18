package com.app.dto;

import com.app.mapper.IMapper;
import com.app.model.Venta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Datos que manda el cliente para confirmar una compra: funcion + UNA O
 * VARIAS butacas elegidas (soporte real para "comprar muchas butacas en
 * una entrada"), y opcionalmente productos de confiteria.
 *
 * El total NO lo manda el cliente: lo calcula Ventas con el precio que le
 * confirma Sala/Productos por Feign, para no confiar en un valor que podria
 * venir manipulado.
 */
public record VentaAltaDTO(
    @NotNull Long funcionId,
    @NotEmpty List<Long> butacaIds,
    @Valid List<ProductoCompraDTO> productos,
    // Texto informativo, no validado: el front ya conoce el titulo de la
    // pelicula al momento de la compra; Productos no tiene entidad
    // Pelicula, asi que Ventas no tiene de donde sacarlo por su cuenta.
    String peliculaTitulo
) implements IMapper<Venta> {

    @Override
    public Venta mapperTo() {
        Venta venta = new Venta();
        venta.setFuncionId(funcionId);
        venta.setPeliculaTitulo(peliculaTitulo);
        return venta;
    }
}
