package com.example.ferrechuvisapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ferrechuvisapp.data.local.dao.ProductoDao
import com.example.ferrechuvisapp.data.local.database.AppDatabase
import com.example.ferrechuvisapp.ui.producto.ProductoAdapter
import com.example.ferrechuvisapp.ui.producto.ProductoDetalleActivity
import com.example.ferrechuvisapp.ui.producto.ProductoFormActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        adapter = ProductoAdapter(emptyList()) { producto ->
            val intent = Intent(this, ProductoDetalleActivity::class.java).apply {
                putExtra(ProductoDetalleActivity.EXTRA_NOMBRE, producto.nombre)
                putExtra(ProductoDetalleActivity.EXTRA_PRECIO, producto.precio)
                putExtra(ProductoDetalleActivity.EXTRA_IMAGEN_PATH, producto.imagenPath)
            }
            startActivity(intent)
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerProductos)
        recycler.layoutManager = GridLayoutManager(this,2)
        recycler.adapter = adapter

        findViewById<ImageButton>(R.id.fabAgregarProducto)
            .setOnClickListener {
                val intent = Intent(this, ProductoFormActivity::class.java)
                startActivity(intent)
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
}


