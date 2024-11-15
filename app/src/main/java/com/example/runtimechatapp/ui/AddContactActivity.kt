package com.example.runtimechatapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runtimechatapp.databinding.ActivityAddContactBinding
import com.example.runtimechatapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.save.setOnClickListener {
            val name = binding.inputName.text.toString()
            val phone = binding.inputPhone.text.toString()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addContactToDatabase(name, phone)
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun addContactToDatabase(name: String, phone: String) {
        binding.nameProgressbar.visibility = View.VISIBLE

        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        try {
            val phoneNumber = phoneNumberUtil.parse(phone, "ID")
            val formattedPhone = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)

            if (formattedPhone == auth.currentUser?.phoneNumber) {
                binding.nameProgressbar.visibility = View.GONE
                Toast.makeText(this, "Tidak bisa menambah kontak sendiri", Toast.LENGTH_SHORT).show()
                return
            }

            val usersRef = database.reference.child(Config.USERS)
            val query = usersRef.orderByChild("phone").equalTo(formattedPhone)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val contactUid = userSnapshot.key

                            val currentUserId = auth.currentUser?.uid
                            val contactsRef = database.reference.child(Config.CONTACTS)
                                .child(currentUserId!!)
                                .child(contactUid!!)

                            contactsRef.setValue(true)
                                .addOnSuccessListener {
                                    binding.nameProgressbar.visibility = View.GONE
                                    Toast.makeText(this@AddContactActivity, "Contact Berhasil Ditambahkan", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    binding.nameProgressbar.visibility = View.GONE
                                    Toast.makeText(this@AddContactActivity, "Error untuk menambah contact: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            break
                        }
                    } else {
                        binding.nameProgressbar.visibility = View.GONE
                        Toast.makeText(this@AddContactActivity, "Nomor tidak terdaftar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.nameProgressbar.visibility = View.GONE
                    Toast.makeText(this@AddContactActivity, "Error validating phone number", Toast.LENGTH_SHORT).show()
                    Log.e("AddContactActivity", "Error validating phone number: ${error.message}")
                }
            })
        } catch (e: NumberParseException) {
            binding.nameProgressbar.visibility = View.GONE
            Toast.makeText(this, "Nomor telepon tidak valid", Toast.LENGTH_SHORT).show()
            Log.e("AddContactActivity", "Error parsing phone number: ${e.message}")
        }
    }
}
