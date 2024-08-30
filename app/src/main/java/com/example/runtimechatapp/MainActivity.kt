package com.example.runtimechatapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.runtimechatapp.ui.auth.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var saveButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var profile_ImageView: ImageView
    private lateinit var editImageButton: ImageButton

    private val PICK_IMAGE_REQUEST = 71
    private var imageUri: Uri? = null

    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                showImage()
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up logout btn
        logoutButton = findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        saveButton = findViewById(R.id.btn_save)
        saveButton.setOnClickListener {
            updateUserData()
        }

        // Add edit image btn
        editImageButton = findViewById(R.id.edit_image)
        profile_ImageView = findViewById(R.id.profile_image)

        editImageButton.setOnClickListener {
            openGallery()
        }

        // Load data userss
        loadUserData()

        // sett window insets to the main view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name")
                        val bio = document.getString("bio")
                        val phone = document.getString("phone_number")
                        val profileImageUrl = document.getString("profile_picture")

                        findViewById<TextInputEditText>(R.id.input_name).setText(name)
                        findViewById<TextInputEditText>(R.id.input_bio).setText(bio)
                        findViewById<TextInputEditText>(R.id.input_phone).setText(phone)

                        // Load profile image pake Glide
                        Glide.with(this).load(profileImageUrl).into(profile_ImageView)
                    } else {
                        Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting document: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openGallery() {
        launcherGallery.launch("image/*")
    }

    private fun showImage() {
        imageUri?.let {
            profile_ImageView.setImageURI(it)
        }
    }

    private fun updateUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val name = findViewById<TextInputEditText>(R.id.input_name).text.toString()
            val bio = findViewById<TextInputEditText>(R.id.input_bio).text.toString()
            val phone = findViewById<TextInputEditText>(R.id.input_phone).text.toString()

            val userData = mapOf(
                "name" to name,
                "bio" to bio,
                "phone_number" to phone
            )

            firestore.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener {
                    // If imageUri is not null, upload the image and save its URL
                    if (imageUri != null) {
                        uploadImageAndSaveUri()
                    } else {
                        Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error updating data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun uploadImageAndSaveUri() {
        val userId = auth.currentUser?.uid
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
            .child("profile_images/${UUID.randomUUID()}")

        imageUri?.let {
            storageRef.putFile(it)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val profileImageUrl = uri.toString()
                        firestore.collection("users").document(userId!!)
                            .update("profile_picture", profileImageUrl)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                                Glide.with(this).load(profileImageUrl).into(profile_ImageView)
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error updating profile picture: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}