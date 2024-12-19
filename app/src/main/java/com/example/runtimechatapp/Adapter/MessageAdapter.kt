package com.example.runtimechatapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.runtimechatapp.databinding.ItemMessageLeftBinding
import com.example.runtimechatapp.databinding.ItemMessageRightBinding
import com.example.runtimechatapp.model.MessageModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val messageList: List<MessageModel>,
    private val senderUid: String,
    private val onLongClick: (MessageModel, Int) -> Unit
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

        when (holder) {
            is MessageRightViewHolder -> {
                holder.binding.apply {
                    pesanKanan.text = message.message
                    timeView.text = getFormattedTime(message.timestamp)

                    // Setup long click for delete
                    root.setOnLongClickListener {
                        onLongClick(message, position)
                        true
                    }
                }
            }
            is MessageLeftViewHolder -> {
                holder.binding.apply {
                    pesanKiri.text = message.message
                    timeKiri.text = getFormattedTime(message.timestamp)
                }
            }
        }
    }

    private fun getFormattedTime(timestamp: String?): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(timestamp?.toLong() ?: 0)
            sdf.format(date)
        } catch (e: Exception) {
            timestamp ?: ""
        }
    }

    override fun getItemCount(): Int = messageList.size

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