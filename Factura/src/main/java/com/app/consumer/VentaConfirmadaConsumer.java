package com.app.consumer;

import com.app.config.RabbitMQConfig;
import com.app.dto.VentaConfirmadaEventDTO;
import com.app.service.FacturaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VentaConfirmadaConsumer {

    private final FacturaService facturaService;

    @RabbitListener(queues = RabbitMQConfig.FACTURA_QUEUE)
    public void consumirVentaConfirmada(VentaConfirmadaEventDTO evento) {
        log.info("Evento venta.confirmada recibido para armar la factura de la venta {}", evento.idVenta());
        try {
            facturaService.procesarVentaConfirmada(evento);
        } catch (Exception e) {
            log.error("Error procesando la factura de la venta {}: {}", evento.idVenta(), e.getMessage());
        }
    }
}
