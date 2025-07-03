package com.example.collega.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.collega.GroupChatSection
import com.example.collega.GroupProfile
import com.example.collega.R
import com.example.collega.data.GroupData
import com.example.collega.databinding.ChatrvitemviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class GroupAdapter(val context: Context, val datalist: ArrayList<GroupData>) :
    RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ChatrvitemviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChatrvitemviewBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference
        val group = datalist[position]

        // Set profile image
        if (group.groupProfile.isNotEmpty()) {
            Picasso.get().load(group.groupProfile).into(holder.binding.profilepicture)
        } else {
            holder.binding.profilepicture.setImageResource(R.drawable.defaultuserimg)
        }

        // Set group name
        holder.binding.username.text = group.groupname

        // Set profile picture click -> open group profile
        holder.binding.profilepicture.setOnClickListener {
            val intent = Intent(context, GroupProfile::class.java).apply {
                putExtra("groupname", group.groupname)
                putExtra("grouprofile", group.groupProfile)
                putExtra("userid", group.admin)
                putExtra("groupid", group.groupId)
                putExtra("des", group.des)
            }
            context.startActivity(intent)
        }

        // Set tagline as last message or group description
        val messagesRef = ref.child("groupschats").child(group.groupId).child("messages")
        messagesRef.orderByKey().limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val lastMessage = snap.child("message").getValue(String::class.java)
                            holder.binding.tagline.text = lastMessage ?: group.des.take(30)
                        }
                    } else {
                        holder.binding.tagline.text = group.des.take(30)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.binding.tagline.text = group.des.take(30)
                }
            })

        // Check if user is a member of the group
        var isMember = false
        val membersRef = ref.child("groupmembers").child(group.groupId).child("members")
        membersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val memberId = snap.getValue(String::class.java)
                    if (memberId == user?.uid) {
                        isMember = true
                        break
                    }
                }
                holder.binding.textView18.text = if (isMember) "Member" else ""
                holder.binding.textView18.isVisible = isMember
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Handle item click for group chat
        holder.itemView.setOnClickListener {
            if (user == null) {
                Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(context, GroupChatSection::class.java).apply {
                putExtra("groupname", group.groupname)
                putExtra("groupid", group.groupId)
                putExtra("grouprofile", group.groupProfile)
                putExtra("userid", group.admin)
                putExtra("uid", user.uid)
                putExtra("des", group.des)
            }

            if (user.uid == group.admin || isMember) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "You are not the member of this group.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = datalist.size
}
