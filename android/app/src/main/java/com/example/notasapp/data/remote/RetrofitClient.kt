package com.example.notasapp.data.remote

import android.content.Context

import okhttp3.Interceptor
import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit


object RetrofitClient {

    // Dirección del backend mediante redirección USB
    private const val URL_SERVIDOR = "http://127.0.0.1:5000/"

    private const val NOMBRE_PREFERENCIAS = "NotasAppSesion"

    private const val CLAVE_TOKEN = "token"


    fun obtenerApi(contexto: Context): ApiService {

        // Acceso a los datos de sesión almacenados
        val almacenamientoSesion = contexto.applicationContext
            .getSharedPreferences(
                NOMBRE_PREFERENCIAS,
                Context.MODE_PRIVATE
            )

        // Incorporar el token en las solicitudes autenticadas
        val interceptorAutenticacion = Interceptor { cadena ->

            val tokenGuardado = almacenamientoSesion.getString(
                CLAVE_TOKEN,
                null
            )

            val constructorPeticion = cadena.request()
                .newBuilder()

            if (!tokenGuardado.isNullOrEmpty()) {

                constructorPeticion.header(
                    "Authorization",
                    "Bearer $tokenGuardado"
                )
            }

            val peticionAutorizada = constructorPeticion.build()

            cadena.proceed(peticionAutorizada)
        }

        // Configuración del cliente HTTP
        val clienteHttp = OkHttpClient.Builder()
            .addInterceptor(interceptorAutenticacion)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        // Configuración principal de Retrofit
        val conexionRetrofit = Retrofit.Builder()
            .baseUrl(URL_SERVIDOR)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return conexionRetrofit.create(ApiService::class.java)
    }
}