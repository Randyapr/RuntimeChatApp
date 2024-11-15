package com.example.runtimechatapp.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.runtimechatapp.MainActivity
import com.example.runtimechatapp.databinding.ActivityNameBinding
import com.example.runtimechatapp.menu.MenuActivity
import com.example.runtimechatapp.model.UserModel
import com.example.runtimechatapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class NameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan support action bar jika ada
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Mengisi nomor telepon secara otomatis
        val phoneNumber = auth.currentUser?.phoneNumber
        binding.textinputPhone.setText(phoneNumber)

        binding.editImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.Aggre.setOnClickListener {
            binding.nameProgressbar.visibility = android.view.View.VISIBLE
            val name: String = binding.txtInputName.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please type your name", Toast.LENGTH_SHORT).show()
                binding.nameProgressbar.visibility = android.view.View.GONE
                return@setOnClickListener
            }

            // Jika gambar tidak dipilih, gunakan URL default atau null
            if (selectedImageUri != null) {
                uploadImageAndGetUrl(name)
            } else {
                uploadUserData(name, null)
            }
        }
        if (isUserLoggedIn()) {
            // Arahkan ke MenuActivity dan tutup NameActivity
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
            return // Hentikan eksekusi onCreate()
        }
    }
    private fun isUserLoggedIn(): Boolean {
        val uid = auth.currentUser?.uid ?: return false // Jika uid null, pengguna belum login

        database.reference.child(Config.USERS).child(uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isLoggedIn = snapshot.exists() // Periksa keberadaan node pengguna

                if (isLoggedIn) {
                    // Arahkan ke MenuActivity dan tutup NameActivity
                    val intent = Intent(this@NameActivity, MenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error jika diperlukan
            }
        })

        return false // Kembalikan false secara default, navigasi akan ditangani di onDataChange()
    }
    // Fungsi untuk mengupload gambar dan mendapatkan URL, lalu upload data user
    private fun uploadImageAndGetUrl(name: String) {
        val storageRef = storage.reference.child("Profile").child(auth.uid!!)
        storageRef.putFile(selectedImageUri!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    uploadUserData(name, imageUrl)
                }
            } else {
                binding.nameProgressbar.visibility = android.view.View.GONE
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadUserData(name: String, imageUrl: String?) {
        val uid = auth.uid
        val phone = auth.currentUser?.phoneNumber

        val user = UserModel(uid, name, phone, imageUrl)

        database.reference
            .child(Config.USERS)
            .child(uid!!)
            .setValue(user)
            .addOnSuccessListener {
                binding.nameProgressbar.visibility = android.view.View.GONE
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                binding.nameProgressbar.visibility = android.view.View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.profileImage.setImageURI(selectedImageUri)
        }
    }
}