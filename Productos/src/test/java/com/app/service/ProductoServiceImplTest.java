package com.app.service;

import com.app.dto.ProductoAltaDTO;
import com.app.dto.ProductoModificarDTO;
import com.app.dto.ProductoSalidaDTO;
import com.app.enums.CategoriaProducto;
import com.app.exception.ProductoDuplicadoException;
import com.app.exception.ProductoInactivoException;
import com.app.exception.RecursoNoEncontradoException;
import com.app.exception.StockInsuficienteException;
import com.app.model.Producto;
import com.app.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto(Long id, String nombre, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setDescripcion("desc");
        p.setPrecio(new BigDecimal("100.00"));
        p.setStock(stock);
        p.setCategoria(CategoriaProducto.COMIDA);
        p.setActivo(true);
        return p;
    }

    @Test
    void altaProducto_cuandoNombreNoExiste_guardaYDevuelveSalida() {
        when(productoRepository.existsByNombreIgnoreCase("Pochoclos")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductoSalidaDTO salida = productoService.altaProducto(
                new ProductoAltaDTO("Pochoclos", "grandes", new BigDecimal("1500.00"), 10, CategoriaProducto.COMIDA));

        assertThat(salida.id()).isEqualTo(1L);
        assertThat(salida.nombre()).isEqualTo("Pochoclos");
        assertThat(salida.activo()).isTrue();
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void altaProducto_cuandoNombreExiste_lanzaDuplicado() {
        when(productoRepository.existsByNombreIgnoreCase("Pochoclos")).thenReturn(true);

        assertThatThrownBy(() -> productoService.altaProducto(
                new ProductoAltaDTO("Pochoclos", "d", new BigDecimal("10.00"), 1, CategoriaProducto.COMIDA)))
                .isInstanceOf(ProductoDuplicadoException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_lanzaNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void modificarProducto_cuandoOtroTieneElNombre_lanzaDuplicado() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Viejo", 3)));
        when(productoRepository.existsByNombreIgnoreCaseAndIdNot("Nuevo", 1L)).thenReturn(true);

        assertThatThrownBy(() -> productoService.modificarProducto(1L,
                new ProductoModificarDTO("Nuevo", "d", new BigDecimal("10.00"), 1, CategoriaProducto.COMIDA)))
                .isInstanceOf(ProductoDuplicadoException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    void modificarProducto_cuandoEsValido_actualizaCampos() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Viejo", 3)));
        when(productoRepository.existsByNombreIgnoreCaseAndIdNot("Nuevo", 1L)).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoSalidaDTO salida = productoService.modificarProducto(1L,
                new ProductoModificarDTO("Nuevo", "nueva desc", new BigDecimal("250.00"), 9, CategoriaProducto.BEBIDA));

        assertThat(salida.nombre()).isEqualTo("Nuevo");
        assertThat(salida.stock()).isEqualTo(9);
        assertThat(salida.categoria()).isEqualTo(CategoriaProducto.BEBIDA);
    }

    @Test
    void ajustarStock_cuandoQuedaNegativo_lanzaStockInsuficiente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Pochoclos", 3)));

        assertThatThrownBy(() -> productoService.ajustarStock(1L, -10))
                .isInstanceOf(StockInsuficienteException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    void ajustarStock_cuandoEsValido_actualizaStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Pochoclos", 3)));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoSalidaDTO salida = productoService.ajustarStock(1L, 7);

        assertThat(salida.stock()).isEqualTo(10);
    }

    @Test
    void ajustarStock_cuandoProductoInactivo_lanzaInactivo() {
        Producto inactivo = producto(1L, "Pochoclos", 3);
        inactivo.setActivo(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(inactivo));

        assertThatThrownBy(() -> productoService.ajustarStock(1L, 1))
                .isInstanceOf(ProductoInactivoException.class);
    }

    @Test
    void eliminarProducto_haceBajaLogica() {
        Producto p = producto(1L, "Pochoclos", 3);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.eliminarProducto(1L);

        assertThat(p.getActivo()).isFalse();
        verify(productoRepository).save(p);
    }

    @Test
    void reactivarProducto_vuelveAActivo() {
        Producto p = producto(1L, "Pochoclos", 3);
        p.setActivo(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoSalidaDTO salida = productoService.reactivarProducto(1L);

        assertThat(salida.activo()).isTrue();
    }
}
