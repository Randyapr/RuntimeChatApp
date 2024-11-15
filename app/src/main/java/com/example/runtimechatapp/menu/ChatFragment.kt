package com.example.runtimechatapp.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runtimechatapp.Adapter.ChatAdapter
import com.example.runtimechatapp.databinding.FragmentChatBinding
import com.example.runtimechatapp.model.ChatListModel
import com.example.runtimechatapp.model.UserModel
import com.example.runtimechatapp.ui.MessageActivity
import com.example.runtimechatapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<ChatListModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inisialisasi RecyclerView dan adapter
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        chatAdapter = ChatAdapter(chatList) { chatListModel ->
            // Navigasi ke MessageActivity saat item chat diklik
            val intent = Intent(requireContext(), MessageActivity::class.java)
            intent.putExtra("contactUid", chatListModel.uid) // Kirim UID kontak
            startActivity(intent)
        }
        binding.rvChat.adapter = chatAdapter

        // Mengambil data chat dari Firebase
        getChatList()
    }

    private fun getChatList() {
        val currentUserId = auth.currentUser?.uid ?: return // Handle jika currentUserId null

        val chatListRef = database.reference.child(Config.CHATS).child(currentUserId)

        chatListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (chatSnapshot in snapshot.children) {
                    val chatUid = chatSnapshot.key // UID kontak

                    // Ambil data kontak dari node USERS
                    database.reference.child(Config.USERS).child(chatUid!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(UserModel::class.java)
                                if (user != null) {
                                    val lastMessageRef = chatSnapshot.child("lastMessage").ref
                                    lastMessageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(messageSnapshot: DataSnapshot) {
                                            val lastMessage = messageSnapshot.getValue(String::class.java) ?: ""
                                            val chatListModel = ChatListModel(
                                                uid = user.uid.toString(),
                                                name = user.name.toString(),
                                                profileImage = user.profileImage,
                                                lastMessage = lastMessage
                                            )
                                            chatList.add(chatListModel)
                                            chatAdapter.notifyDataSetChanged()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // Handle error
                                        }
                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}