package com.app.client;

import com.app.config.FeignConfig;
import com.app.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign hacia Auth, SOLO para resolver nombre/apellido reales del
 * comprador y mostrarlos en la factura del mail de confirmacion (el ticket
 * de Cineplanet de referencia muestra "WALTER RONAL QUISPE PRETEL", no un
 * username). "auth" es el spring.application.name con el que ese
 * microservicio se registra en Eureka.
 */
@FeignClient(name = "auth", configuration = FeignConfig.class)
public interface AuthClient {

    @GetMapping("/auth/usuarios/{username}")
    UsuarioDTO obtenerUsuario(@PathVariable("username") String username);
}
