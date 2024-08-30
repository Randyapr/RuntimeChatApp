package com.example.runtimechatapp.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtimechatapp.R
import com.example.runtimechatapp.data.User
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private var userList: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tv_contact)
        val bioTextView: TextView = itemView.findViewById(R.id.tv_bio)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.iv_contact)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(userList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.nameTextView.text = user.name
        holder.bioTextView.text = user.bio
        if (user.profile_picture.isNullOrEmpty()) {
            // Jika kosong, gunakan gambar default
            Glide.with(holder.itemView.context)
                .load(R.drawable.circle_bg)
                .into(holder.profileImageView)
        } else {
            // Jika tidak kosong, load gambar dari URL
            Glide.with(holder.itemView.context)
                .load(user.profile_picture)
                .into(holder.profileImageView)
        }

            Log.d("UserAdapter", "Binding user: $user") // Log user data being bound
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}