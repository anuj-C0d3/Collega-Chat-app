package com.example.collega

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collega.adapters.PostAdapter
import com.example.collega.data.PostData
import com.example.collega.databinding.ActivityAdminPanelBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class AdminPanel : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPanelBinding
    private lateinit var datalist: ArrayList<PostData>
    private lateinit var adapter: PostAdapter
    private lateinit var ref: DatabaseReference
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ref = FirebaseDatabase.getInstance().reference
        datalist = ArrayList()
        adapter = PostAdapter(this, datalist)

        binding.yourpostrv.layoutManager = LinearLayoutManager(this)
        binding.yourpostrv.adapter = adapter

        // Set profile picture
        val profilepic = intent.getStringExtra("profilepic")
        if (!profilepic.isNullOrEmpty()) {
            Picasso.get().load(profilepic).into(binding.profilepic)
        } else {
            binding.profilepic.setImageResource(R.drawable.defaultuserimg)
        }

        // Buttons for post and group creation
        binding.createGrp.setOnClickListener {
            startActivity(Intent(this, GroupCreateActivity::class.java))
        }
        binding.createPost.setOnClickListener {
            startActivity(Intent(this, MakePostActivity::class.java))
        }

        // Load posts from all categories
        loadPosts()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // ✅ Correctly loads all posts from all categories
    private fun loadPosts() {
        val postCategories = listOf("Event", "Study", "Game")
        val tempList = ArrayList<PostData>()

        var processedCategories = 0
        postCategories.forEach { category ->
            ref.child("posts").child(category).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val value = snap.getValue(PostData::class.java)
                        value?.let {
                            if (it.work == user?.uid) {
                                tempList.add(it)
                            }
                        }
                    }
                    processedCategories++

                    // ✅ Only update UI when all categories are processed
                    if (processedCategories == postCategories.size) {
                        updatePostList(tempList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // ✅ Updates RecyclerView safely
    @SuppressLint("NotifyDataSetChanged")
    private fun updatePostList(tempList: List<PostData>) {
        datalist.clear()
        datalist.addAll(tempList) // Sort by latest post
        adapter.notifyDataSetChanged()

        // ✅ Show/hide "No Posts" message correctly
        binding.nopost.isVisible = datalist.isEmpty()
    }
}
