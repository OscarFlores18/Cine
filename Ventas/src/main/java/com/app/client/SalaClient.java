package com.app.client;

import com.app.config.FeignConfig;
import com.app.dto.ButacaDTO;
import com.app.dto.FuncionDTO;
import com.app.dto.SalaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Cliente Feign (comunicacion SINCRONICA, solo LECTURA) hacia el
 * microservicio Sala.
 *
 * Reescrito para calzar con el contrato REAL implementado por el equipo de
 * Sala (confirmado leyendo su codigo, no el diagrama del Figma, que estaba
 * desactualizado en este punto):
 *
 *   GET /funciones/{id}        -> FuncionDTO (incluye nombreSala)
 *   GET /salas/{id}            -> SalaDTO
 *   GET /butacas/sala/{salaId} -> List<ButacaDTO>  (NO existe GET /butacas/{id} singular)
 *
 * El cambio de estado de una butaca (ocuparla al confirmar el pago,
 * liberarla al cancelar) YA NO se hace por Feign (no hay endpoint de
 * escritura pensado para eso, y Sala expone PATCH /butacas/{id}/estado como
 * accion administrativa, no como parte del flujo de compra). En su lugar,
 * Ventas publica un evento async a RabbitMQ (exchange.ventas / routing.butacas)
 * que el propio VentasConsumer de Sala escucha — ver RabbitMQConfig y
 * VentaServiceImpl.publicarCambioButaca().
 */
@FeignClient(name = "modulo-salas", configuration = FeignConfig.class)
public interface SalaClient {

    @GetMapping("/funciones/{id}")
    FuncionDTO obtenerFuncion(@PathVariable("id") Long id);

    @GetMapping("/salas/{id}")
    SalaDTO obtenerSala(@PathVariable("id") Long id);

    // Unica forma real de leer butacas: la lista completa de la sala. Ventas
    // filtra las que necesita (ver VentaServiceImpl.obtenerButacas).
    @GetMapping("/butacas/sala/{salaId}")
    List<ButacaDTO> obtenerButacasPorSala(@PathVariable("salaId") Long salaId);
}
