package com.app.config;

import com.app.enums.CategoriaProducto;
import com.app.model.Producto;
import com.app.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga un catalogo inicial de productos (pochoclos, bebidas, combos, etc.)
 * la primera vez que arranca el microservicio, para que el frontend tenga
 * datos que mostrar sin cargarlos a mano.
 *
 * Como la base es H2 en memoria, en cada arranque esta vacia y se vuelve a
 * sembrar. Si ya hay productos (ej: una base persistente), no hace nada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            return;
        }

        List<Producto> productos = List.of(
                producto("Combo Mega Pochoclo + 2 Gaseosas",
                        "1 Balde de pochoclos gigantes (dulces o salados) + 2 gaseosas grandes de 1L.",
                        "14500", 40, CategoriaProducto.COMBO),

                producto("Pochoclo Grande",
                        "Balde grande de pochoclos recién hechos.",
                        "6500", 60, CategoriaProducto.COMIDA),

                producto("Nacho Supreme con Queso Cheddar",
                        "Caja de nachos crocantes con salsa de queso cheddar caliente y jalapeños.",
                        "8900", 35, CategoriaProducto.COMIDA),

                producto("Gaseosa Grande 1 Litro",
                        "Coca-Cola, Coca Zero, Sprite o Fanta helada de 1000ml.",
                        "4500", 80, CategoriaProducto.BEBIDA),

                producto("Agua Mineral 500ml",
                        "Botella de agua mineral sin gas, bien fría.",
                        "2500", 100, CategoriaProducto.BEBIDA),

                producto("Vaso Coleccionable Resident Evil",
                        "Vaso de edición limitada de 1.2L con figura 3D en la tapa.",
                        "12000", 25, CategoriaProducto.MERCHANDISING)
        );

        productoRepository.saveAll(productos);
        log.info("Cargados {} productos predeterminados en modulo-productos", productos.size());
    }

    private Producto producto(String nombre, String descripcion, String precio,
                              int stock, CategoriaProducto categoria) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(new BigDecimal(precio));
        p.setStock(stock);
        p.setCategoria(categoria);
        p.setActivo(true);
        return p;
    }
}
