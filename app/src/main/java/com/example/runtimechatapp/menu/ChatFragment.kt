package com.example.runtimechatapp.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runtimechatapp.Adapter.ChatAdapter
import com.example.runtimechatapp.databinding.FragmentChatBinding
import com.example.runtimechatapp.model.ChatListModel
import com.example.runtimechatapp.model.MessageModel
import com.example.runtimechatapp.model.UserModel
import com.example.runtimechatapp.ui.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<ChatListModel>()
    private var chatListListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFirebase()
        setupRecyclerView()
        observeChats()
    }

    private fun setupFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun setupRecyclerView() {
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            chatAdapter = ChatAdapter(chatList) { chatModel ->
                navigateToMessage(chatModel)
            }
            adapter = chatAdapter
        }
    }

    private fun observeChats() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Amati perubahan di node chats
        database.reference
            .child("chats")  // Pastikan path sesuai dengan struktur database
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatList.clear()

                    for (chatSnapshot in snapshot.children) {
                        // Cek apakah user adalah participant
                        if (!chatSnapshot.child("participants")
                                .child(currentUserId).exists()) continue

                        val participantsSnapshot = chatSnapshot.child("participants")
                        val otherUserId = participantsSnapshot.children
                            .map { it.key }
                            .firstOrNull { it != currentUserId } ?: continue

                        // Ambil data pesan terakhir
                        val lastMessage = chatSnapshot.child("lastMessage")
                        val lastMessageText = lastMessage.child("message")
                            .getValue(String::class.java)
                        val timestamp = lastMessage.child("timestamp")
                            .getValue(Long::class.java) ?: 0L

                        // Ambil data user
                        database.reference.child("users").child(otherUserId)
                            .get()
                            .addOnSuccessListener { userSnapshot ->
                                val user = userSnapshot.getValue(UserModel::class.java)
                                user?.let {
                                    val chatModel = ChatListModel(
                                        uid = it.uid,
                                        name = it.name,
                                        profileImage = it.profileImage,
                                        lastMessage = lastMessageText,
                                        timestamp = timestamp
                                    )
                                    updateChatList(chatModel)
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatFragment", "Error loading chats: ${error.message}")
                }
            })
    }

    private fun updateChatList(newChat: ChatListModel) {
        val existingIndex = chatList.indexOfFirst { it.uid == newChat.uid }
        if (existingIndex != -1) {
            chatList[existingIndex] = newChat
            chatAdapter.notifyItemChanged(existingIndex)
        } else {
            chatList.add(newChat)
            chatList.sortByDescending { it.timestamp }
            chatAdapter.notifyDataSetChanged()
        }
    }
    private fun getUnreadCount(chatSnapshot: DataSnapshot, currentUserId: String): Int {
        var count = 0
        chatSnapshot.child("messages").children.forEach { messageSnapshot ->
            val message = messageSnapshot.getValue(MessageModel::class.java)
            if (message?.senderId != currentUserId && message?.status != "read") {
                count++
            }
        }
        return count
    }


    private fun navigateToMessage(chatModel: ChatListModel) {
        val intent = Intent(requireContext(), MessageActivity::class.java).apply {
            putExtra("contactUid", chatModel.uid)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatListListener?.let {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                database.reference
                    .child("userChats")
                    .child(currentUserId)
                    .removeEventListener(it)
            }
        }
        _binding = null
    }
}