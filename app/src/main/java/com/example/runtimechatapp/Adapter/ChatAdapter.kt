package com.example.runtimechatapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtimechatapp.databinding.ItemChatBinding
import com.example.runtimechatapp.model.ChatListModel

class ChatAdapter(
    private val chatList: List<ChatListModel>,
    private val onChatClick: (ChatListModel) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]

        holder.binding.username.text = chat.name
        holder.binding.lastMessage.text = chat.lastMessage

        // Load gambar profil jika ada
        Glide.with(holder.itemView.context)
            .load(chat.profileImage)
            .into(holder.binding.ivProfileImage)

        // Set onClickListener untuk item chat
        holder.itemView.setOnClickListener {
            onChatClick(chat) // Panggil lambda function
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}