package com.example.runtimechatapp.Adapter

import android.icu.text.SimpleDateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtimechatapp.R
import com.example.runtimechatapp.data.Message
import com.example.runtimechatapp.databinding.ItemMessageBinding
import com.example.runtimechatapp.databinding.ItemMessageLeftBinding
import com.example.runtimechatapp.databinding.ItemMessageRightBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import java.util.Date
import java.util.Locale

class MessageAdapter(
    options: FirebaseRecyclerOptions<Message>,
    private val currentUserName: String?
) : FirebaseRecyclerAdapter<Message, MessageAdapter.MessageViewHolder>(options) {

    private val VIEW_TYPE_MESSAGE_ME = 1
    private val VIEW_TYPE_MESSAGE_OTHER = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = if (viewType == VIEW_TYPE_MESSAGE_ME) {
            ItemMessageRightBinding.inflate(inflater, parent, false)
        } else {
            ItemMessageLeftBinding.inflate(inflater, parent, false)
        }
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        holder.bind(model)
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (currentUserName == message.sentTo) {
            VIEW_TYPE_MESSAGE_OTHER
        } else {
            VIEW_TYPE_MESSAGE_ME
        }
    }


    inner class MessageViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) {
            if (binding is ItemMessageLeftBinding) {
                binding.pesanKiri.text = item.lastMessage
                val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                binding.timeKiri.text = item.timestamp
            } else if (binding is ItemMessageRightBinding) {
                binding.pesanKanan.text = item.lastMessage
                val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                binding.timeView.text = item.timestamp
//                Glide.with(itemView.context)
//                    .load(item.photoUrl)
//                    .circleCrop()
//                    .into(binding.iv_contact)
            }
        }
    }
}
