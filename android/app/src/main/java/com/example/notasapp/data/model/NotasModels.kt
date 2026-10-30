package com.example.notasapp.data.model

// Información enviada al crear una nota
data class SolicitudNota(
    val titulo: String,
    val contenido: String
)

// Estructura de una nota almacenada
data class NotaPersonal(
    val id: Int,
    val titulo: String,
    val contenido: String,
    val fecha_creacion: String
)

// Respuesta al guardar una nota
data class RespuestaGuardarNota(
    val mensaje: String,
    val nota: NotaPersonal
)

// Respuesta al consultar las notas
data class RespuestaListadoNotas(
    val mensaje: String,
    val total: Int,
    val notas: List<NotaPersonal>
)