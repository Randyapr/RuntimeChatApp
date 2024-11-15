package com.example.runtimechatapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.runtimechatapp.Adapter.MessageAdapter
import com.example.runtimechatapp.R
import com.example.runtimechatapp.databinding.ActivityMessageBinding
import com.example.runtimechatapp.menu.ChatFragment
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

        // Get contact details
        getContactDetails(receiverUid)

        // Initialize RecyclerView and adapter
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messageList, senderUid)
        binding.messageRecyclerView.adapter = messageAdapter

        // Fetch messages from Firebase
        getMessages()

        // Handle send message
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageEditText.text.clear()
            }
        }
    }

    private fun getContactDetails(uid: String) {
        database.reference.child(Config.USERS).child(uid).get()
            .addOnSuccessListener { snapshot ->
                val contact = snapshot.getValue(UserModel::class.java)
                if (contact != null) {
                    binding.tvNameAtas.text = contact.name
                    Glide.with(this@MessageActivity)
                        .load(contact.profileImage)
                        .placeholder(R.drawable.circle_bg)
                        .into(binding.ivContact)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@MessageActivity, "Error loading contact details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getMessages() {
        database.reference.child(Config.CHATS)
            .child(senderUid).child(receiverUid)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(MessageModel::class.java)
                    if (message != null) {
                        messageList.add(message)
                        messageAdapter.notifyItemInserted(messageList.size - 1)
                        binding.messageRecyclerView.scrollToPosition(messageList.size - 1)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle updated message if needed
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Handle deleted message if needed
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle moved message if needed
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MessageActivity, "Error loading messages", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendMessage(messageText: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val message = MessageModel(
            senderId = senderUid,
            receiverId = receiverUid,
            message = messageText,
            timestamp = timestamp
        )

        // Push message to Firebase under both sender and receiver
        val messageRef = database.reference.child(Config.CHATS)
            .child(senderUid).child(receiverUid).push()

        messageRef.setValue(message).addOnSuccessListener {
            // Update last message for both users
            updateLastMessage(messageText, timestamp)
        }
    }

    private fun updateLastMessage(messageText: String, timestamp: String) {
        val lastMessageData = mapOf(
            "message" to messageText,
            "timestamp" to timestamp
        )

        // Update last message for both user conversations
        database.reference.child(Config.CHATS)
            .child(senderUid).child(receiverUid).child("lastMessage").setValue(lastMessageData)

        database.reference.child(Config.CHATS)
            .child(receiverUid).child(senderUid).child("lastMessage").setValue(lastMessageData)
    }
}
