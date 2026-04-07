package com.example.ferrechuvisapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ferrechuvisapp.data.local.dao.ProductoDao
import com.example.ferrechuvisapp.data.local.database.AppDatabase
import com.example.ferrechuvisapp.data.local.entity.Producto
import com.example.ferrechuvisapp.ui.producto.ProductoAdapter
import kotlinx.coroutines.launch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.TextWatcher

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

        adapter = ProductoAdapter(emptyList())

        val recycler = findViewById<RecyclerView>(R.id.recyclerProductos)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        //Este insert fue de prueba para insertar una tupla en la tabla Productos y probar layout
        /*lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                productoDao.insert(
                    Producto(
                        nombre = "Martillo",
                        codigo = "MT001",
                        precio = 120.0,
                        categoriaId = 1,
                        imagenPath = null
                    )
                )
            }
        }*/

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

        findViewById<Button>(R.id.btnBuscar).setOnClickListener {
            val texto = etBuscar.text.toString()

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

    }
}


