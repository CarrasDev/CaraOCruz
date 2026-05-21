package com.example.caraocruz.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.caraocruz.R
import com.example.caraocruz.data.api.RankingItem

class RankingAdapter(private val items: List<RankingItem>) :
    RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPosicion: TextView = view.findViewById(R.id.tvPosicion)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuario)
        val tvPremio: TextView = view.findViewById(R.id.tvPremio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvPosicion.text = (position + 1).toString()
        holder.tvNombre.text = item.nombreUsuario
        holder.tvPremio.text = item.premio.toString()
    }

    override fun getItemCount() = items.size
}
