package com.app.model;

import com.app.enums.CategoriaProducto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;

    // Locking optimista: sin esto, dos compras del mismo producto casi
    // simultaneas (ej. dos webhooks de Mercado Pago procesandose a la vez)
    // podian leer el mismo stock, las dos pasar la validacion y las dos
    // guardar, dejando el stock final mal (incluso negativo). Con @Version,
    // la segunda escritura que llega tarde falla con
    // ObjectOptimisticLockingFailureException (409) en vez de pisar la
    // primera en silencio — ver ajustarStock().
    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private CategoriaProducto categoria;

    // URL de una imagen (no se guarda el archivo en si, para no necesitar
    // un servidor de almacenamiento aparte). Opcional: si no se carga, el
    // frontend muestra un emoji segun la categoria.
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    // Baja logica: en vez de borrar la fila se marca como inactiva,
    // asi no se pierde el historial de ventas que referencia este producto.
    private Boolean activo = true;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;
}
