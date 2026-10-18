package com.app.dto;

import com.app.model.Usuario;

// DTO liviano para que OTROS microservicios (hoy: Ventas, para armar la
// factura con el nombre real del comprador) puedan resolver un username a
// nombre/apellido via Feign, sin exponer el hash de password ni el rol.
public record UsuarioDTO(String username, String nombre, String apellido, String email) {
    public static UsuarioDTO from(Usuario usuario) {
        return new UsuarioDTO(usuario.getUsername(), usuario.getNombre(), usuario.getApellido(), usuario.getEmail());
    }
}
