package com.app.repository;

import com.app.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByUsername(String username);
    Optional<Factura> findByVentaId(Long ventaId);
}
