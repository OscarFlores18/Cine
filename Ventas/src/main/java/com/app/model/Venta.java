package com.app.model;

import com.app.enums.EstadoReembolso;
import com.app.enums.EstadoVenta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Nombres de tabla/campo calcados del diagrama de BD "MS-VENTA" del Figma,
// con UNA correccion deliberada: butacaId pasa de ser una columna propia de
// Venta a una lista de VentaButaca (una Venta = N butacas), para soportar
// comprar varias entradas juntas en una sola compra. Mismo patron que ya
// existia para venta_producto.
@Entity
@Table(name = "venta")
@Data
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que hizo la compra. Viene de la cabecera X-User-Username que
    // el Gateway arma a partir del JWT, no del body (asi nadie compra "como otro").
    private String username;

    // Funcion (pelicula + sala + horario) elegida, en MS Sala. Las butacas
    // elegidas de esa funcion viven en la lista "butacas" de abajo.
    private Long funcionId;

    // Texto informativo que manda el front para la factura (titulo de la
    // pelicula que el usuario vio al elegir la funcion). Productos no tiene
    // entidad Pelicula, asi que no hay forma de validarlo contra ningun
    // microservicio: es solo para mostrar en el mail, no afecta el precio
    // ni la validacion de la compra.
    private String peliculaTitulo;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @Enumerated(EnumType.STRING)
    private EstadoReembolso estadoReembolso = EstadoReembolso.NO_APLICA;

    private BigDecimal total = BigDecimal.ZERO;
    private LocalDateTime fecha;

    // Datos de la preferencia/pago de Mercado Pago (ver MercadoPagoClient).
    private String preferenceId;
    private String paymentId;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VentaButaca> butacas = new ArrayList<>();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VentaProducto> productos = new ArrayList<>();

    public void addButaca(VentaButaca vb) {
        vb.setVenta(this);
        butacas.add(vb);
    }

    public void addProducto(VentaProducto vp) {
        vp.setVenta(this);
        productos.add(vp);
    }
}
