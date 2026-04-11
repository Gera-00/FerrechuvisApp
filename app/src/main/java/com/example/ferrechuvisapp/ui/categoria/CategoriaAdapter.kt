package com.example.ferrechuvisapp.ui.categoria

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ferrechuvisapp.R
import com.example.ferrechuvisapp.data.local.entity.Categoria

class CategoriaAdapter(
    private var categorias: List<Categoria>,
    private val onItemLongClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    inner class CategoriaViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvCategoriaItem)

        fun bind(categoria: Categoria) {
            tvNombre.text = categoria.nombre
            itemView.setOnLongClickListener {
                onItemLongClick(categoria)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.bind(categorias[position])
    }

    override fun getItemCount() = categorias.size

    fun actualizarLista(nuevaLista: List<Categoria>) {
        categorias = nuevaLista
        notifyDataSetChanged()
    }
}
