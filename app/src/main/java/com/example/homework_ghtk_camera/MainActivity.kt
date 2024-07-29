package com.example.homework_ghtk_camera

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import com.example.homework_ghtk_camera.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.internal.ImageUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor : ExecutorService
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var imageCaptured: ImageView
    private fun setUpCameraX(){
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        },ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        val preview: Preview = Preview.Builder()
            .build()

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, ObjectDetectorImageAnalyzer())
            }

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(binding.root.display.rotation)
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.surfaceProvider = binding.cameraPreview.getSurfaceProvider()
        if (cameraProvider != null) {
            camera = cameraProvider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                imageCapture,
                preview,
                imageAnalyzer
            )
        }
    }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted:Boolean ->
        if(isGranted){
            setUpCameraX()
        }else{
            Toast.makeText(this,"Tính năng cần được cho phép",Toast.LENGTH_LONG).show()
            finish()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }

    private fun doCapture() {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            File.createTempFile(
                "temp",
                ".jpg",
                getOutputDirectory()
            )
        ).build()

        imageCapture?.takePicture(outputOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    // insert your code here.
                    Toast.makeText(applicationContext, "Error capture", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // insert your code here.
                    val savedUri = outputFileResults.savedUri
                    savedUri?.let {
                        val bitmap = com.example.homework_ghtk_camera.ImageUtils.decodeBitmapFromUri(contentResolver, it)
                        // Use the bitmap as needed (e.g., display in ImageView)
                        runOnUiThread {
                            imageCaptured.setImageBitmap(bitmap)


                        }
                    }
                }
            })
    }
}

object ImageUtils {
    fun decodeBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
}