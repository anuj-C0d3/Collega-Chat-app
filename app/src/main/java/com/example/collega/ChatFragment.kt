package com.example.collega

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collega.adapters.ChatsAdapter
import com.example.collega.data.UserData
import com.example.collega.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ChatFragment : Fragment() {
    private lateinit var datalist: ArrayList<UserData>
    private lateinit var initlist: ArrayList<UserData>
    private lateinit var binding: FragmentChatBinding

    // Now always initialized in onCreateView
    private lateinit var adapter1: ChatsAdapter

    private lateinit var ref: DatabaseReference
    private lateinit var currentUser: String
    private var myprofile: UserData? = null

    private val tempUserList = mutableListOf<UserWithTimestamp>()
    private var totalUsers = 0
    private var processedUsers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ref = FirebaseDatabase.getInstance().reference
        currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        val context = requireContext()

        // 1. Prepare your data lists
        datalist = ArrayList()
        initlist = ArrayList()

        // 2. RecyclerView setup
        binding.recyclerview.layoutManager = LinearLayoutManager(context)

        // 3. Initialize the adapter *immediately* with an empty username
        //    so filterList() never crashes on adapter1.notifyDataSetChanged()
        adapter1 = ChatsAdapter(context, datalist, "")
        binding.recyclerview.adapter = adapter1

        // 4. Now load profiles & chats, and wire up search
        loadMyProfileAndUsers()
        setupSearch()

        // 5. Profile pic click
        binding.imageView.setOnClickListener {
            myprofile?.let {
                val intent = Intent(context, ProfilePage::class.java).apply {
                    putExtra("username", it.username)
                    putExtra("email", it.email)
                    putExtra("grad", it.graduation)
                    putExtra("address", it.address)
                    putExtra("tagline", it.tagline)
                    putExtra("work", it.work)
                    putExtra("college", it.college)
                    putExtra("profilepic", it.profilePic)
                    putExtra("uid", it.userId)
                }
                startActivity(intent)
            }
        }

        return binding.root
    }

    private fun loadMyProfileAndUsers() {
        ref.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalUsers = snapshot.childrenCount.toInt()
                processedUsers = 0

                for (snap in snapshot.children) {
                    val user = snap.getValue(UserData::class.java) ?: continue

                    if (user.userId == currentUser) {
                        // Save your own profile
                        myprofile = user
                        if (!user.profilePic.isNullOrEmpty()) {
                            Picasso.get().load(user.profilePic).into(binding.imageView)
                        }
                        binding.admintext.isVisible = user.work.equals("Admin", true)

                        // **Now that we know your username**, re-create adapter with it:
                        adapter1 = ChatsAdapter(requireContext(), datalist, user.username)
                        binding.recyclerview.adapter = adapter1

                        checkIfReadyToShow()
                    } else {
                        getLastMessageTimestamp(user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getLastMessageTimestamp(user: UserData) {
        // Prevent duplicates
        if (tempUserList.any { it.userData.userId == user.userId }) return

        ref.child("chats")
            .child(currentUser)
            .child(user.userId)
            .orderByChild("timestamp")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lastTimestamp = 0L
                    for (msgSnap in snapshot.children) {
                        lastTimestamp = msgSnap.child("timestamp").getValue(Long::class.java) ?: 0
                    }
                    // Add only if still not present
                    if (tempUserList.none { it.userData.userId == user.userId }) {
                        tempUserList.add(UserWithTimestamp(user, lastTimestamp))
                    }
                    processedUsers++
                    checkIfReadyToShow()
                }

                override fun onCancelled(error: DatabaseError) {
                    processedUsers++
                    checkIfReadyToShow()
                }
            })
    }

    private fun checkIfReadyToShow() {
        // Wait until you have your own profile AND all other users processed
        if (myprofile != null && processedUsers == totalUsers - 1) {
            // Sort & dedupe
            val sorted = tempUserList
                .distinctBy { it.userData.userId }
                .sortedByDescending { it.lastMessageTimestamp }

            datalist.clear()
            datalist.addAll(sorted.map { it.userData })

            initlist.clear()
            initlist.addAll(datalist)

            // Re-create adapter with final data + your username
            adapter1 = ChatsAdapter(requireContext(), datalist, myprofile!!.username)
            binding.recyclerview.adapter = adapter1
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { filterList(query) }
            override fun onQueryTextChange(newText: String?) = true.also { filterList(newText) }
        })
    }

    private fun filterList(query: String?) {
        datalist.clear()
        if (query.isNullOrBlank()) {
            datalist.addAll(initlist)
        } else {
            datalist.addAll(initlist.filter {
                it.username.contains(query, ignoreCase = true)
            })
        }
        adapter1.notifyDataSetChanged()
    }

    data class UserWithTimestamp(val userData: UserData, val lastMessageTimestamp: Long)
}
