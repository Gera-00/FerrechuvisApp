package com.example.ferrechuvisapp.ui.categoria

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ferrechuvisapp.R
import com.example.ferrechuvisapp.data.local.dao.CategoriaDao
import com.example.ferrechuvisapp.data.local.database.AppDatabase
import com.example.ferrechuvisapp.data.local.entity.Categoria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriaActivity : ComponentActivity() {
    lateinit var db: AppDatabase
    lateinit var categoriaDao: CategoriaDao
    lateinit var adapter: CategoriaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categoria)

        db = AppDatabase.getDatabase(this)
        categoriaDao = db.categoriaDao()

        adapter = CategoriaAdapter(
            emptyList(),
            onItemLongClick = { categoria ->
                mostrarAccionesCategoria(categoria)
            }
        )

        val recycler = findViewById<RecyclerView>(R.id.recyclerCategorias)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val etNuevaCategoria = findViewById<EditText>(R.id.etNuevaCategoria)
        val btnAgregarCategoria = findViewById<Button>(R.id.btnAgregarCategoria)

        btnAgregarCategoria.setOnClickListener {
            val nombre = etNuevaCategoria.text?.toString()?.trim()

            if (nombre.isNullOrEmpty()) {
                Toast.makeText(this, "Ingresa un nombre para la categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    categoriaDao.insert(Categoria(nombre = nombre))
                }

                etNuevaCategoria.text.clear()
                cargarCategorias()
                Toast.makeText(this@CategoriaActivity, "Categoría agregada", Toast.LENGTH_SHORT).show()
            }
        }

        cargarCategorias()
    }

    private fun cargarCategorias() {
        lifecycleScope.launch {
            val categorias = withContext(Dispatchers.IO) {
                categoriaDao.getAll()
            }
            adapter.actualizarLista(categorias)
        }
    }

    private fun mostrarAccionesCategoria(categoria: Categoria) {
        AlertDialog.Builder(this)
            .setTitle("Acciones de categoría")
            .setMessage("Selecciona una acción para ${categoria.nombre}")
            .setPositiveButton("Editar") { _, _ ->
                mostrarDialogoEditar(categoria)
            }
            .setNegativeButton("Eliminar") { _, _ ->
                confirmarEliminacion(categoria)
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(categoria: Categoria) {
        val editText = EditText(this).apply {
            setText(categoria.nombre)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar categoría")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString().trim()

                if (nuevoNombre.isNotEmpty()) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            categoriaDao.update(categoria.copy(nombre = nuevoNombre))
                        }
                        cargarCategorias()
                        Toast.makeText(this@CategoriaActivity, "Categoría actualizada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminacion(categoria: Categoria) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar categoría")
            .setMessage("¿Deseas eliminar la categoría '${categoria.nombre}'?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        categoriaDao.delete(categoria)
                    }
                    cargarCategorias()
                    Toast.makeText(this@CategoriaActivity, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}
