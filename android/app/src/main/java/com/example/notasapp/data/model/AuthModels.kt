package com.example.notasapp.data.model

// Información enviada para iniciar sesión
data class SolicitudAcceso(
    val email: String,
    val password: String
)

// Datos del usuario autenticado
data class UsuarioAutenticado(
    val id: Int,
    val nombre: String,
    val email: String
)

// Respuesta recibida después de la autenticación
data class RespuestaAcceso(
    val mensaje: String,
    val token: String,
    val usuario: UsuarioAutenticado
)

// Mensajes de error enviados por el backend
data class RespuestaError(
    val mensaje: String
)