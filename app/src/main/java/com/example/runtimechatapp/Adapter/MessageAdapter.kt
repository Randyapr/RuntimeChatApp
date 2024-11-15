package com.example.runtimechatapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.runtimechatapp.databinding.ItemMessageLeftBinding
import com.example.runtimechatapp.databinding.ItemMessageRightBinding
import com.example.runtimechatapp.model.MessageModel

class MessageAdapter(
    private val messageList: List<MessageModel>,
    private val senderUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_TYPE_RIGHT = 1
        const val ITEM_TYPE_LEFT = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_RIGHT) {
            val binding = ItemMessageRightBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            MessageRightViewHolder(binding)
        } else {
            val binding = ItemMessageLeftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            MessageLeftViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]

        if (holder.javaClass == MessageRightViewHolder::class.java) {
            val viewHolder = holder as MessageRightViewHolder
            viewHolder.binding.pesanKanan.text = message.message
            viewHolder.binding.timeView.text = message.timestamp
        } else {
            val viewHolder = holder as MessageLeftViewHolder
            viewHolder.binding.pesanKiri.text = message.message
            viewHolder.binding.timeKiri.text = message.timestamp
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId == senderUid) {
            ITEM_TYPE_RIGHT
        } else {
            ITEM_TYPE_LEFT
        }
    }

    inner class MessageRightViewHolder(val binding: ItemMessageRightBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class MessageLeftViewHolder(val binding: ItemMessageLeftBinding) :
        RecyclerView.ViewHolder(binding.root)
}