package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class ImageClassifierHelper(private val context: Context) {

    companion object {
        private const val TAG = "ImageClassifierHelper"
        private const val MODEL_FILE_NAME = "cancer_classification.tflite"
        private const val threshold = 0.5f
        private const val maxResults = 3
    }

    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                MODEL_FILE_NAME,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error creating TensorFlow Lite Image Classifier: ${e.message}")
        }
    }

    fun classifyStaticImage(imageUri: Uri): ClassificationResult? {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
        val tensorImage = TensorImage.fromBitmap(bitmap)

        val results = imageClassifier?.classify(tensorImage)
        val bestResult = results?.firstOrNull()

        return bestResult?.let {
            ClassificationResult(it.categories.first().label, it.categories.first().score)
        }
    }

    data class ClassificationResult(val label: String, val confidence: Float)
}
