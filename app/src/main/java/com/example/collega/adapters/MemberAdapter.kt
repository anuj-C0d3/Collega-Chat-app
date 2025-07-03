package com.example.collega.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collega.data.UserData
import com.example.collega.databinding.MemberrvitemBinding
import com.squareup.picasso.Picasso

class MemberAdapter(val context: Context,val memberlist:ArrayList<UserData>) :RecyclerView.Adapter<MemberAdapter.ViewHolder>(){
    inner class ViewHolder(val binding: MemberrvitemBinding):RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberAdapter.ViewHolder {
        val binding = MemberrvitemBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberAdapter.ViewHolder, position: Int) {
        if(memberlist[position].profilePic!=""){
        Picasso.get().load(memberlist[position].profilePic).into(holder.binding.profilepicture)
        }
        holder.binding.username.text = memberlist[position].username
    }

    override fun getItemCount(): Int {
        return memberlist.size
    }
}