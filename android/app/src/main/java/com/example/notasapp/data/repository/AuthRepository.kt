package com.example.notasapp.data.repository

import android.content.Context

import com.example.notasapp.data.model.SolicitudAcceso
import com.example.notasapp.data.model.RespuestaAcceso
import com.example.notasapp.data.remote.RetrofitClient

import retrofit2.Response


class AuthRepository(contexto: Context) {

    // Servicio encargado de comunicarse con Flask
    private val servicioAutenticacion =
        RetrofitClient.obtenerApi(contexto)


    // Enviar credenciales para verificar el acceso
    suspend fun verificarCredenciales(
        correoElectronico: String,
        claveAcceso: String
    ): Response<RespuestaAcceso> {

        val datosAcceso = SolicitudAcceso(
            email = correoElectronico,
            password = claveAcceso
        )

        return servicioAutenticacion.autenticarCuenta(
            datosAcceso
        )
    }
}