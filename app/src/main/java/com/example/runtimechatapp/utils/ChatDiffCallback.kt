package com.example.runtimechatapp.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.runtimechatapp.model.ChatListModel

class ChatDiffCallback(private val oldList: List<ChatListModel>, private val newList: List<ChatListModel>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uid == newList[newItemPosition].uid
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}