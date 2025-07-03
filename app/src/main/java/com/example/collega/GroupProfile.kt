package com.example.collega

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collega.adapters.MemberAdapter
import com.example.collega.data.GroupData
import com.example.collega.data.UserData
import com.example.collega.databinding.ActivityGroupProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso

class GroupProfile : AppCompatActivity() {
    private lateinit var binding:ActivityGroupProfileBinding
    var  profileurl =""
    var groupId :String?= ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGroupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val ref = FirebaseDatabase.getInstance().reference

        binding.grpimg.setOnClickListener {
            ImagePicker.with(this)
                .compress(500)
                .provider(ImageProvider.BOTH)
                .cropSquare()
                .createIntent {
                    LauncherImage.launch(it)
                }
        }
        val id = intent.getStringExtra("groupid")
     groupId = id
        val userid = intent.getStringExtra("userid")
        val user = FirebaseAuth.getInstance().currentUser
        val des = intent.getStringExtra("des")
        val groupname = intent.getStringExtra("groupname")
        val prof = intent.getStringExtra("grouprofile")
        binding.grouprof.setText(des)
        binding.grpname.setText(groupname)
        if(prof !=""){
            Picasso.get().load(prof).into(binding.grpimg)
        }else {
            binding.grpimg.setImageResource(R.drawable.defaultuserimg)
        }
        val memberlist = ArrayList<UserData>()
        val memberrv = binding.memberrv
        val adapter = MemberAdapter(this,memberlist)
        memberrv.layoutManager = LinearLayoutManager(this)
        memberrv.adapter = adapter
        ref.child("groupmembers").child(groupId!!).child("members").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    val value = snap.getValue(String::class.java)
                    value?.let {
                        ref.child("users").child(it).addValueEventListener(object:ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val value = snapshot.getValue(UserData::class.java)
                                value?.let {
                                    memberlist.add(it)
                                   adapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

if (userid==user?.uid){
    Toast.makeText(this, "Equal", Toast.LENGTH_SHORT).show()
    binding.creategrp.isVisible = true
    binding.cre.isVisible= true
    binding.join.isVisible  = false
}else {
    binding.join.isVisible = true
    Toast.makeText(this, "Not equal", Toast.LENGTH_SHORT).show()
    binding.creategrp.isVisible = false
    binding.cre.isVisible = false

}
        binding.join.setOnClickListener {
            ref.child("groupmembers").child(groupId!!).child("members").child(user!!.uid).setValue(user.uid).addOnSuccessListener {
                Toast.makeText(this, "Joined", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,MainActivity::class.java))
            }
        }
        binding.creategrp.setOnClickListener {
            val grpname = binding.grpname.text.toString()
           groupId =   ref.child("groups").push().key!!
            val group = GroupData(grpname,groupId!!,profileurl,des!!,userid!!)
            ref.child("groups").child(groupId!!).setValue(group)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    val LauncherImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data != null) {
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    currentUser?.let { user ->

                        val ref = Firebase.storage.reference.child("profilepic/" + groupId)
                        ref.putFile(it.data!!.data!!)
                            .addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener {
                                    profileurl = it.toString()
                                    Picasso.get().load(it.toString()).into(binding.grpimg)
                                }
                            }
                    }
                }
            }
        }
}