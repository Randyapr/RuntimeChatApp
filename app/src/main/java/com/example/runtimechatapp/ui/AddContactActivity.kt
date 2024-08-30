package com.example.runtimechatapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.runtimechatapp.Adapter.UserAdapter
import com.example.runtimechatapp.R
import com.example.runtimechatapp.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddContactActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val contactList = mutableListOf<User>()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.input_name)
        val etPhoneNumber = findViewById<EditText>(R.id.input_phone)
        val btnSave = findViewById<Button>(R.id.save)
        val btnCancel = findViewById<Button>(R.id.btn_cancel)

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val phoneNumber = etPhoneNumber.text.toString()

            if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                saveContactToFirestore(name, phoneNumber)
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveContactToFirestore(name: String, phoneNumber: String) {
        val userId = db.collection("users").document("User").id
        val newUser = User(userId, name, phoneNumber, "Busy", "")

        db.collection("users").document(userId).set(newUser).addOnSuccessListener {
            contactList.add(newUser)
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            Log.e("ContactFragment", "Error adding new contact: ", exception)
        }
    }
}
