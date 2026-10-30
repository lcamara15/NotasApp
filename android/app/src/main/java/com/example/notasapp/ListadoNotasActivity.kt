package com.example.notasapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.notasapp.adapter.NotasAdapter
import com.example.notasapp.viewmodel.EstadoListado
import com.example.notasapp.viewmodel.ListadoNotasViewModel

import com.google.android.material.button.MaterialButton

class ListadoNotasActivity : ComponentActivity() {

    private val modeloListado: ListadoNotasViewModel by viewModels()

    private lateinit var listaNotas: RecyclerView
    private lateinit var adaptadorNotas: NotasAdapter

    private lateinit var textoCantidad: TextView
    private lateinit var textoEstado: TextView
    private lateinit var indicadorCarga: ProgressBar
    private lateinit var botonRegresar: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listado_notas)

        inicializarComponentes()
        configurarListado()
        configurarNavegacion()
        observarResultados()

        modeloListado.cargarNotas()
    }


    // Recuperar los componentes del diseño XML
    private fun inicializarComponentes() {

        listaNotas = findViewById(R.id.recyclerNotas)

        textoCantidad = findViewById(R.id.tvCantidadNotas)
        textoEstado = findViewById(R.id.tvEstadoListado)

        indicadorCarga = findViewById(R.id.progresoListado)
        botonRegresar = findViewById(R.id.btnRegresarListado)
    }


    // Preparar el RecyclerView y su adaptador
    private fun configurarListado() {

        adaptadorNotas = NotasAdapter()

        listaNotas.layoutManager = LinearLayoutManager(this)
        listaNotas.adapter = adaptadorNotas

        listaNotas.setHasFixedSize(false)
    }


    // Regresar al Dashboard
    private fun configurarNavegacion() {

        botonRegresar.setOnClickListener {
            finish()
        }
    }


    // Observar las respuestas del ViewModel
    private fun observarResultados() {

        modeloListado.estadoListado.observe(this) { estado ->

            when (estado) {

                is EstadoListado.Inicial -> {

                    indicadorCarga.visibility = View.GONE
                    textoEstado.visibility = View.GONE
                }

                is EstadoListado.Consultando -> {

                    indicadorCarga.visibility = View.VISIBLE
                    textoEstado.visibility = View.GONE
                    listaNotas.visibility = View.GONE

                    textoCantidad.text = "Consultando tus notas..."
                }

                is EstadoListado.Completado -> {

                    indicadorCarga.visibility = View.GONE

                    val notasRecibidas = estado.notas

                    adaptadorNotas.actualizarNotas(notasRecibidas)

                    textoCantidad.text = when (notasRecibidas.size) {
                        1 -> "1 nota guardada"
                        else -> "${notasRecibidas.size} notas guardadas"
                    }

                    if (notasRecibidas.isEmpty()) {

                        listaNotas.visibility = View.GONE

                        textoEstado.text =
                            "Todavía no tienes notas guardadas. Comienza escribiendo tu primera idea."

                        textoEstado.setTextColor(
                            android.graphics.Color.parseColor("#7187A4")
                        )

                        textoEstado.visibility = View.VISIBLE

                    } else {

                        textoEstado.visibility = View.GONE
                        listaNotas.visibility = View.VISIBLE
                    }
                }

                is EstadoListado.Error -> {

                    indicadorCarga.visibility = View.GONE
                    listaNotas.visibility = View.GONE

                    textoCantidad.text = "Mis notas"

                    textoEstado.text = estado.mensaje

                    textoEstado.setTextColor(
                        android.graphics.Color.parseColor("#C15B68")
                    )

                    textoEstado.visibility = View.VISIBLE
                }
            }
        }
    }
}