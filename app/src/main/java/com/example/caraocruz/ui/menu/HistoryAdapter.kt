package com.example.caraocruz.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.caraocruz.R
import com.example.caraocruz.data.Partida
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : ListAdapter<Partida, HistoryAdapter.PartidaViewHolder>(PartidaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partida, parent, false)
        return PartidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartidaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PartidaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvApuesta: TextView = itemView.findViewById(R.id.tvApuesta)
        private val tvResultado: TextView = itemView.findViewById(R.id.tvResultado)
        private val tvGanancia: TextView = itemView.findViewById(R.id.tvGanancia)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        private val tvCoordenadas: TextView = itemView.findViewById(R.id.tvCoordenadas)

        // TODO Corregir harcodeo para la siguiente versión
        fun bind(partida: Partida) {
            tvApuesta.text = "Apuesta: ${partida.apuesta} monedas"
            tvResultado.text = "Resultado: ${partida.resultado}"
            
            val ganancia = if (partida.gano) {
                "+${partida.apuesta * 2}"
            } else {
                "-${partida.apuesta}"
            }
            tvGanancia.text = ganancia
            tvGanancia.setTextColor(
                if (partida.gano) 
                    itemView.context.getColor(android.R.color.holo_green_dark)
                else 
                    itemView.context.getColor(android.R.color.holo_red_dark)
            )

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = dateFormat.format(partida.fecha)

            if (partida.latitud != null && partida.longitud != null) {
                tvCoordenadas.visibility = View.VISIBLE
                tvCoordenadas.text = "Ubicación: ${partida.latitud}, ${partida.longitud}"
            } else {
                tvCoordenadas.visibility = View.GONE
            }
        }
    }
}

class PartidaDiffCallback : DiffUtil.ItemCallback<Partida>() {
    override fun areItemsTheSame(oldItem: Partida, newItem: Partida): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Partida, newItem: Partida): Boolean {
        return oldItem == newItem
    }
}
