package com.example.funding

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.funding.databinding.ActivityProfileBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val PICK_IMAGE_REQUEST = 1
    private val IMAGE_FILE_NAME = "profile_image.png"
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Load the saved profile image if it exists
        loadProfileImage()

        binding.imageCartoon.setOnClickListener {
            // Open the image picker when the user clicks the image
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.signOutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.imageBack.setOnClickListener {
            // Redirect to MainPage
            val intent = Intent(this, Mainpage::class.java)
            startActivity(intent)
        }

        binding.aboutUsSection.setOnClickListener {
            // Open About Us Activity
            val intent = Intent(this, AboutUsActivity::class.java)
            startActivity(intent)
        }
        val settingTextView: TextView = findViewById(R.id.SettingTextview)
        settingTextView.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Clear session and redirect to Login Page
                sessionManager.clearLoginState()
                val intent = Intent(this, LoginActivity::class.java) // Replace with your Login activity class
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                // Dismiss the dialog and stay on the profile page
                dialog.dismiss()
            }
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data!!
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.imageCartoon.setImageBitmap(bitmap)

                // Save the image to internal storage
                saveProfileImage(bitmap)

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        try {
            val file = File(filesDir, IMAGE_FILE_NAME)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(this, "Profile image saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage() {
        try {
            val file = File(filesDir, IMAGE_FILE_NAME)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.imageCartoon.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }
}
