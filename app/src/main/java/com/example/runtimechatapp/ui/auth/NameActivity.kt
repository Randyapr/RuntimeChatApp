package com.example.runtimechatapp.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.runtimechatapp.MainActivity
import com.example.runtimechatapp.R
import com.example.runtimechatapp.menu.MenuActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class NameActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: TextInputEditText
    private lateinit var progressBar: ProgressBar

    private val PICK_IMAGE_REQUEST = 71
    private var imageUri: Uri? = null

    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                showImage()
            } else {
                Log.d("Pemilih Foto", "Tidak ada media yang dipilih")
            }
        }

    private fun showImage() {
        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            profileImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImageView = findViewById(R.id.profile_image)
        nameEditText = findViewById(R.id.txtInputName)
        progressBar = findViewById(R.id.name_progressbar)

        val editImageButton: ImageButton = findViewById(R.id.edit_image)
        val saveButton: Button = findViewById(R.id.Aggre)

        editImageButton.setOnClickListener {
            openGallery()
        }

        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun openGallery() {
        launcherGallery.launch("image/*")
    }

    private fun saveUserData() {
        val userName = nameEditText.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE

        if (imageUri != null) {
            val fileName = UUID.randomUUID().toString()
            val storageRef: StorageReference = storage.reference.child("profile_pictures/$fileName")
            storageRef.putFile(imageUri!!).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveUserProfile(userId!!, userName, uri.toString())
                }.addOnFailureListener {
                    Log.e("SaveUserData", "Failed to get download URL", it)
                    progressBar.visibility = ProgressBar.INVISIBLE
                    Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Log.e("SaveUserData", "Failed to upload image", it)
                progressBar.visibility = ProgressBar.INVISIBLE
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveUserProfile(userId!!, userName, "")
        }
    }

    private fun saveUserProfile(userId: String, userName: String, profileImageUrl: String) {
        val userProfile = hashMapOf(
            "name" to userName,
            "profile_picture" to profileImageUrl
        )

        firestore.collection("users").document(userId).set(userProfile)
            .addOnSuccessListener {
                progressBar.visibility = ProgressBar.INVISIBLE
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                progressBar.visibility = ProgressBar.INVISIBLE
                Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
            }
    }
}
