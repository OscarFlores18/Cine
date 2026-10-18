package com.app.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quien esta haciendo el pedido, segun las cabeceras que agrega el Gateway
 * despues de validar el JWT: X-User-Username y X-User-Roles (ej: ROLE_ADMIN).
 *
 * Es seguro confiar en ellas porque: (1) el Gateway borra las que mande el
 * cliente y (2) GatewayInterceptor rechaza cualquier pedido que no pase por
 * el Gateway.
 */
public record UsuarioActual(String username, Set<String> roles) {

    public static final String USERNAME_HEADER = "X-User-Username";
    public static final String ROLES_HEADER = "X-User-Roles";

    public static UsuarioActual desde(HttpServletRequest request) {
        String roles = request.getHeader(ROLES_HEADER);
        Set<String> rolesSet = roles == null || roles.isBlank()
                ? Set.of()
                : Arrays.stream(roles.split(",")).map(String::trim).collect(Collectors.toSet());

        return new UsuarioActual(request.getHeader(USERNAME_HEADER), rolesSet);
    }

    public boolean tieneRol(String rol) {
        return roles.contains("ROLE_" + rol);
    }

    public boolean esAdmin() {
        return tieneRol("ADMIN");
    }

    public boolean esEmpleado() {
        return tieneRol("EMPLEADO");
    }
}
