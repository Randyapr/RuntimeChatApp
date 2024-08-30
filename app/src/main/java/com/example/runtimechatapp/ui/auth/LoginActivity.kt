package com.example.runtimechatapp.ui.auth

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.runtimechatapp.R
import com.example.runtimechatapp.menu.MenuActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var sendOtp: Button
    private lateinit var etPhoneNumberRegister: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var number: String
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        sendOtp.setOnClickListener {
            number = etPhoneNumberRegister.text.trim().toString()
            if (number.isNotEmpty()) {
                if (number.length == 11) {
                    number = "+62$number"

                    progressBar.visibility = View.VISIBLE
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this) // Activity (for callback binding)
                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)

                } else {
                    Toast.makeText(this, "Masukkan nomor dengan benar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Masukkan nomor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init() {
        sendOtp = findViewById(R.id.send_otp)
        etPhoneNumberRegister = findViewById(R.id.etPhoneNumberRegister)
        progressBar = findViewById(R.id.loading)
        auth = FirebaseAuth.getInstance()
        progressBar.visibility = View.GONE
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this, "Authentication Success", Toast.LENGTH_SHORT).show()
                    sendToName()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    private fun sendToName() {
        startActivity(Intent(this, NameActivity::class.java))
        finish()
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this@LoginActivity, "Invalid request", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(this@LoginActivity, "Quota exceeded", Toast.LENGTH_SHORT).show()
            }
            progressBar.visibility = View.INVISIBLE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            val intent = Intent(this@LoginActivity, OTPActivity::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phone", number)
            startActivity(intent)
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
    }
}
