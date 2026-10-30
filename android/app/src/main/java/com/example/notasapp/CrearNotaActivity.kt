package com.example.notasapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

import androidx.activity.ComponentActivity
import androidx.activity.viewModels

import com.example.notasapp.viewmodel.CrearNotaViewModel
import com.example.notasapp.viewmodel.EstadoGuardarNota

import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CrearNotaActivity : ComponentActivity() {

    private val modeloNotas: CrearNotaViewModel by viewModels()

    private lateinit var campoTitulo: TextInputEditText
    private lateinit var campoContenido: TextInputEditText

    private lateinit var botonGuardar: MaterialButton
    private lateinit var botonRegresar: MaterialButton

    private lateinit var indicadorGuardado: ProgressBar
    private lateinit var textoResultado: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_crear_nota)

        inicializarFormulario()
        configurarEventos()
        observarEstadoGuardado()
    }


    // Asociar los componentes con el diseño XML
    private fun inicializarFormulario() {

        campoTitulo = findViewById(R.id.etTituloNota)
        campoContenido = findViewById(R.id.etContenidoNota)

        botonGuardar = findViewById(R.id.btnGuardarNota)
        botonRegresar = findViewById(R.id.btnVolverDashboard)

        indicadorGuardado = findViewById(R.id.progresoGuardar)
        textoResultado = findViewById(R.id.tvResultadoNota)
    }


    // Configurar las acciones del formulario
    private fun configurarEventos() {

        botonGuardar.setOnClickListener {

            val tituloIngresado = campoTitulo.text.toString()
            val contenidoIngresado = campoContenido.text.toString()

            modeloNotas.registrarNuevaNota(
                tituloIngresado,
                contenidoIngresado
            )
        }

        botonRegresar.setOnClickListener {
            finish()
        }
    }


    // Observar los resultados del ViewModel
    private fun observarEstadoGuardado() {

        modeloNotas.estadoGuardado.observe(this) { estado ->

            when (estado) {

                is EstadoGuardarNota.Inicial -> {

                    indicadorGuardado.visibility = View.GONE
                    textoResultado.visibility = View.GONE

                    botonGuardar.isEnabled = true
                    botonRegresar.isEnabled = true
                }

                is EstadoGuardarNota.Guardando -> {

                    indicadorGuardado.visibility = View.VISIBLE
                    textoResultado.visibility = View.GONE

                    botonGuardar.isEnabled = false
                    botonRegresar.isEnabled = false
                }

                is EstadoGuardarNota.Guardada -> {

                    indicadorGuardado.visibility = View.GONE

                    botonGuardar.isEnabled = true
                    botonRegresar.isEnabled = true

                    textoResultado.text = estado.mensaje
                    textoResultado.setTextColor(
                        android.graphics.Color.parseColor("#3F8A72")
                    )

                    textoResultado.visibility = View.VISIBLE

                    // Limpiar el formulario después del guardado exitoso
                    campoTitulo.text?.clear()
                    campoContenido.text?.clear()
                }

                is EstadoGuardarNota.Error -> {

                    indicadorGuardado.visibility = View.GONE

                    botonGuardar.isEnabled = true
                    botonRegresar.isEnabled = true

                    textoResultado.text = estado.mensaje
                    textoResultado.setTextColor(
                        android.graphics.Color.parseColor("#C15B68")
                    )

                    textoResultado.visibility = View.VISIBLE
                }
            }
        }
    }
}