package com.example.ferrechuvisapp.ui.barcode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.ferrechuvisapp.R

class BarcodeScannerActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var analyzer: MlKitBarcodeAnalyzer
    private var hasDeliveredResult = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Se requiere permiso de camara", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        previewView = findViewById(R.id.previewViewBarcode)
        findViewById<ImageButton>(R.id.btnCloseScanner).setOnClickListener { finish() }

        analyzer = MlKitBarcodeAnalyzer { code ->
            deliverScanResult(code)
        }

        if (hasCameraPermission()) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(mainExecutor, analyzer)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (_: Exception) {
                Toast.makeText(this, "No se pudo iniciar la camara", Toast.LENGTH_SHORT).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun deliverScanResult(value: String) {
        if (hasDeliveredResult) return
        hasDeliveredResult = true

        val intent = Intent().apply {
            putExtra(EXTRA_BARCODE_VALUE, value)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onDestroy() {
        analyzer.close()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_BARCODE_VALUE = "extra_barcode_value"
    }
}
