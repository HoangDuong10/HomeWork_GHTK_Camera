package com.example.homework_ghtk_camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ObjectDetectorImageAnalyzer : ImageAnalysis.Analyzer {
    private val mTextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            mTextRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Xử lý văn bản nhận diện được
                    val text = visionText.text
                    Log.d("TextRecognition", "Detected text: $text")

                    // Cập nhật UI hoặc xử lý văn bản theo nhu cầu
                    // Ví dụ: gửi kết quả về MainActivity để hiển thị
//                    binding.text12.text = text
                }
                .addOnFailureListener { e ->
                    Log.e("TextRecognition", "Text recognition failed", e)
                }
                .addOnCompleteListener {
                    // Đảm bảo đóng ImageProxy
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}