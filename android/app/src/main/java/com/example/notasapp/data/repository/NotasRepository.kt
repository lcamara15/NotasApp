package com.example.notasapp.data.repository

import android.content.Context

import com.example.notasapp.data.model.NotaPersonal
import com.example.notasapp.data.model.SolicitudNota
import com.example.notasapp.data.model.RespuestaGuardarNota
import com.example.notasapp.data.model.RespuestaListadoNotas
import com.example.notasapp.data.remote.RetrofitClient

import retrofit2.Response


class NotasRepository(contexto: Context) {

    private val servicioNotas =
        RetrofitClient.obtenerApi(contexto)


    // Registrar una nota en el backend
    suspend fun guardarNota(
        tituloNota: String,
        contenidoNota: String
    ): Response<RespuestaGuardarNota> {

        val nuevaNota = SolicitudNota(
            titulo = tituloNota,
            contenido = contenidoNota
        )

        return servicioNotas.registrarNota(nuevaNota)
    }


    // Recuperar las notas del usuario autenticado
    suspend fun consultarNotas(): Response<RespuestaListadoNotas> {

        return servicioNotas.obtenerNotas()
    }
}