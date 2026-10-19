package com.app.dto;

// Body de PATCH /auth/usuarios/{username}/estado (ADMIN). Un record de un
// solo campo en vez de un boolean en la URL/query param para que quede
// explicito en el body que es lo que se esta pidiendo, y para poder sumarle
// mas campos a futuro (ej. "motivo") sin romper la firma del endpoint.
public record ActualizarEstadoUsuarioRequest(boolean habilitado) {
}
