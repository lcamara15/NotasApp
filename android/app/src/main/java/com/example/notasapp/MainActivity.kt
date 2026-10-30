package com.example.notasapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.viewModels

import com.example.notasapp.viewmodel.EstadoAcceso
import com.example.notasapp.viewmodel.LoginViewModel

import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : ComponentActivity() {

    private val modeloAutenticacion: LoginViewModel by viewModels()

    private lateinit var entradaCorreo: TextInputEditText
    private lateinit var entradaContrasena: TextInputEditText

    private lateinit var botonIniciarSesion: MaterialButton
    private lateinit var mensajeEstado: TextView
    private lateinit var indicadorCarga: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        inicializarFormulario()
        configurarInicioSesion()
        observarResultadoLogin()
    }


    // Recuperar los componentes de la interfaz
    private fun inicializarFormulario() {

        entradaCorreo = findViewById(R.id.etCorreo)

        entradaContrasena = findViewById(R.id.etContrasena)

        botonIniciarSesion = findViewById(R.id.btnIniciarSesion)

        mensajeEstado = findViewById(R.id.tvMensaje)

        indicadorCarga = findViewById(R.id.progresoLogin)
    }


    // Enviar credenciales al ViewModel
    private fun configurarInicioSesion() {

        botonIniciarSesion.setOnClickListener {

            val correoEscrito = entradaCorreo.text.toString()

            val contrasenaEscrita = entradaContrasena.text.toString()

            modeloAutenticacion.autenticarUsuario(
                correoEscrito,
                contrasenaEscrita
            )
        }
    }


    // Observar los estados de autenticación
    private fun observarResultadoLogin() {

        modeloAutenticacion.estadoAcceso.observe(this) { estado ->

            when (estado) {

                is EstadoAcceso.Inicial -> {

                    indicadorCarga.visibility = View.GONE
                    mensajeEstado.visibility = View.GONE

                    botonIniciarSesion.isEnabled = true
                }

                is EstadoAcceso.Cargando -> {

                    indicadorCarga.visibility = View.VISIBLE
                    mensajeEstado.visibility = View.GONE

                    botonIniciarSesion.isEnabled = false
                }

                is EstadoAcceso.Exitoso -> {

                    indicadorCarga.visibility = View.GONE
                    botonIniciarSesion.isEnabled = true

                    Toast.makeText(
                        this,
                        "¡Hola, ${estado.informacion.usuario.nombre}! Qué gusto verte de nuevo.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val abrirDashboard = Intent(
                        this,
                        DashboardActivity::class.java
                    )

                    startActivity(abrirDashboard)
                    finish()
                }

                is EstadoAcceso.Fallido -> {

                    indicadorCarga.visibility = View.GONE
                    botonIniciarSesion.isEnabled = true

                    mensajeEstado.text = estado.mensaje
                    mensajeEstado.visibility = View.VISIBLE
                }
            }
        }
    }
}