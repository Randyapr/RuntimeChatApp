package com.example.runtimechatapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.runtimechatapp.databinding.ActivityMainBinding
import com.example.runtimechatapp.menu.MenuActivity
import com.example.runtimechatapp.model.UserModel
import com.example.runtimechatapp.ui.WelcomeActivity
import com.example.runtimechatapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Periksa apakah Intent berasal dari MenuActivity
        if (intent.getStringExtra("FROM_ACTIVITY") != "MenuActivity") {
            // Jika tidak dari MenuActivity, periksa status login
            if (auth.currentUser != null) {
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
                return
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
                return
            }
        }

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Mengambil data pengguna dari database
        fetchUserProfile()

        // Mengatur listener untuk tombol edit gambar profil
        binding.editImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, 100)
        }

        // Mengatur listener untuk tombol edit nama, bio, dan nomor telepon
        binding.editName.setOnClickListener { binding.inputName.isEnabled = true }
        binding.editBio.setOnClickListener { binding.inputBio.isEnabled = true }
        binding.editPhone.setOnClickListener { binding.inputPhone.isEnabled = true }

        // Mengatur listener untuk tombol save
        binding.btnSave.setOnClickListener {
            updateUserProfile()
        }

        // Mengatur listener untuk tombol logout
        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchUserProfile() {
        val currentUser = auth.currentUser ?: return // Jika currentUser null, hentikan fungsi
        val userReference = database.reference.child(Config.USERS).child(currentUser.uid)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    // Menampilkan data profil ke UI
                    binding.inputName.setText(user.name)
                    binding.inputBio.setText(user.bio)
                    binding.inputPhone.setText(user.phone)

                    // Menampilkan foto profil jika ada
                    user.profileImage?.let { imageUrl ->
                        Glide.with(this@MainActivity).load(imageUrl).into(binding.profileImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserProfile() {
        val name = binding.inputName.text.toString()
        val bio = binding.inputBio.text.toString()
        val phone = binding.inputPhone.text.toString()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userReference = database.reference.child(Config.USERS).child(currentUser.uid)
            // Update data pengguna di database
            val updatedUser = UserModel(
                uid = currentUser.uid,
                name = name,
                bio = bio,
                phone = phone,
                profileImage = currentUser.photoUrl?.toString() // Ambil URL gambar profil dari auth
            )
            userReference.setValue(updatedUser)
                .addOnSuccessListener {
                    Toast.makeText(this@MainActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    binding.inputName.isEnabled = false
                    binding.inputBio.isEnabled = false
                    binding.inputPhone.isEnabled = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@MainActivity, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            // Update foto profil jika ada yang dipilih
            selectedImageUri?.let { uri ->
                uploadProfileImage(uri)
            }
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val imageRef: StorageReference = storageReference.child("images/${currentUser.uid}/${UUID.randomUUID()}")

            imageRef.putFile(imageUri)
                .addOnSuccessListener { _ ->
                    // Dapatkan URL gambar yang diupload
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Update URL gambar profil di database
                        val userReference = database.reference.child(Config.USERS).child(currentUser.uid)
                        userReference.child("profileImage").setValue(downloadUri.toString())
                            .addOnSuccessListener {
                                Toast.makeText(this@MainActivity, "Profile image updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@MainActivity, "Error updating profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@MainActivity, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Glide.with(this).load(selectedImageUri).into(binding.profileImage)
        }
    }
}