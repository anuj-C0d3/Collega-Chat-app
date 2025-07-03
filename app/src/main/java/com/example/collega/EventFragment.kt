package com.example.collega

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.collega.adapters.PostAdapter
import com.example.collega.data.PostData
import com.example.collega.data.UserData
import com.example.collega.databinding.ActivityMainBinding
import com.example.collega.databinding.FragmentEventBinding
import com.example.collega.databinding.FragmentGameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EventFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val  binding = FragmentEventBinding.inflate(inflater,container,false)
        val context = container!!.context
        val rv = binding.eventrv

        rv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 5) {
                    val mainActivity = activity as MainActivity
                    mainActivity.hideBottomNavigation()
                } else {
                    val mainActivity = activity as MainActivity
                    mainActivity.showBottomNavigation()
                }
            }
        })
        rv.layoutManager = LinearLayoutManager(context)
        val datalist = ArrayList<PostData>()
        val adapter = PostAdapter(context,datalist)
        val user = FirebaseAuth.getInstance().currentUser
        var userinfo = UserData()
        var empty = true
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("users").child(user!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(UserData::class.java)
                value?.let {
                    userinfo = it
                    if(it.work=="admin" || it.work == "Admin"){

                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
        ref.child("posts").child("Event").addValueEventListener(object: ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    val value = snap.getValue(PostData::class.java)
                    value?.let {
                        if(!datalist.contains(it)){
                            datalist.add(it)
                            empty = false
                        } 
                    } 
                }
                datalist.reverse()
                rv.adapter = adapter
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
        binding.nopost.isVisible = false
//        if (datalist.isEmpty()){
//            binding.nopost.isVisible = true
//        }
        
        return binding.root
    }


}