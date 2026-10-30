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


// Estados de la consulta al backend
sealed class EstadoListado {

    object Inicial : EstadoListado()

    object Consultando : EstadoListado()

    data class Completado(
        val notas: List<NotaPersonal>
    ) : EstadoListado()

    data class Error(
        val mensaje: String
    ) : EstadoListado()
}


class ListadoNotasViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repositorio = NotasRepository(application)

    private val _estadoListado =
        MutableLiveData<EstadoListado>(EstadoListado.Inicial)

    val estadoListado: LiveData<EstadoListado>
        get() = _estadoListado


    // Recuperar las notas desde Flask
    fun cargarNotas() {

        _estadoListado.value = EstadoListado.Consultando

        viewModelScope.launch {

            try {

                val respuesta = repositorio.consultarNotas()

                if (respuesta.isSuccessful) {

                    val informacion = respuesta.body()

                    if (informacion != null) {

                        _estadoListado.value = EstadoListado.Completado(
                            informacion.notas
                        )

                    } else {

                        _estadoListado.value = EstadoListado.Error(
                            "No se recibió información válida del servidor."
                        )
                    }

                } else {

                    val mensajeServidor = try {

                        val contenidoError =
                            respuesta.errorBody()?.string().orEmpty()

                        JSONObject(contenidoError)
                            .optString("mensaje")

                    } catch (excepcion: Exception) {
                        ""
                    }

                    val mensajeFinal = when {

                        respuesta.code() == 401 ->
                            "Tu sesión ha expirado. Inicia sesión nuevamente."

                        mensajeServidor.isNotBlank() ->
                            mensajeServidor

                        else ->
                            "No fue posible recuperar tus notas."
                    }

                    _estadoListado.value = EstadoListado.Error(
                        mensajeFinal
                    )
                }

            } catch (excepcion: IOException) {

                _estadoListado.value = EstadoListado.Error(
                    "No se pudo establecer conexión con el servidor."
                )

            } catch (excepcion: Exception) {

                _estadoListado.value = EstadoListado.Error(
                    "Ocurrió un inconveniente al consultar las notas."
                )
            }
        }
    }
}