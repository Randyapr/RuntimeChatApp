package com.example.runtimechatapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.runtimechatapp.Adapter.MessageAdapter
import com.example.runtimechatapp.databinding.ActivityMessageBinding
import com.example.runtimechatapp.model.MessageModel
import com.example.runtimechatapp.model.UserModel
import com.example.runtimechatapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<MessageModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        senderUid = auth.currentUser?.uid.toString()
        receiverUid = intent.getStringExtra("contactUid") ?: ""

        // Mengambil data kontak dan menampilkannya di toolbar
        getContactDetails(receiverUid)

        // Inisialisasi RecyclerView dan adapter
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messageList, senderUid)
        binding.messageRecyclerView.adapter = messageAdapter

        // Mengambil data pesan dari Firebase
        getMessages()

        // Mengirim pesan saat tombol Send diklik
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageEditText.text.clear()
            }
        }
    }

    private fun getContactDetails(uid: String) {
        database.reference.child(Config.USERS).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contact = snapshot.getValue(UserModel::class.java)
                    if (contact != null) {
                        binding.tvNameAtas.text = contact.name
                        Glide.with(this@MessageActivity)
                            .load(contact.profileImage)
                            .into(binding.ivContact)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MessageActivity, "Error loading contact details", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getMessages() {
        database.reference.child(Config.CHATS)
            .child(senderUid).child(receiverUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(MessageModel::class.java)
                        if (message != null) {
                            messageList.add(message)
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                    // Gulir ke posisi pesan terakhir
                    binding.messageRecyclerView.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MessageActivity, "Error loading messages", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendMessage(messageText: String) {
        val message = MessageModel(
            senderId = senderUid,
            receiverId = receiverUid,
            message = messageText,
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        database.reference.child(Config.CHATS)
            .child(senderUid).child(receiverUid).push().setValue(message)
        database.reference.child(Config.CHATS).child(receiverUid).child(senderUid).push().setValue(message)
    }
}