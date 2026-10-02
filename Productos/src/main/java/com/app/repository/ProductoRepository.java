package com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
