package com.example.collega.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.collega.MakePostActivity
import com.example.collega.PhotoVideoView
import com.example.collega.R
import com.example.collega.data.PostData
import com.example.collega.databinding.StudyrvitemfileBinding
import com.example.collega.databinding.StudyrvitemphotoBinding
import com.example.collega.databinding.StudyrvitemvideoBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class PostAdapter(val context:Context, val itemList:ArrayList<PostData>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object
    {
        val VIDEO_LAYOUT = 1
        val IMAGE_LAY0UT = 2
        val PDF_file = 3
    }

    override fun getItemViewType(position: Int): Int {
       return if(itemList[position].fileType.equals("video", ignoreCase = true)){
            VIDEO_LAYOUT
        }else if (itemList[position].fileType.equals("image", ignoreCase = true)){
            IMAGE_LAY0UT
       } else {
           PDF_file
       }

    }
    inner class fileViewHolder(val binding:StudyrvitemfileBinding):RecyclerView.ViewHolder(binding.root)
    inner class videoViewHolder(val binding:StudyrvitemvideoBinding):RecyclerView.ViewHolder(binding.root)
    inner class imageViewHolder(val binding:StudyrvitemphotoBinding):RecyclerView.ViewHolder(binding.root)
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == VIDEO_LAYOUT){
            val binding = StudyrvitemvideoBinding.inflate(LayoutInflater.from(context),parent,false)
                videoViewHolder(binding)
        }else  if (viewType == IMAGE_LAY0UT){
            val binding = StudyrvitemphotoBinding.inflate(LayoutInflater.from(context),parent,false)
            imageViewHolder(binding)
        } else {
            val binding = StudyrvitemfileBinding.inflate(LayoutInflater.from(context),parent,false)
            fileViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        val userid = user?.uid
        val ref = FirebaseDatabase.getInstance().reference
        val postadmin= itemList[position].work
        if(holder is videoViewHolder){
            holder.binding.deleteicon.isVisible = false
            holder.binding.editbutton.isVisible = false

            if (userid==postadmin){
                holder.binding.deleteicon.isVisible = true
                holder.binding.editbutton.isVisible = true
                holder.binding.deleteicon.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure to Delete?")
                        .setIcon(R.drawable.logouticon)
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->

                            ref.child("posts").child(itemList[position].zone).child(itemList[position].notkey).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                                    notifyDataSetChanged()
                                    itemList.removeAt(position)
                                }
                        })
                        alertDialog.create()
                        alertDialog.show()
                }
                holder.binding.editbutton.setOnClickListener {
                    val intent = Intent(context,MakePostActivity::class.java)
                    intent.putExtra("title",itemList[position].title)
                    intent.putExtra("des",itemList[position].description)
                    intent.putExtra("notkey",itemList[position].notkey)
                    context.startActivity(intent)
                }
            }
            if (itemList[position].profilepic!="") {
                Picasso.get().load(itemList[position].profilepic).into(holder.binding.profilepic)
            }
            holder.binding.username.text = itemList[position].username
            holder.binding.title.text = itemList[position].title
            holder.binding.description.text = itemList[position].description
            val player = ExoPlayer.Builder(context).build()
            val playerView = holder.binding.video
            playerView.player = player

            val mediaItem = MediaItem.fromUri(itemList[position].picture)
            player.setMediaItem(mediaItem)
            player.videoScalingMode
            holder.binding.video.setOnClickListener {
                player.prepare()
                player.play()
            }
            holder.binding.exp.setOnClickListener {
                context.startActivity(Intent(context,PhotoVideoView::class.java).putExtra("vdo",itemList[position].picture))
            }


        } else if(holder is imageViewHolder) {
            val post = itemList[position]
            holder.binding.deleteicon.isVisible = false
            holder.binding.editbutton.isVisible = false

            var isExpand = false
            holder.binding.description.setOnClickListener {
                if (isExpand) {
                    holder.binding.description.maxLines = 4
                    isExpand = false
                } else {
                    holder.binding.description.maxLines = Int.MAX_VALUE
                    isExpand = true
                }
            }
            if (userid==postadmin){

                holder.binding.deleteicon.isVisible = true
                holder.binding.editbutton.isVisible = true
                holder.binding.deleteicon.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure to Delete?")
                        .setIcon(R.drawable.logouticon)
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            ref.child("posts").child(itemList[position].zone).child(itemList[position].notkey).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                                    notifyDataSetChanged()
                                    itemList.removeAt(position)
                                }
                        })
                    alertDialog.create()
                    alertDialog.show()
                }
                holder.binding.editbutton.setOnClickListener {
                    val intent = Intent(context,MakePostActivity::class.java)
                    intent.putExtra("title",itemList[position].title)
                    intent.putExtra("des",itemList[position].description)
                    intent.putExtra("notkey",itemList[position].notkey)
                    context.startActivity(intent)
                }
            }
            if(itemList[position].profilepic!=""){
            Picasso.get().load(itemList[position].profilepic).into(holder.binding.profilepic)
            }
            holder.binding.username.text = itemList[position].username
            holder.binding.title.text = itemList[position].title
            holder.binding.description.text = itemList[position].description
            if(itemList[position].picture!="") {
                Picasso.get().load(itemList[position].picture).into(holder.binding.picture)
                holder.binding.picture.setOnClickListener {
                    context.startActivity(Intent(context,PhotoVideoView::class.java).putExtra("img",itemList[position].picture))
                }
            }
        }else if(holder is fileViewHolder){
            holder.binding.deleteicon.isVisible = false
            holder.binding.editbutton.isVisible = false
            if (userid==postadmin){
                holder.binding.deleteicon.isVisible = true
                holder.binding.editbutton.isVisible = true
                holder.binding.deleteicon.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure to Delete?")
                        .setIcon(R.drawable.logouticon)
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            ref.child("posts").child(itemList[position].zone).child(itemList[position].notkey).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                                    notifyDataSetChanged()
                                    itemList.removeAt(position)
                                }
                        })
                    alertDialog.create()
                    alertDialog.show()
                }
                holder.binding.editbutton.setOnClickListener {
                    val intent = Intent(context,MakePostActivity::class.java)
                    intent.putExtra("title",itemList[position].title)
                    intent.putExtra("des",itemList[position].description)
                    intent.putExtra("notkey",itemList[position].notkey)
                    context.startActivity(intent)
                }
            }
            if(itemList[position].profilepic!=""){
                Picasso.get().load(itemList[position].profilepic).into(holder.binding.profilepic)
            }
            holder.binding.username.text = itemList[position].username
            holder.binding.title.text = itemList[position].title
            holder.binding.description.text = itemList[position].description
            holder.binding.picture.setImageResource(R.drawable.doc)
        }
    }
}