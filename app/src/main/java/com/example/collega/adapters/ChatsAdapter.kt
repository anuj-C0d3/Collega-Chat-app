package com.example.collega.adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.collega.ChatSection
import com.example.collega.ProfilePage
import com.example.collega.data.UserData
import com.example.collega.databinding.ChatrvitemviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

class ChatsAdapter(val context: Context,val UserList:ArrayList<UserData>,val sendername:String):RecyclerView.Adapter<ChatsAdapter.viewHolder>() {
   inner class viewHolder(val binding:ChatrvitemviewBinding):RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsAdapter.viewHolder {
       val binding = ChatrvitemviewBinding.inflate(LayoutInflater.from(context),parent,false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatsAdapter.viewHolder, position: Int) {
       holder.binding.username.text = UserList[position].username
        val senderid= FirebaseAuth.getInstance().currentUser?.uid
        val receiverid = UserList[position].userId
        val room = senderid + receiverid
        val ref =  FirebaseDatabase.getInstance().reference
        ref.child("chats").child(room)
            .orderByChild("timestamp")
            .limitToLast(1)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snap in snapshot.children){
                        val lastmessg = snap.child("message").getValue().toString()
                        val seen = snap.child("read").getValue().toString()
                        val timemiles:Long = snap.child("timestamp").getValue(Long::class.java)!!
                        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val date = Date(timemiles)
                        val formattime = sdf.format(date)
                        holder.binding.textView18.text = formattime
                        if(lastmessg.length>40){
                            holder.binding.tagline.text = lastmessg.substring(0,40)
                        } else {
                            holder.binding.tagline.text = lastmessg
                        }
                        if (seen.equals("false")) {
                            holder.binding.textView18.setTypeface(null, android.graphics.Typeface.BOLD)
                            holder.binding.tagline.setTypeface(null, android.graphics.Typeface.BOLD)
                        } else {
                            holder.binding.textView18.setTypeface(null, android.graphics.Typeface.NORMAL)
                            holder.binding.tagline.setTypeface(null, android.graphics.Typeface.NORMAL)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        if (UserList[position].profilePic!=""){
            Picasso.get().load(UserList[position].profilePic).into(holder.binding.profilepicture)
        }
        holder.binding.profilepicture.setOnClickListener {
            val intent = Intent(context,ProfilePage::class.java)
            intent.putExtra("username",UserList[position].username)
            intent.putExtra("email",UserList[position].email)
            intent.putExtra("grad",UserList[position].graduation)
            intent.putExtra("address",UserList[position].address)
            intent.putExtra("tagline",UserList[position].tagline)
            intent.putExtra("work",UserList[position].work)
            intent.putExtra("college",UserList[position].college)
            intent.putExtra("profilepic",UserList[position].profilePic)
            intent.putExtra("uid",UserList[position].userId)
            context.startActivity(intent)
        }
        holder.itemView.setOnClickListener {

            val intent = Intent(context,ChatSection::class.java)
            intent.putExtra("sendername",sendername)
            intent.putExtra("username",UserList[position].username)
            intent.putExtra("email",UserList[position].email)
            intent.putExtra("grad",UserList[position].graduation)
            intent.putExtra("address",UserList[position].address)
            intent.putExtra("tagline",UserList[position].tagline)
            intent.putExtra("work",UserList[position].work)
            intent.putExtra("college",UserList[position].college)
            intent.putExtra("profilepic",UserList[position].profilePic)
            intent.putExtra("uid",UserList[position].userId)
            context.startActivity(intent)
        }
//        holder.itemView.setOnLongClickListener {
//            val alertdialog = AlertDialog.Builder(context)
//                .setTitle("Delete")
//                .setMessage("All your chats will be deleted")
//                .setPositiveButton("Yes",{Dialog,which->
//                    FirebaseDatabase.getInstance().reference.child("chats").
//                })
//            true
//
//        }

    }

    override fun getItemCount(): Int {
        return UserList.size
    }


}