package com.example.runtimechatapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.runtimechatapp.ui.auth.LoginActivity
import com.example.runtimechatapp.MainActivity
import com.example.runtimechatapp.R
import com.example.runtimechatapp.menu.MenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val startButton = findViewById<Button>(R.id.start_button)

        checkUserProfile()

        startButton.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists() && document.getString("name") != null) {
                    // Profil pengguna sudah lengkap, arahkan ke MainActivity
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                } else {
                    // Profil pengguna belum lengkap, tetap di WelcomeActivity atau arahkan ke LoginActivity
                    // Jika Anda ingin tetap di WelcomeActivity, hapus baris ini atau tambahkan logika lainnya
                }
            }.addOnFailureListener {
                // Gagal mengambil data pengguna, tetap di WelcomeActivity atau arahkan ke LoginActivity
                // Jika Anda ingin tetap di WelcomeActivity, hapus baris ini atau tambahkan logika lainnya
            }
        } else {
            // Pengguna belum terautentikasi, tetap di WelcomeActivity atau arahkan ke LoginActivity
            // Jika Anda ingin tetap di WelcomeActivity, hapus baris ini atau tambahkan logika lainnya
        }
    }
}
