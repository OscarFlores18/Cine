package com.app.repository;

import com.app.model.Funcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FuncionRepository extends JpaRepository<Funcion, Long> {
    List<Funcion> findBySalaId(Long salaId);
    List<Funcion> findByFechaHoraAfter(LocalDateTime fechaHora);
    List<Funcion> findByPeliculaId(Long peliculaId);
}