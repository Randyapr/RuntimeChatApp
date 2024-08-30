package com.example.runtimechatapp.ui.auth

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.runtimechatapp.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verifyOTPBtn: Button
    private lateinit var resendTextView: TextView
    private lateinit var inputOtp1: EditText
    private lateinit var inputOtp2: EditText
    private lateinit var inputOtp3: EditText
    private lateinit var inputOtp4: EditText
    private lateinit var inputOtp5: EditText
    private lateinit var inputOtp6: EditText
    private lateinit var progressBar: ProgressBar

    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otpactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phone").toString()

        auth = FirebaseAuth.getInstance()
        verifyOTPBtn = findViewById(R.id.verifyOTPBtn)
        resendTextView = findViewById(R.id.resendTextView)
        inputOtp1 = findViewById(R.id.otpEditText1)
        inputOtp2 = findViewById(R.id.otpEditText2)
        inputOtp3 = findViewById(R.id.otpEditText3)
        inputOtp4 = findViewById(R.id.otpEditText4)
        inputOtp5 = findViewById(R.id.otpEditText5)
        inputOtp6 = findViewById(R.id.otpEditText6)
        progressBar = findViewById(R.id.otpProgressBar)

        progressBar.visibility = View.GONE

        setupOtpFields()
        verifyOTPBtn.setOnClickListener {
            val otp = getOTPFromFields()
            if (otp.length == 6) {
                verifyOTP(otp)
            } else {
                Toast.makeText(this, "Masukkan kode OTP yang valid", Toast.LENGTH_SHORT).show()
            }
        }

        resendTextView.setOnClickListener {
            resendOtp()
        }
    }

    private fun setupOtpFields() {
        val otpFields = listOf(inputOtp1, inputOtp2, inputOtp3, inputOtp4, inputOtp5, inputOtp6)

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    } else if (s.isNullOrEmpty() && index > 0) {
                        otpFields[index - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun getOTPFromFields(): String {
        return inputOtp1.text.toString() +
                inputOtp2.text.toString() +
                inputOtp3.text.toString() +
                inputOtp4.text.toString() +
                inputOtp5.text.toString() +
                inputOtp6.text.toString()
    }

    private fun verifyOTP(otp: String) {
        progressBar.visibility = View.VISIBLE
        val credential = PhoneAuthProvider.getCredential(OTP, otp)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    Toast.makeText(this, "Authentication Success", Toast.LENGTH_SHORT).show()
                    navigateToNameActivity()
                } else {
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "OTP tidak valid", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
            }
    }

    private fun navigateToNameActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, NameActivity::class.java))
            finish()
        }, 1000)
    }

    private fun resendOtp() {
        progressBar.visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w(ContentValues.TAG, "onVerificationFailed", e)
                    Toast.makeText(this@OTPActivity, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    OTP = verificationId
                    resendToken = token
                    progressBar.visibility = View.INVISIBLE
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    Toast.makeText(this, "Authentication Success", Toast.LENGTH_SHORT).show()
                    navigateToNameActivity()
                } else {
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
