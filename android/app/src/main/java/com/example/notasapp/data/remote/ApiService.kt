package com.example.notasapp.data.remote

import com.example.notasapp.data.model.SolicitudAcceso
import com.example.notasapp.data.model.RespuestaAcceso

import com.example.notasapp.data.model.SolicitudNota
import com.example.notasapp.data.model.RespuestaGuardarNota
import com.example.notasapp.data.model.RespuestaListadoNotas

import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiService {

    // Autenticación de usuarios
    @POST("api/login")
    suspend fun autenticarCuenta(
        @Body credenciales: SolicitudAcceso
    ): Response<RespuestaAcceso>


    // Almacenamiento de una nueva nota
    @POST("api/notas")
    suspend fun registrarNota(
        @Body datosNota: SolicitudNota
    ): Response<RespuestaGuardarNota>


    // Consulta de las notas del usuario autenticado
    @GET("api/notas")
    suspend fun obtenerNotas(): Response<RespuestaListadoNotas>

}