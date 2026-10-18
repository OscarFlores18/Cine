package com.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pelicula para la cartelera. Vive en el mismo microservicio que Producto
 * (modulo-productos) porque conceptualmente es "lo que se exhibe/vende" en
 * el cine, igual que la confiteria, y asi no hace falta levantar un modulo
 * nuevo solo para esto. No tiene relacion con Funcion (en Salas): Funcion
 * solo guarda el peliculaId como un Long suelto, sin validarlo via Feign
 * (si Productos estuviera caido, Salas igual podria programar funciones).
 */
@Entity
@Table(name = "peliculas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String genero;
    private Integer duracion;
    private String director;
    private String guionista;

    // Fecha en que la pelicula se estrena/estreno en el cine (distinta de
    // creadoEn, que es cuando se cargo el registro en el sistema).
    private LocalDate fechaEstreno;

    @Column(length = 1000)
    private String sinopsis;

    private String clasificacionEdad;
    private Double calificacion;

    // URL de la imagen del poster (no se almacena el archivo en si).
    @Column(name = "poster_url", length = 500)
    private String poster;

    private String reparto;
    private String formatoDisponible;

    // Baja logica: una pelicula que sale de cartelera no se borra (puede
    // tener funciones ya vendidas que la referencian por id).
    private Boolean activo = true;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;
}
