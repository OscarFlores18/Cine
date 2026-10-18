package com.app.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * La factura/resumen de compra como RECURSO PROPIO, en su propia base, en
 * su propio microservicio — a pedido explicito del profesor (antes era
 * solo un evento que armaba Ventas y consumia Email, sin persistencia
 * propia ni separacion de responsabilidades real).
 *
 * Se llena a partir de VentaConfirmadaEvent (ver FacturaEventDTO), que
 * publica MS Ventas por RabbitMQ cuando se confirma el pago. Es una copia
 * snapshot de esos datos: si Venta, Sala o Productos cambian despues, la
 * factura ya emitida no cambia (es justamente lo que se espera de una
 * factura real).
 */
@Entity
@Table(name = "factura")
@Data
@NoArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ventaId;
    private String numeroCompra;
    private String username;
    private String nombreCompleto;
    private Long funcionId;
    private String peliculaTitulo;
    private String salaNombre;
    private String salaTipo;
    private LocalDateTime funcionFechaHora;
    private LocalDateTime fecha;

    @ElementCollection
    @CollectionTable(name = "factura_butaca", joinColumns = @JoinColumn(name = "factura_id"))
    @Column(name = "butaca")
    private List<String> butacas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "factura_item", joinColumns = @JoinColumn(name = "factura_id"))
    private List<FacturaItem> entradas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "factura_item_producto", joinColumns = @JoinColumn(name = "factura_id"))
    private List<FacturaItem> productos = new ArrayList<>();

    private BigDecimal subtotalEntradas = BigDecimal.ZERO;
    private BigDecimal subtotalProductos = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    // El mail de confirmacion NO lo manda Factura: MS Gmail escucha el
    // MISMO evento venta.confirmada con su propia cola
    // (cola.confirmacion-venta) y lo manda por su cuenta, en paralelo,
    // sin que Factura tenga que saber que Gmail existe. Si queres saber si
    // el mail de una venta salio, consultalo en Gmail (GET /gmail/historial).
}
