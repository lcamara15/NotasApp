package com.example.notasapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView

import androidx.activity.ComponentActivity

import com.google.android.material.button.MaterialButton


class DashboardActivity : ComponentActivity() {

    private lateinit var textoBienvenida: TextView

    private lateinit var botonCrearNota: MaterialButton
    private lateinit var botonVerNotas: MaterialButton
    private lateinit var botonCerrarSesion: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        inicializarComponentes()

        if (verificarSesion()) {
            configurarNavegacion()
        }
    }


    // Relacionar los elementos visuales con sus identificadores
    private fun inicializarComponentes() {

        textoBienvenida = findViewById(R.id.tvBienvenida)

        botonCrearNota = findViewById(R.id.btnCrearNota)

        botonVerNotas = findViewById(R.id.btnConsultarNotas)

        botonCerrarSesion = findViewById(R.id.btnCerrarSesion)
    }


    // Comprobar la sesión y personalizar un saludo
    private fun verificarSesion(): Boolean {

        val preferencias = getSharedPreferences(
            "NotasAppSesion",
            Context.MODE_PRIVATE
        )

        val tokenActual = preferencias.getString(
            "token",
            null
        )

        if (tokenActual.isNullOrBlank()) {
            regresarAlLogin()
            return false
        }

        val nombreGuardado = preferencias.getString(
            "nombre_usuario",
            null
        )

        val nombreVisible = nombreGuardado
            ?.takeIf { it.isNotBlank() }
            ?: "de nuevo"

        textoBienvenida.text = "¡Hola, $nombreVisible!"

        return true
    }


    // Configurar los accesos del dashboard
    private fun configurarNavegacion() {

        botonCrearNota.setOnClickListener {

            val pantallaEditor = Intent(
                this,
                CrearNotaActivity::class.java
            )

            startActivity(pantallaEditor)
        }


        botonVerNotas.setOnClickListener {

            val pantallaListado = Intent(
                this,
                ListadoNotasActivity::class.java
            )

            startActivity(pantallaListado)
        }


        botonCerrarSesion.setOnClickListener {
            finalizarSesion()
        }
    }


    // Eliminar la información local de autenticación
    private fun finalizarSesion() {

        val preferencias = getSharedPreferences(
            "NotasAppSesion",
            Context.MODE_PRIVATE
        )

        preferencias.edit()
            .clear()
            .apply()

        regresarAlLogin()
    }


    // Regresar al login sin conservar pantallas anteriores
    private fun regresarAlLogin() {

        val pantallaAcceso = Intent(
            this,
            MainActivity::class.java
        ).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(pantallaAcceso)

        finish()
    }
}