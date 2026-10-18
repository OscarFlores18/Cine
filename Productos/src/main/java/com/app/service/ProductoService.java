package com.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.dto.ProductoAltaDTO;
import com.app.dto.ProductoModificarDTO;
import com.app.dto.ProductoSalidaDTO;
import com.app.enums.CategoriaProducto;

public interface ProductoService {

    ProductoSalidaDTO altaProducto(ProductoAltaDTO dto);

    ProductoSalidaDTO modificarProducto(Long id, ProductoModificarDTO dto);

    Page<ProductoSalidaDTO> listarProductos(CategoriaProducto categoria, String nombre,
                                            boolean incluirInactivos, Pageable pageable);

    ProductoSalidaDTO obtenerPorId(Long id);

    List<ProductoSalidaDTO> listarBajoStock(int umbral);

    ProductoSalidaDTO ajustarStock(Long id, Integer cantidad);

    ProductoSalidaDTO reactivarProducto(Long id);

    void eliminarProducto(Long id);
}
