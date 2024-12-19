package com.example.runtimechatapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtimechatapp.R
import com.example.runtimechatapp.databinding.ItemChatBinding
import com.example.runtimechatapp.model.ChatListModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val chatList: List<ChatListModel>,
    private val onItemClick: (ChatListModel) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]

        holder.binding.apply {
            // Set name
            tvName.text = chat.name

            // Set last message
            tvLastMessage.text = chat.lastMessage ?: "No messages yet"

            // Set time
            tvTime.text = getFormattedTime(chat.timestamp)

            // Load profile image
            Glide.with(ivProfile)
                .load(chat.profileImage)
                .placeholder(R.drawable.circle_bg)
                .into(ivProfile)

            // Handle unread count
            if (chat.unreadCount > 0) {
                tvUnreadCount.apply {
                    visibility = View.VISIBLE
                    text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString()
                }
            } else {
                tvUnreadCount.visibility = View.GONE
            }

            // Click listener
            root.setOnClickListener { onItemClick(chat) }
        }
    }

    override fun getItemCount() = chatList.size

    private fun getFormattedTime(timestamp: Long): String {
        if (timestamp == 0L) return ""

        val sdf = when {
            isToday(timestamp) -> SimpleDateFormat("HH:mm", Locale.getDefault())
            isYesterday(timestamp) -> return "Yesterday"
            isThisWeek(timestamp) -> SimpleDateFormat("EEE", Locale.getDefault())
            isThisYear(timestamp) -> SimpleDateFormat("dd MMM", Locale.getDefault())
            else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        }

        return sdf.format(Date(timestamp))
    }

    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.timeInMillis = timestamp
        val day = calendar.get(Calendar.DAY_OF_YEAR)
        return today == day
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.timeInMillis = timestamp
        val day = calendar.get(Calendar.DAY_OF_YEAR)
        return yesterday == day
    }

    private fun isThisWeek(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val thisWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        calendar.timeInMillis = timestamp
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        return thisWeek == week
    }

    private fun isThisYear(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val thisYear = calendar.get(Calendar.YEAR)
        calendar.timeInMillis = timestamp
        val year = calendar.get(Calendar.YEAR)
        return thisYear == year
    }
}