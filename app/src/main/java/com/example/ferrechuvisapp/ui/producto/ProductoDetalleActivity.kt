package com.example.ferrechuvisapp.ui.producto

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.example.ferrechuvisapp.R
import java.io.File

class ProductoDetalleActivity : ComponentActivity() {

    companion object {
        const val EXTRA_NOMBRE = "extra_nombre"
        const val EXTRA_PRECIO = "extra_precio"
        const val EXTRA_IMAGEN_PATH = "extra_imagen_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_detalle)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val imgProducto = findViewById<ImageView>(R.id.imgDetalleProducto)
        val tvNombre = findViewById<TextView>(R.id.tvDetalleNombre)
        val tvPrecio = findViewById<TextView>(R.id.tvDetallePrecio)

        btnBack.setOnClickListener { finish() }

        val nombre = intent.getStringExtra(EXTRA_NOMBRE).orEmpty()
        val precio = intent.getDoubleExtra(EXTRA_PRECIO, 0.0)
        val imagenPath = intent.getStringExtra(EXTRA_IMAGEN_PATH)

        tvNombre.text = if (nombre.isBlank()) "Nombre" else nombre
        tvPrecio.text = String.format("$%.2f", precio)

        if (!imagenPath.isNullOrBlank()) {
            val modeloImagen = if (imagenPath.startsWith("content://")) {
                Uri.parse(imagenPath)
            } else {
                File(imagenPath)
            }
            Glide.with(this)
                .load(modeloImagen)
                .into(imgProducto)
        } else {
            imgProducto.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }
}
