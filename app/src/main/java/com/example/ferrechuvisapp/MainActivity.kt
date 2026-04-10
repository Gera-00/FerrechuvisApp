package com.example.ferrechuvisapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.ferrechuvisapp.data.local.dao.CategoriaDao
import com.example.ferrechuvisapp.data.local.dao.ProductoDao
import com.example.ferrechuvisapp.data.local.database.AppDatabase
import com.example.ferrechuvisapp.data.local.entity.Categoria
import com.example.ferrechuvisapp.ui.producto.ProductoAdapter
import kotlinx.coroutines.launch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ferrechuvisapp.ui.producto.ProductoFormActivity

class MainActivity : ComponentActivity() {
    lateinit var db: AppDatabase
    lateinit var categoriaDao: CategoriaDao
    lateinit var productoDao: ProductoDao
    lateinit var adapter: ProductoAdapter

    lateinit var etBuscar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)
        categoriaDao = db.categoriaDao()
        productoDao = db.productoDao()

        adapter = ProductoAdapter(emptyList()) { producto ->
            val intent = Intent(this, ProductoFormActivity::class.java)
            intent.putExtra(ProductoFormActivity.EXTRA_PRODUCT_ID, producto.id)
            startActivity(intent)
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerProductos)
        recycler.layoutManager = GridLayoutManager(this,2)
        recycler.adapter = adapter

        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_agregar_producto -> {
                        val intent = Intent(this, ProductoFormActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
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
}


