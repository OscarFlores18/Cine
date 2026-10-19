package com.app.client;

import com.app.config.FeignConfig;
import com.app.dto.AjustarStockDTO;
import com.app.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente Feign (comunicacion SINCRONICA) hacia el microservicio Productos.
 *
 * Reescrito para calzar con el contrato REAL: Productos solo expone un
 * catalogo plano "GET /productos/{id}" (sin "/confiteria" ni "/peliculas" —
 * esas rutas eran una suposicion del Figma que el equipo de Productos nunca
 * implemento, y no existe ninguna entidad Pelicula en ese microservicio).
 *
 * Por eso el titulo de la pelicula para la factura YA NO se busca por Feign:
 * ahora lo manda el propio front como texto informativo en VentaAltaDTO
 * (peliculaTitulo), porque es el unico lugar que hoy conoce esa info.
 */
@FeignClient(name = "modulo-productos", configuration = FeignConfig.class)
public interface ProductoClient {

    @GetMapping("/productos/{id}")
    ProductoDTO obtenerProducto(@PathVariable("id") Long id);

    // Ajuste relativo de stock (negativo descuenta, positivo repone). Se usa
    // para descontar al confirmarse el pago y para devolver stock si una
    // venta ya pagada se cancela.
    @PatchMapping("/productos/{id}/stock")
    ProductoDTO ajustarStock(@PathVariable("id") Long id, @RequestBody AjustarStockDTO dto);
}
