package com.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    // Nombre y apellido reales de la persona — se usan para mostrar
    // "comprado por" en la factura/resumen de compra que arma Ventas.
    private String nombre;
    private String apellido;

    // Direccion para mandar el mail de factura (lo pide MS Factura).
    private String email;

    // Nunca se guarda en texto plano: siempre pasa por BCryptPasswordEncoder
    // antes de llegar acá (ver AuthController y DataInitializer).
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;
}
