package com.example.collega

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collega.adapters.GroupAdapter
import com.example.collega.data.GroupData
import com.example.collega.databinding.FragmentGroupChatsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupChats : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context  = container!!.context
        val binding = FragmentGroupChatsBinding.inflate(inflater,container,false)
        val rv = binding.rv
        rv.layoutManager = LinearLayoutManager(context)
        val datalist = ArrayList<GroupData>()
        val adapter = GroupAdapter(context,datalist)
        rv.adapter=  adapter
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("groups").addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                datalist.clear()
                for(snap in snapshot.children){
                    val value = snap.getValue(GroupData::class.java)
                    value?.let {
                        if(!datalist.contains(it)){
                            datalist.reverse()
                            datalist.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
        return binding.root
    }
}