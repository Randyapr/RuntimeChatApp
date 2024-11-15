package com.example.runtimechatapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runtimechatapp.databinding.ActivityOtpactivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var verifyId: String
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        verifyId = intent.getStringExtra("storedVerificationId").toString()
        phoneNumber = intent.getStringExtra("phoneNumber").toString()

        binding.otpProgressBar.visibility = android.view.View.INVISIBLE

        addTextChangeListener()
        resendOtpVisibility()

        binding.verifyOTPBtn.setOnClickListener {
            val otp = (binding.otpEditText1.text.toString() +
                    binding.otpEditText2.text.toString() +
                    binding.otpEditText3.text.toString() +
                    binding.otpEditText4.text.toString() +
                    binding.otpEditText5.text.toString() +
                    binding.otpEditText6.text.toString())

            if (otp.isNotEmpty()) {
                binding.otpProgressBar.visibility = android.view.View.VISIBLE
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    verifyId, otp
                )
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    binding.otpProgressBar.visibility = android.view.View.INVISIBLE
                    Toast.makeText(this, "Welcome...", Toast.LENGTH_SHORT).show()
                    sendToMain()
                } else {
                    binding.otpProgressBar.visibility = android.view.View.INVISIBLE
                    Toast.makeText(this, "OTP is not Valid", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendToMain() {
        startActivity(Intent(this, NameActivity::class.java))
        finish()
    }

    private fun addTextChangeListener() {
        binding.otpEditText1.addTextChangedListener(EditTextWatcher(binding.otpEditText1))
        binding.otpEditText2.addTextChangedListener(EditTextWatcher(binding.otpEditText2))
        binding.otpEditText3.addTextChangedListener(EditTextWatcher(binding.otpEditText3))
        binding.otpEditText4.addTextChangedListener(EditTextWatcher(binding.otpEditText4))
        binding.otpEditText5.addTextChangedListener(EditTextWatcher(binding.otpEditText5))
        binding.otpEditText6.addTextChangedListener(EditTextWatcher(binding.otpEditText6))
    }

    private fun resendOtpVisibility() {
        binding.otpEditText1.setText("")
        binding.otpEditText2.setText("")
        binding.otpEditText3.setText("")
        binding.otpEditText4.setText("")
        binding.otpEditText5.setText("")
        binding.otpEditText6.setText("")

        binding.resendTextView.setOnClickListener {
            // resend otp
        }
    }

    inner class EditTextWatcher(private val view: android.view.View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            when (view.id) {
                binding.otpEditText1.id -> {
                    if (p0.toString().isNotEmpty()) {
                        binding.otpEditText2.requestFocus()
                    }
                }
                binding.otpEditText2.id -> {
                    if (p0.toString().isNotEmpty()) {
                        binding.otpEditText3.requestFocus()
                    } else {
                        binding.otpEditText1.requestFocus()
                    }
                }
                binding.otpEditText3.id -> {
                    if (p0.toString().isNotEmpty()) {
                        binding.otpEditText4.requestFocus()
                    } else {
                        binding.otpEditText2.requestFocus()
                    }
                }
                binding.otpEditText4.id -> {
                    if (p0.toString().isNotEmpty()) {
                        binding.otpEditText5.requestFocus()
                    } else {
                        binding.otpEditText3.requestFocus()
                    }
                }
                binding.otpEditText5.id -> {
                    if (p0.toString().isNotEmpty()) {
                        binding.otpEditText6.requestFocus()
                    } else {
                        binding.otpEditText4.requestFocus()
                    }
                }
                binding.otpEditText6.id -> {
                    if (p0.toString().isEmpty()) {
                        binding.otpEditText5.requestFocus()
                    }
                }
            }
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    companion object {
        private const val TAG = "OTPActivity"
    }
}