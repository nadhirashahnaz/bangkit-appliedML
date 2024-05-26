package com.dicoding.asclepius.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                currentImageUri = uri
                showImage(uri)
            }
        } else {
            showToast("Failed to pick an image")
        }
    }

    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener {
            showToast("Opening Gallery")
            startGallery()
        }

        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let { uri ->
                analyzeImage(uri)
            } ?: showToast("Please select an image first")
        }
    }

    private fun startGallery() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGallery()
            } else {
                showToast("Permission Denied")
            }
        }
    }

    private fun showImage(imageUri: Uri) {
        binding.previewImageView.setImageURI(imageUri)
    }

    private fun analyzeImage(imageUri: Uri) {
        try {
            val imageClassifierHelper = ImageClassifierHelper(this)
            val result = imageClassifierHelper.classifyStaticImage(imageUri)

            if (result != null) {
                moveToResult(result.label, result.confidence, imageUri)
            } else {
                showToast("No results found")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error analyzing image", e)
            showToast("Error analyzing image")
        }
    }

    private fun moveToResult(label: String, confidence: Float, imageUri: Uri) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("label", label)
            putExtra("confidence", confidence)
            putExtra("imageUri", imageUri.toString())
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
