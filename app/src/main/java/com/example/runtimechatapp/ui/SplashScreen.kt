package com.example.runtimechatapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.runtimechatapp.ui.auth.LoginActivity
import com.example.runtimechatapp.MainActivity
import com.example.runtimechatapp.ui.auth.NameActivity
import com.example.runtimechatapp.R
import com.example.runtimechatapp.menu.MenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        checkUserStatus()
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                Log.d("SplashScreen", "Document exists: ${document.exists()}")
                Log.d("SplashScreen", "Document data: ${document.data}")

                if (document.exists() && document.getString("name") != null) {
                    // Profil pengguna sudah lengkap, arahkan ke MainActivity
                    startActivity(Intent(this, MenuActivity::class.java))
                } else {
                    // Profil pengguna belum lengkap, arahkan ke NameActivity
                    startActivity(Intent(this, NameActivity::class.java))
                }
                finish() // Tutup SplashScreenActivity
            }.addOnFailureListener {
                Log.e("SplashScreen", "Failed to get user document", it)
                // Gagal mengambil data pengguna, arahkan ke NameActivity
                startActivity(Intent(this, NameActivity::class.java))
                finish()
            }
        } else {
            Log.d("SplashScreen", "No current user")
            // Pengguna belum terautentikasi, arahkan ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

}
