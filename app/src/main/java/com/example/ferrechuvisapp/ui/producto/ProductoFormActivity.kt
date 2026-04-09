package com.example.ferrechuvisapp.ui.producto

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.ferrechuvisapp.R
import com.example.ferrechuvisapp.data.local.database.AppDatabase
import com.example.ferrechuvisapp.data.local.entity.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProductoFormActivity : ComponentActivity() {

    private lateinit var imgPreview: ImageView
    private var fotoUri: Uri? = null
    private var fotoFile: File? = null
    private var imagenUri: Uri? = null

    // 1. Lanzador para tomar la foto
    private val tomarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            fotoUri?.let {
                imgPreview.setImageURI(it)
                imagenUri = it
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
        val productoDao = db.productoDao()

        imgPreview = findViewById(R.id.imgPreview)
        val btnImagen = findViewById<Button>(R.id.btnImagen)
        val btnCamara = findViewById<Button>(R.id.btnCamara)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etPrecio = findViewById<EditText>(R.id.etPrecio)

        // Seleccionar de galería
        val seleccionarImagen = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imagenUri = it
                imgPreview.setImageURI(it)
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

            // Usamos la ruta del archivo si existe, sino la del URI de galería
            val rutaFinal = fotoFile?.absolutePath ?: imagenUri?.toString()

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    productoDao.insert(
                        Producto(
                            nombre = nombre,
                            codigo = codigo,
                            precio = precio,
                            categoriaId = 1,
                            imagenPath = rutaFinal
                        )
                    )
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
}