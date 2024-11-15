package com.example.runtimechatapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.runtimechatapp.R
import com.example.runtimechatapp.databinding.ItemContactBinding
import com.example.runtimechatapp.model.UserModel

class ContactAdapter(
    private var contactList: List<UserModel>,
    private val onContactClick: (UserModel) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding =
            ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]

        holder.binding.tvContact.text = contact.name
        holder.binding.tvBio.text = contact.phone

        // Load gambar profil jika ada
        Glide.with(holder.itemView.context)
            .load(contact.profileImage)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.circle_bg)
                    .error(R.drawable.circle_bg)
            )
            .into(holder.binding.ivContact)


        // Set onClickListener untuk item kontak
        holder.itemView.setOnClickListener {
            onContactClick(contact) // Panggil lambda function
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    fun updateList(filteredList: List<UserModel>) {
        contactList = filteredList
        notifyDataSetChanged()
    }
}