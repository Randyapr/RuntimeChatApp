package com.example.runtimechatapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runtimechatapp.databinding.ActivityAddContactBinding
import com.example.runtimechatapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setupUI()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun setupUI() {
        binding.save.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val phone = binding.inputPhone.text.toString().trim()

            if (validateInput(name, phone)) {
                binding.nameProgressbar.visibility = View.VISIBLE
                addContactToDatabase(name, phone)
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(name: String, phone: String): Boolean {
        when {
            name.isEmpty() -> {
                showToast("Nama tidak boleh kosong")
                return false
            }
            phone.isEmpty() -> {
                showToast("Nomor telepon tidak boleh kosong")
                return false
            }
        }
        return true
    }

    private fun addContactToDatabase(name: String, phone: String) {
        try {
            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.parse(phone, "ID")
            val formattedPhone = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)

            Log.d(TAG, "Current User Phone: ${auth.currentUser?.phoneNumber}")
            Log.d(TAG, "Input Phone: $formattedPhone")

            // Validasi nomor sendiri
            if (formattedPhone == auth.currentUser?.phoneNumber) {
                binding.nameProgressbar.visibility = View.GONE
                showToast("Tidak bisa menambahkan nomor sendiri")
                return
            }

            // Cari user berdasarkan nomor telepon
            val usersRef = database.reference.child(Config.USERS)
            usersRef.orderByChild("phone")
                .equalTo(formattedPhone)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            binding.nameProgressbar.visibility = View.GONE
                            showToast("Nomor telepon tidak terdaftar")
                            return
                        }

                        // Ambil user pertama yang ditemukan
                        val userSnapshot = snapshot.children.first()
                        val contactUid = userSnapshot.key!!

                        // Tambahkan ke kontak
                        saveContact(contactUid)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.nameProgressbar.visibility = View.GONE
                        showToast("Gagal mencari user: ${error.message}")
                        Log.e(TAG, "Error finding user: ${error.message}")
                    }
                })

        } catch (e: NumberParseException) {
            binding.nameProgressbar.visibility = View.GONE
            showToast("Format nomor telepon tidak valid")
            Log.e(TAG, "Error parsing phone: ${e.message}")
        }
    }

    private fun saveContact(contactUid: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val contactRef = database.reference
            .child(Config.CONTACTS)
            .child(currentUserId)
            .child(contactUid)

        val contactData = mapOf(
            "timestamp" to ServerValue.TIMESTAMP
        )

        contactRef.setValue(contactData)
            .addOnSuccessListener {
                binding.nameProgressbar.visibility = View.GONE
                showToast("Kontak berhasil ditambahkan")
                finish()
            }
            .addOnFailureListener { e ->
                binding.nameProgressbar.visibility = View.GONE
                showToast("Gagal menambahkan kontak: ${e.message}")
                Log.e(TAG, "Error adding contact: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "AddContactActivity"
    }
}