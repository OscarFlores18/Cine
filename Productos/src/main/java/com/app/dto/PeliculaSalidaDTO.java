package com.app.dto;

import com.app.model.Pelicula;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeliculaSalidaDTO(
    Long id,
    String titulo,
    String genero,
    Integer duracion,
    String director,
    String guionista,
    LocalDate fechaEstreno,
    String sinopsis,
    String clasificacionEdad,
    Double calificacion,
    String poster,
    String reparto,
    String formatoDisponible,
    Boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {

    public static PeliculaSalidaDTO from(Pelicula p) {
        return new PeliculaSalidaDTO(
                p.getId(),
                p.getTitulo(),
                p.getGenero(),
                p.getDuracion(),
                p.getDirector(),
                p.getGuionista(),
                p.getFechaEstreno(),
                p.getSinopsis(),
                p.getClasificacionEdad(),
                p.getCalificacion(),
                p.getPoster(),
                p.getReparto(),
                p.getFormatoDisponible(),
                p.getActivo(),
                p.getCreadoEn(),
                p.getActualizadoEn()
        );
    }
}
