package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByUsername(String username);
}
