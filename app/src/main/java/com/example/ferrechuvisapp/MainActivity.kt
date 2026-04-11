package com.example.ferrechuvisapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ferrechuvisapp.data.local.entity.Producto
import com.example.ferrechuvisapp.data.local.dao.ProductoDao
import com.example.ferrechuvisapp.data.local.database.AppDatabase
import com.example.ferrechuvisapp.ui.categoria.CategoriaActivity
import com.example.ferrechuvisapp.ui.producto.ProductoAdapter
import com.example.ferrechuvisapp.ui.producto.ProductoDetalleActivity
import com.example.ferrechuvisapp.ui.producto.ProductoFormActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    lateinit var db: AppDatabase
    lateinit var productoDao: ProductoDao
    lateinit var adapter: ProductoAdapter

    lateinit var etBuscar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)
        productoDao = db.productoDao()

        adapter = ProductoAdapter(
            emptyList(),
            onItemClick = { producto ->
                val intent = Intent(this, ProductoDetalleActivity::class.java).apply {
                    putExtra(ProductoDetalleActivity.EXTRA_NOMBRE, producto.nombre)
                    putExtra(ProductoDetalleActivity.EXTRA_PRECIO, producto.precio)
                    putExtra(ProductoDetalleActivity.EXTRA_IMAGEN_PATH, producto.imagenPath)
                }
                startActivity(intent)
            },
            onItemLongClick = { producto ->
                mostrarAccionesProducto(producto)
            }
        )

        val recycler = findViewById<RecyclerView>(R.id.recyclerProductos)
        recycler.layoutManager = GridLayoutManager(this,2)
        recycler.adapter = adapter

        findViewById<ImageButton>(R.id.fabAgregarProducto)
            .setOnClickListener {
                val intent = Intent(this, ProductoFormActivity::class.java)
                startActivity(intent)
            }

        findViewById<ImageButton>(R.id.btnMenu)
            .setOnClickListener { btnMenu ->
                val popupMenu = PopupMenu(this, btnMenu)
                popupMenu.menu.add("Categorías")
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.title == "Categorías") {
                        val intent = Intent(this, CategoriaActivity::class.java)
                        startActivity(intent)
                        true
                    } else {
                        false
                    }
                }
                popupMenu.show()
            }

        etBuscar = findViewById(R.id.etBuscar)

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                val texto = s.toString()

                lifecycleScope.launch {
                    val resultados = withContext(Dispatchers.IO) {
                        if (texto.isEmpty()) {
                            productoDao.getAll()
                        } else {
                            productoDao.buscar(texto)
                        }
                    }

                    adapter.actualizarLista(resultados)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            val productos = withContext(Dispatchers.IO) {
                productoDao.getAll()
            }
            adapter.actualizarLista(productos)
        }
    }

    private fun mostrarAccionesProducto(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Acciones del producto")
            .setMessage("Selecciona una acción para ${producto.nombre}")
            .setPositiveButton("Editar") { _, _ ->
                val intent = Intent(this, ProductoFormActivity::class.java)
                intent.putExtra(ProductoFormActivity.EXTRA_PRODUCT_ID, producto.id)
                startActivity(intent)
            }
            .setNegativeButton("Eliminar") { _, _ ->
                confirmarEliminacion(producto)
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminacion(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Deseas eliminar este producto? Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        productoDao.delete(producto)
                        eliminarArchivoLocalSiAplica(producto.imagenPath)
                    }

                    val textoBusqueda = etBuscar.text?.toString().orEmpty()
                    val productos = withContext(Dispatchers.IO) {
                        if (textoBusqueda.isBlank()) productoDao.getAll() else productoDao.buscar(textoBusqueda)
                    }
                    adapter.actualizarLista(productos)
                    Toast.makeText(this@MainActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun eliminarArchivoLocalSiAplica(ruta: String?) {
        if (ruta.isNullOrBlank() || ruta.startsWith("content://")) {
            return
        }

        try {
            val archivo = File(ruta)
            if (archivo.exists()) {
                archivo.delete()
            }
        } catch (_: Exception) {
        }
    }
}


