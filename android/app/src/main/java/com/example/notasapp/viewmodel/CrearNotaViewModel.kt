package com.example.notasapp.viewmodel

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.example.notasapp.data.model.NotaPersonal
import com.example.notasapp.data.repository.NotasRepository

import kotlinx.coroutines.launch

import org.json.JSONObject

import java.io.IOException

// Estados de la operación de guardado
sealed class EstadoGuardarNota {

    object Inicial : EstadoGuardarNota()

    object Guardando : EstadoGuardarNota()

    data class Guardada(
        val mensaje: String,
        val nota: NotaPersonal
    ) : EstadoGuardarNota()

    data class Error(
        val mensaje: String
    ) : EstadoGuardarNota()
}


class CrearNotaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repositorioNotas = NotasRepository(application)

    private val _estadoGuardado =
        MutableLiveData<EstadoGuardarNota>(EstadoGuardarNota.Inicial)

    val estadoGuardado: LiveData<EstadoGuardarNota>
        get() = _estadoGuardado


    // Validar y enviar una nueva nota
    fun registrarNuevaNota(
        titulo: String,
        contenido: String
    ) {

        val tituloValidado = titulo.trim()
        val contenidoValidado = contenido.trim()

        if (tituloValidado.isEmpty() || contenidoValidado.isEmpty()) {

            _estadoGuardado.value = EstadoGuardarNota.Error(
                "Completa el título y el contenido antes de guardar."
            )

            return
        }

        if (tituloValidado.length > 150) {

            _estadoGuardado.value = EstadoGuardarNota.Error(
                "El título no puede superar los 150 caracteres."
            )

            return
        }

        _estadoGuardado.value = EstadoGuardarNota.Guardando

        viewModelScope.launch {

            try {

                val respuestaServidor = repositorioNotas.guardarNota(
                    tituloValidado,
                    contenidoValidado
                )

                if (respuestaServidor.isSuccessful) {

                    val resultado = respuestaServidor.body()

                    if (resultado != null) {

                        _estadoGuardado.value = EstadoGuardarNota.Guardada(
                            mensaje = resultado.mensaje,
                            nota = resultado.nota
                        )

                    } else {

                        _estadoGuardado.value = EstadoGuardarNota.Error(
                            "El servidor no devolvió la información de la nota."
                        )
                    }

                } else {

                    val mensajeBackend = try {

                        val respuestaError =
                            respuestaServidor.errorBody()?.string().orEmpty()

                        JSONObject(respuestaError)
                            .optString("mensaje")

                    } catch (excepcion: Exception) {
                        ""
                    }

                    val mensajeFinal = when {

                        respuestaServidor.code() == 401 ->
                            "Tu sesión ha expirado. Inicia sesión nuevamente."

                        mensajeBackend.isNotBlank() ->
                            mensajeBackend

                        else ->
                            "No fue posible guardar la nota. Inténtalo nuevamente."
                    }

                    _estadoGuardado.value = EstadoGuardarNota.Error(
                        mensajeFinal
                    )
                }

            } catch (excepcion: IOException) {

                _estadoGuardado.value = EstadoGuardarNota.Error(
                    "No se pudo establecer conexión con el servidor."
                )

            } catch (excepcion: Exception) {

                _estadoGuardado.value = EstadoGuardarNota.Error(
                    "Ocurrió un problema al procesar la nota."
                )
            }
        }
    }
}