package com.example.notasapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.example.notasapp.R
import com.example.notasapp.data.model.NotaPersonal

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class NotasAdapter(
    private var notasRegistradas: List<NotaPersonal> = emptyList()
) : RecyclerView.Adapter<NotasAdapter.NotaViewHolder>() {


    class NotaViewHolder(vista: View) :
        RecyclerView.ViewHolder(vista) {

        val titulo: TextView =
            vista.findViewById(R.id.tvTituloItem)

        val contenido: TextView =
            vista.findViewById(R.id.tvContenidoItem)

        val fecha: TextView =
            vista.findViewById(R.id.tvFechaItem)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotaViewHolder {

        val vistaNota = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_nota,
                parent,
                false
            )

        return NotaViewHolder(vistaNota)
    }


    override fun onBindViewHolder(
        holder: NotaViewHolder,
        position: Int
    ) {

        val notaActual = notasRegistradas[position]

        holder.titulo.text = notaActual.titulo
        holder.contenido.text = notaActual.contenido

        holder.fecha.text = convertirFecha(
            notaActual.fecha_creacion
        )
    }


    override fun getItemCount(): Int {
        return notasRegistradas.size
    }


    // Actualizar el contenido del RecyclerView
    fun actualizarNotas(
        nuevasNotas: List<NotaPersonal>
    ) {

        notasRegistradas = nuevasNotas
        notifyDataSetChanged()
    }


    // Presentar la fecha recibida desde SQLite
    private fun convertirFecha(
        fechaOriginal: String
    ): String {

        return try {

            val formatoEntrada = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.US
            )

            formatoEntrada.timeZone = TimeZone.getTimeZone("UTC")

            val formatoSalida = SimpleDateFormat(
                "dd/MM/yyyy · hh:mm a",
                Locale.getDefault()
            )

            val fechaInterpretada =
                formatoEntrada.parse(fechaOriginal)

            if (fechaInterpretada != null) {
                formatoSalida.format(fechaInterpretada)
            } else {
                fechaOriginal
            }

        } catch (excepcion: Exception) {
            fechaOriginal
        }
    }
}