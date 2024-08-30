package com.example.runtimechatapp.ui

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.runtimechatapp.Adapter.MessageAdapter
import com.example.runtimechatapp.R
import com.example.runtimechatapp.data.ItemMessage
import com.example.runtimechatapp.data.Message
import com.example.runtimechatapp.databinding.ActivityMessageBinding
import com.example.runtimechatapp.ui.auth.LoginActivity
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Date
import java.util.Locale

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val toolbar: Toolbar = findViewById(R.id.toolbar_message)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val userId = intent.getStringExtra("USER_ID") ?: return
        val userName = intent.getStringExtra("USER_NAME")
        val userProfilePicture = intent.getStringExtra("USER_PROFILE_PICTURE")

        val tvName: TextView = findViewById(R.id.tv_name_atas)
        tvName.text = userName

        val ivProfile: CircleImageView = findViewById(R.id.iv_contact)
        userProfilePicture?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(ivProfile)
        }

        auth = FirebaseAuth.getInstance()
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val db = FirebaseDatabase.getInstance().reference

        val chatId = firebaseUser.uid

        val messagesRef = db.child(chatId).child(userId)

        val messageRefSender = db.child("chats").child(chatId).child(userId)
        val messageRefReceiver = db.child("chats").child(userId).child(chatId)

        binding.sendButton.setOnClickListener {

//            val chat = Chat(
//                member = listOf(
//                    firebaseUser.uid,
//                    userId
//                ),
//                lastMessageSent = firebaseUser.uid
//            )
//
//            val chatMessage = ChatMessage(
//                sentBy = firebaseUser.uid,
//                sentTo = userId,
//                messageTime = Date().time,
//                message = binding.messageEditText.text.toString()
//            )
//
//            val chats = mapOf(
//                "0" to firebaseUser.uid,
//                "1" to userId
//            )
//
//            Log.d("CHAT", "onCreate: $chat")
//
//            val userChatId = "chat-${firebaseUser.uid}-$userId"
//
//            db.child("chats").child(userChatId).setValue(chat)
//            db.child("chatMessage").child(userChatId)
//                .child("${firebaseUser.uid}-${chatMessage.messageTime}").setValue(chatMessage)
//            db.child("userChats").child(firebaseUser.uid).child(userChatId).setValue(chats)

            val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val convertTime = dateFormat.format(Date(Date().time))

            val friendlyMessage = Message(
                sentTo = userId,
                lastMessage = binding.messageEditText.text.toString(),
                timestamp = convertTime,
                nameReceiver = userName,
                photoUrl = userProfilePicture,
                item = listOf(
                    ItemMessage(
                        text = binding.messageEditText.text.toString(),
                        timestamp = convertTime,
                    )
                )
            )

            val receiver = Message(
                sentTo = firebaseUser.uid,
                lastMessage = binding.messageEditText.text.toString(),
                timestamp = convertTime,
                nameReceiver = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl.toString(),
                item = listOf(
                    ItemMessage(
                        text = binding.messageEditText.text.toString(),
                        timestamp = convertTime,
                    )
                )
            )
            db.child("chats").child(userId).child(chatId).setValue(receiver)
            db.child("chats").child(chatId).child(userId).setValue(friendlyMessage)

            messagesRef.push().setValue(friendlyMessage) { error, _ ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        getString(R.string.send_error) + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, getString(R.string.send_success), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.messageEditText.setText("")
        }

        val manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = manager

        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(messagesRef, Message::class.java)
            .build()

        adapter = MessageAdapter(options, firebaseUser.uid)
        binding.messageRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.startListening()
    }

    override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    companion object {
        const val MESSAGES_CHILD = "Messages"
    }
}