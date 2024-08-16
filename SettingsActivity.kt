package com.example.funding

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI components
        val deleteAccountButton: TextView = findViewById(R.id.deleteAccountButton)

        // Set click listener for delete account button
        deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()

        user?.let {
            Log.d("SettingsActivity", "Attempting to delete user data for UID: ${it.uid}")

            // Delete user data from Firestore
            firestore.collection("users").document(it.uid)
                .delete()
                .addOnSuccessListener {
                    Log.d("SettingsActivity", "User data deleted successfully")

                    // Now delete the user's account from Firebase Auth
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("SettingsActivity", "User account deleted successfully")

                                // Redirect to login page
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                                Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("SettingsActivity", "Failed to delete user account: ${task.exception?.message}")
                                Toast.makeText(this, "Failed to delete account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("SettingsActivity", "Failed to delete user data: ${e.message}")
                    Toast.makeText(this, "Failed to delete user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Log.e("SettingsActivity", "No authenticated user found.")
            Toast.makeText(this, "No authenticated user found.", Toast.LENGTH_SHORT).show()
        }
    }
}
