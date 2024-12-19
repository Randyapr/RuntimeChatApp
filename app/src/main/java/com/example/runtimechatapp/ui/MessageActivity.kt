package com.example.runtimechatapp.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.runtimechatapp.Adapter.MessageAdapter
import com.example.runtimechatapp.R
import com.example.runtimechatapp.databinding.ActivityMessageBinding
import com.example.runtimechatapp.model.MessageModel
import com.example.runtimechatapp.model.UserModel
import com.example.runtimechatapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var chatId: String
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<MessageModel>()
    private lateinit var notificationManager: NotificationManager
    private var receiverName: String = ""
    private var messagesListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeVariables()
        setupUI()
        setupNotificationChannel()
        loadMessages()
        checkNotificationPermission()
    }

    private fun initializeVariables() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        senderUid = auth.currentUser?.uid.orEmpty()
        receiverUid = intent.getStringExtra("contactUid").orEmpty()
        chatId = generateChatId(senderUid, receiverUid)
    }

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupMessageInput()
        loadContactDetails()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarMessage)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat message notifications"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMessageInput() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageEditText.text.clear()
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(
            messageList,
            senderUid
        ) { message, position ->
            showDeleteDialog(message, position)
        }

        binding.messageRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@MessageActivity).apply {
                stackFromEnd = true
            }
        }
    }

    private fun loadContactDetails() {
        database.reference.child(Config.USERS).child(receiverUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)
                    user?.let {
                        receiverName = it.name ?: "User"
                        binding.tvNameAtas.text = receiverName
                        Glide.with(this@MessageActivity)
                            .load(it.profileImage)
                            .placeholder(R.drawable.circle_bg)
                            .into(binding.ivContact)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Error loading contact details")
                }
            })
    }

    private fun loadMessages() {
        val messagesRef = database.reference
            .child(Config.CHATS)
            .child(chatId)
            .child("messages")
            .orderByChild("timestamp")

        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(MessageModel::class.java)?.apply {
                        messageId = messageSnapshot.key
                    }
                    message?.let {
                        messageList.add(it)
                    }
                }
                messageAdapter.notifyDataSetChanged()
                if (messageList.isNotEmpty()) {
                    binding.messageRecyclerView.scrollToPosition(messageList.size - 1)
                }
                markMessagesAsRead()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error loading messages: ${error.message}")
            }
        }

        messagesRef.addValueEventListener(messagesListener!!)
    }

    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    private fun sendMessage(content: String) {
        val timestamp = System.currentTimeMillis()
        val messageId = database.reference.child("chats")
            .child(chatId)
            .child("messages")
            .push()
            .key ?: return

        val message = MessageModel(
            messageId = messageId,
            senderId = senderUid,
            message = content,
            timestamp = timestamp.toString()
        )

        val updates = hashMapOf<String, Any>(
            // Tambah message baru
            "/chats/$chatId/messages/$messageId" to message,

            // Update participants jika belum ada
            "/chats/$chatId/participants/$senderUid" to true,
            "/chats/$chatId/participants/$receiverUid" to true,

            // Update last message
            "/chats/$chatId/lastMessage" to mapOf(
                "message" to content,
                "timestamp" to timestamp,
                "senderId" to senderUid
            )
        )

        database.reference.updateChildren(updates)
    }

    @SuppressLint("NotificationPermission")
    private fun showNotification(messageContent: String) {
        val intent = Intent(this, MessageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("contactUid", receiverUid)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("New message from $receiverName")
            .setContentText(messageContent)
            .setSmallIcon(R.drawable.ic_notifikasi)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showDeleteDialog(message: MessageModel, position: Int) {
        if (message.senderId != senderUid) return

        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMessage(message, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(message: MessageModel, position: Int) {
        val messageId = message.messageId ?: return

        database.reference
            .child(Config.CHATS)
            .child(chatId)
            .child("messages")
            .child(messageId)
            .removeValue()
            .addOnSuccessListener {
                messageList.removeAt(position)
                messageAdapter.notifyItemRemoved(position)
                showToast("Message deleted")
            }
            .addOnFailureListener {
                showToast("Failed to delete message")
            }
    }

    private fun showDeleteChatDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Chat")
            .setMessage("Are you sure you want to delete this chat?")
            .setPositiveButton("Delete") { _, _ ->
                deleteChat()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteChat() {
        database.reference
            .child(Config.CHATS)
            .child(chatId)
            .removeValue()
            .addOnSuccessListener {
                showToast("Chat deleted successfully")
                finish()
            }
            .addOnFailureListener {
                showToast("Failed to delete chat")
            }
    }

    private fun markMessagesAsRead() {
        messageList.forEach { message ->
            if (message.senderId == receiverUid && message.status != "read") {
                message.messageId?.let { messageId ->
                    updateMessageStatus(messageId, "read")
                }
            }
        }
    }

    private fun updateMessageStatus(messageId: String, status: String) {
        database.reference
            .child(Config.CHATS)
            .child(chatId)
            .child("messages")
            .child(messageId)
            .child("status")
            .setValue(status)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_delete -> {
                showDeleteChatDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.let { listener ->
            database.reference
                .child(Config.CHATS)
                .child(chatId)
                .child("messages")
                .removeEventListener(listener)
        }
    }

    companion object {
        private const val CHANNEL_ID = "chat_messages"
        private const val NOTIFICATION_ID = 1
        private var isActivityVisible = false
    }
}