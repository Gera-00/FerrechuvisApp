package com.example.ferrechuvisapp.ui.producto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ferrechuvisapp.R
import com.example.ferrechuvisapp.data.local.entity.Producto
import com.bumptech.glide.Glide
import java.io.File

class ProductoAdapter(
    private var lista: List<Producto>
) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        val imagen: ImageView = view.findViewById(R.id.imgProducto)
        val nombre: TextView = view.findViewById(R.id.tvNombre)
        val codigo: TextView = view.findViewById(R.id.tvCodigo)
        val precio: TextView = view.findViewById(R.id.tvPrecio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = lista[position]
        holder.nombre.text = producto.nombre
        holder.codigo.text = producto.codigo
        holder.precio.text = String.format("$%.2f", producto.precio)

        if (producto.imagenPath != null) {
            /*Glide.with(holder.itemView.context)
                .load(File(producto.imagenPath))
                .centerCrop()
                .into(holder.imagen)*/
            Glide.with(holder.itemView.context)
                .load(File(producto.imagenPath))
                .into(holder.imagen)
        } else {
            holder.imagen.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    fun actualizarLista(nuevaLista: List<Producto>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}