

package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val label = intent.getStringExtra("label") ?: "Unknown"
        val confidence = intent.getFloatExtra("confidence", 0.0f)

        val imageUri = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }
        imageUri?.let {
            Glide.with(this)
                .load(it)
                .into(binding.resultImage)
        }

        displayResult(label, confidence)
    }

    private fun displayResult(label: String, confidence: Float) {
        val confidencePercentage = (confidence * 100).toInt()
        val resultText = "Prediction: $label"
        val confidenceText = "Confidence: $confidencePercentage%"
        binding.resultText.text = "$resultText\n$confidenceText"
    }
}

