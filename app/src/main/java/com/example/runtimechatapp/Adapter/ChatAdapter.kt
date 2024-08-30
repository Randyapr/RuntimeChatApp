package com.example.runtimechatapp.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtimechatapp.R
import com.example.runtimechatapp.data.ChatItem
import com.example.runtimechatapp.data.User
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    private val chatList: List<ChatItem>,
    private val onItemClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.username)
        val lastMessage : TextView = itemView.findViewById(R.id.last_message)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.iv_profile_image)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(chatList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val chatItem = chatList[position]
        holder.nameTextView.text = chatItem.nameReceiver
        holder.lastMessage.text = chatItem.lastMessage
        holder.timestamp.text = chatItem.timestamp.toString()
        if (chatItem.photoUrl.isNullOrEmpty()) {
            // Jika kosong, gunakan gambar default
            Glide.with(holder.itemView.context)
                .load(R.drawable.circle_bg)
                .into(holder.profileImageView)
        } else {
            // Jika tidak kosong, load gambar dari URL
            Glide.with(holder.itemView.context)
                .load(chatItem.photoUrl)
                .into(holder.profileImageView)
        }
        Log.d("ChatAdapter", "Binding user: $chatItem")
    }
    override fun getItemCount(): Int = chatList.size
}