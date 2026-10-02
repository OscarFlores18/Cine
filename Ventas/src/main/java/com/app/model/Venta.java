package com.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que hizo la compra. Viene de la cabecera X-User-Username que
    // el Gateway arma a partir del JWT, no del body (asi nadie compra "como otro").
    private String username;

    private Long productoId;
    private Integer cantidad;
    private BigDecimal total;
    private LocalDateTime fecha;
}
