package com.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una butaca dentro de una Venta. Reemplaza al viejo Venta.butacaId
 * ("una Venta = una butaca") para soportar comprar varias butacas en una
 * sola compra, mismo patron que VentaProducto (lineas hijas de Venta).
 *
 * fila/numero se copian ACA al momento de confirmar la compra (mismo
 * criterio que VentaProducto.precioUnitario): la factura tiene que poder
 * mostrar "Butaca J6" aunque Sala este caida despues, o aunque la numeracion
 * de la sala cambie en el futuro.
 */
@Entity
@Table(name = "venta_butaca")
@Data
@NoArgsConstructor
public class VentaButaca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    @JsonBackReference
    private Venta venta;

    private Long butacaId;
    private String fila;
    private Integer numero;

    public VentaButaca(Long butacaId, String fila, Integer numero) {
        this.butacaId = butacaId;
        this.fila = fila;
        this.numero = numero;
    }
}
