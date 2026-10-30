package com.example.notasapp.viewmodel

import android.app.Application
import android.content.Context

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.example.notasapp.data.model.RespuestaAcceso
import com.example.notasapp.data.repository.AuthRepository

import kotlinx.coroutines.launch
import org.json.JSONObject

import java.io.IOException

sealed class EstadoAcceso {

    object Inicial : EstadoAcceso()

    object Cargando : EstadoAcceso()

    data class Exitoso(
        val informacion: RespuestaAcceso
    ) : EstadoAcceso()

    data class Fallido(
        val mensaje: String
    ) : EstadoAcceso()
}


class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repositorio = AuthRepository(application)

    private val _estadoAcceso =
        MutableLiveData<EstadoAcceso>(EstadoAcceso.Inicial)

    val estadoAcceso: LiveData<EstadoAcceso>
        get() = _estadoAcceso


    fun autenticarUsuario(
        correo: String,
        contrasena: String
    ) {

        val correoValidado = correo.trim()

        if (correoValidado.isEmpty() || contrasena.isEmpty()) {

            _estadoAcceso.value = EstadoAcceso.Fallido(
                "Completa tu correo electrónico y contraseña."
            )

            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(correoValidado).matches()) {

            _estadoAcceso.value = EstadoAcceso.Fallido(
                "Ingresa una dirección de correo que sea válida."
            )

            return
        }

        _estadoAcceso.value = EstadoAcceso.Cargando

        viewModelScope.launch {

            try {

                val respuesta = repositorio.verificarCredenciales(
                    correoValidado,
                    contrasena
                )

                if (respuesta.isSuccessful) {

                    val informacion = respuesta.body()

                    if (informacion != null) {

                        guardarSesion(informacion)

                        _estadoAcceso.value =
                            EstadoAcceso.Exitoso(informacion)

                    } else {

                        _estadoAcceso.value = EstadoAcceso.Fallido(
                            "No se recibió una respuesta válida del servidor."
                        )
                    }

                } else {

                    val mensajeServidor = try {

                        val contenidoError =
                            respuesta.errorBody()?.string().orEmpty()

                        JSONObject(contenidoError)
                            .optString("mensaje")

                    } catch (error: Exception) {
                        ""
                    }

                    _estadoAcceso.value = EstadoAcceso.Fallido(
                        mensajeServidor.ifBlank {
                            "No es posible iniciar sesión. Inténtalo nuevamente."
                        }
                    )
                }

            } catch (error: IOException) {

                _estadoAcceso.value = EstadoAcceso.Fallido(
                    "No se pudo conectar con el servidor. Comprueba la conexión."
                )

            } catch (error: Exception) {

                _estadoAcceso.value = EstadoAcceso.Fallido(
                    "Ocurrió un inconveniente al procesar el inicio de sesión."
                )
            }
        }
    }


    private fun guardarSesion(
        informacion: RespuestaAcceso
    ) {

        val preferencias = getApplication<Application>()
            .getSharedPreferences(
                "NotasAppSesion",
                Context.MODE_PRIVATE
            )

        preferencias.edit()
            .putString("token", informacion.token)
            .putInt("usuario_id", informacion.usuario.id)
            .putString("nombre_usuario", informacion.usuario.nombre)
            .putString("correo_usuario", informacion.usuario.email)
            .apply()
    }
}