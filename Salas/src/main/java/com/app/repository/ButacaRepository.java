package com.app.repository;

import com.app.enums.EstadoButaca;
import com.app.model.Butaca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ButacaRepository extends JpaRepository<Butaca, Long> {
    List<Butaca> findBySalaId(Long salaId);
    List<Butaca> findBySalaIdAndEstado(Long salaId, EstadoButaca estado);
}