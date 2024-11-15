package com.example.runtimechatapp.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runtimechatapp.Adapter.ContactAdapter
import com.example.runtimechatapp.databinding.FragmentContactBinding
import com.example.runtimechatapp.model.UserModel
import com.example.runtimechatapp.ui.AddContactActivity
import com.example.runtimechatapp.ui.MessageActivity
import com.example.runtimechatapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: ContactAdapter
    private val contactList = mutableListOf<UserModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnAddContact.setOnClickListener {
            val intent = Intent(requireContext(), AddContactActivity::class.java)
            startActivity(intent)
        }

        binding.rvContact.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContactAdapter(contactList) { contact ->
            val intent = Intent(requireContext(), MessageActivity::class.java)
            intent.putExtra("contactUid", contact.uid)
            startActivity(intent)
        }
        binding.rvContact.adapter = adapter

        getContactList()
    }

    private fun getContactList() {
        val currentUserId = auth.currentUser?.uid ?: return

        val contactsRef = database.reference.child(Config.CONTACTS).child(currentUserId)

        contactsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactList.clear()

                for (contactSnapshot in snapshot.children) {
                    val contactUid = contactSnapshot.key

                    if (contactList.any { it.uid == contactUid }) continue

                    database.reference.child(Config.USERS).child(contactUid!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val contact = userSnapshot.getValue(UserModel::class.java)
                                if (contact != null) {
                                    contactList.add(contact)
                                    adapter.notifyItemInserted(contactList.size - 1)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ContactFragment", "Error getting contact details: ${error.message}")
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ContactFragment", "Error getting contact list: ${error.message}")
            }
        })
    }

    fun onSearchQuery(query: String) {
        val filteredList = contactList.filter { contact ->
            contact.name?.lowercase()?.contains(query.lowercase()) == true ||
                    contact.phone?.lowercase()?.contains(query.lowercase()) == true
        }
        adapter.updateList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
