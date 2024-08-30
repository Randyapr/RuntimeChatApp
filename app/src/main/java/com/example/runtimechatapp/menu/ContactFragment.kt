package com.example.runtimechatapp.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtimechatapp.Adapter.UserAdapter
import com.example.runtimechatapp.R
import com.example.runtimechatapp.data.User
import com.example.runtimechatapp.ui.AddContactActivity
import com.example.runtimechatapp.ui.MessageActivity
import com.google.firebase.firestore.FirebaseFirestore

class ContactFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val contactList = mutableListOf<User>()
    private lateinit var adapter: UserAdapter
    private lateinit var searchView: SearchView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("ContactFragment", "onCreateView called")
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ContactFragment", "onViewCreated called")

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_contact)
        val btnAddContact: ImageButton = view.findViewById(R.id.btn_add_contact)

        // Set layout manager and adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = UserAdapter(contactList) { user ->
            val intent = Intent(activity, MessageActivity::class.java).apply {
                putExtra("USER_ID", user.userId)
                putExtra("USER_NAME", user.name)
                putExtra("USER_PROFILE_PICTURE", user.profile_picture)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        fetchContacts()

        btnAddContact.setOnClickListener {
            val intent = Intent(context, AddContactActivity::class.java)
            startActivity(intent)
        }
        searchView = activity?.findViewById(R.id.search_view)!!
        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterContacts(newText ?: "")
                return true
            }
        })
    }
    private fun filterContacts(query: String) {
        val filteredList = if (query.isEmpty()) {
            contactList
        } else {
            contactList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        adapter.updateList(filteredList)
    }

    private fun fetchContacts() {
        db.collection("users").get().addOnSuccessListener { documents ->
            Log.d("ContactFragment", "Data fetched: ${documents.size()} documents")
            contactList.clear()
            for (document in documents) {
                val user = document.toObject(User::class.java)
                contactList.add(user)
                Log.d("ContactFragment", "User added: $user") // Log each user added
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            Log.e("ContactFragment", "Error getting documents: ", exception)
        }
    }


    fun onSearchQuery(query: String) {
        filterContacts(query)
    }
}
