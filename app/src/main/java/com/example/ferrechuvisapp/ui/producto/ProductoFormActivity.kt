package com.example.ferrechuvisapp.ui.producto

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.ferrechuvisapp.R
import com.example.ferrechuvisapp.data.local.dao.CategoriaDao
import com.example.ferrechuvisapp.data.local.database.AppDatabase
import com.example.ferrechuvisapp.data.local.entity.Categoria
import com.example.ferrechuvisapp.data.local.entity.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProductoFormActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    private lateinit var imgPreview: ImageView
    private var fotoUri: Uri? = null
    private var fotoFile: File? = null
    private var imagenPathGuardada: String? = null
    private var productoIdEnEdicion: Int? = null
    private var productoEnEdicion: Producto? = null
    private var categoriaIdActual: Int = 0
    private lateinit var categoriaDao: CategoriaDao
    private lateinit var spCategoria: Spinner
    private var categoriasDisponibles: List<Categoria> = emptyList()

    // 1. Lanzador para tomar la foto
    private val tomarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            fotoUri?.let {
                imgPreview.setImageURI(it)
                imagenPathGuardada = fotoFile?.absolutePath
            }
        }
    }

    // 2. Lanzador para pedir el permiso de cámara
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            prepararYTomarFoto()
        } else {
            Toast.makeText(this, "Permiso denegado. No puedes tomar fotos.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_form)

        val db = AppDatabase.getDatabase(this)
        categoriaDao = db.categoriaDao()
        val productoDao = db.productoDao()

        imgPreview = findViewById(R.id.imgPreview)
        spCategoria = findViewById(R.id.spCategoria)
        val btnImagen = findViewById<Button>(R.id.btnImagen)
        val btnCamara = findViewById<Button>(R.id.btnCamara)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvFormTitle = findViewById<TextView>(R.id.tvFormTitle)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etPrecio = findViewById<EditText>(R.id.etPrecio)

        val idRecibido = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (idRecibido != -1) {
            productoIdEnEdicion = idRecibido
            btnGuardar.text = "Actualizar producto"
            tvFormTitle.text = "Editar Producto"
        }

        btnBack.setOnClickListener { finish() }

        spCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position == 0) {
                    categoriaIdActual = 0
                    return
                }

                categoriasDisponibles.getOrNull(position - 1)?.let { categoriaSeleccionada ->
                    categoriaIdActual = categoriaSeleccionada.id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        lifecycleScope.launch {
            asegurarCategoriasDePruebaSiHaceFalta()
            categoriasDisponibles = withContext(Dispatchers.IO) {
                categoriaDao.getAll()
            }

            configurarSpinnerCategorias()

            if (idRecibido != -1) {
                val producto = withContext(Dispatchers.IO) {
                    productoDao.getById(idRecibido)
                }

                producto?.let {
                    productoEnEdicion = it
                    etNombre.setText(it.nombre)
                    etCodigo.setText(it.codigo)
                    etPrecio.setText(it.precio.toString())
                    categoriaIdActual = it.categoriaId
                    imagenPathGuardada = it.imagenPath
                    seleccionarCategoriaPorId(it.categoriaId)

                    it.imagenPath?.let { ruta ->
                        val modeloImagen = if (ruta.startsWith("content://")) {
                            Uri.parse(ruta)
                        } else {
                            File(ruta)
                        }

                        Glide.with(this@ProductoFormActivity)
                            .load(modeloImagen)
                            .into(imgPreview)
                    }
                }
            }
        }

        // Seleccionar de galería
        val seleccionarImagen = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val archivoInterno = copiarImagenAGuardadoInterno(it)
                if (archivoInterno != null) {
                    imagenPathGuardada = archivoInterno.absolutePath
                    imgPreview.setImageURI(Uri.fromFile(archivoInterno))
                } else {
                    Toast.makeText(this, "No se pudo guardar la imagen seleccionada", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnImagen.setOnClickListener { seleccionarImagen.launch("image/*") }

        // 3. Lógica del botón Cámara con validación de permiso
        btnCamara.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                prepararYTomarFoto()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val codigo = etCodigo.text.toString()
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0

            if (categoriaIdActual == 0) {
                Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rutaFinal = imagenPathGuardada

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val productoAGuardar = Producto(
                        id = productoIdEnEdicion ?: 0,
                        nombre = nombre,
                        codigo = codigo,
                        precio = precio,
                        categoriaId = categoriaIdActual,
                        imagenPath = rutaFinal
                    )

                    if (productoIdEnEdicion == null) {
                        productoDao.insert(productoAGuardar)
                    } else {
                        val rutaAnterior = productoEnEdicion?.imagenPath
                        productoDao.update(productoAGuardar)

                        if (rutaAnterior != rutaFinal) {
                            eliminarArchivoLocalSiAplica(rutaAnterior)
                        }
                    }
                }
                finish()
            }
        }
    }

    private fun prepararYTomarFoto() {
        fotoFile = crearArchivoImagen()
        fotoFile?.let { file ->
            fotoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
            tomarFotoLauncher.launch(fotoUri!!)
        }
    }

    private fun crearArchivoImagen(): File {
        val nombre = "foto_${System.currentTimeMillis()}.jpg"
        return File(filesDir, nombre)
    }

    private suspend fun asegurarCategoriasDePruebaSiHaceFalta() {
        withContext(Dispatchers.IO) {
            if (categoriaDao.getAll().isEmpty()) {
                categoriaDao.insert(Categoria(nombre = "Herramientas"))
                categoriaDao.insert(Categoria(nombre = "Electricidad"))
                categoriaDao.insert(Categoria(nombre = "Pinturas"))
                categoriaDao.insert(Categoria(nombre = "Plomeria"))
                categoriaDao.insert(Categoria(nombre = "Ferreteria general"))
            }
        }
    }

    private fun configurarSpinnerCategorias() {
        val nombresCategorias = buildList {
            add("Selecciona una categoría")
            addAll(categoriasDisponibles.map { it.nombre })
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombresCategorias
        ).also { spinnerAdapter ->
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spCategoria.adapter = adapter

        if (productoIdEnEdicion == null) {
            spCategoria.setSelection(0)
            categoriaIdActual = 0
        } else {
            seleccionarCategoriaPorId(categoriaIdActual)
        }
    }

    private fun seleccionarCategoriaPorId(categoriaId: Int) {
        val indice = categoriasDisponibles.indexOfFirst { it.id == categoriaId }
        if (indice >= 0 && spCategoria.adapter != null) {
            spCategoria.setSelection(indice + 1)
            categoriaIdActual = categoriasDisponibles[indice].id
        }
    }

    private fun copiarImagenAGuardadoInterno(uriOrigen: Uri): File? {
        return try {
            val archivoDestino = File(filesDir, "img_${System.currentTimeMillis()}.jpg")

            contentResolver.openInputStream(uriOrigen)?.use { input ->
                FileOutputStream(archivoDestino).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            archivoDestino
        } catch (_: Exception) {
            null
        }
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