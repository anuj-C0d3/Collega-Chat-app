package com.example.collega.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.collega.data.messageschema
import com.example.collega.databinding.SampleReceiverBinding
import com.example.collega.databinding.SampleSenderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatSectionAdapter(private val context: Context, private val messages: ArrayList<messageschema>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
val senderid = FirebaseAuth.getInstance().currentUser!!.uid

    // ViewHolder for Sender Messages
    inner class SenderViewHolder(val binding: SampleSenderBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ViewHolder for Receiver Messages
    inner class ReceiverViewHolder(val binding: SampleReceiverBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val VIEW_TYPE_SENDER = 1
        private const val VIEW_TYPE_RECEIVER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].uid == FirebaseAuth.getInstance().currentUser?.uid) {
            VIEW_TYPE_SENDER
        } else {
            VIEW_TYPE_RECEIVER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
            val binding = SampleSenderBinding.inflate(LayoutInflater.from(context), parent, false)
            SenderViewHolder(binding)
        } else {
            val binding = SampleReceiverBinding.inflate(LayoutInflater.from(context), parent, false)
            ReceiverViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = messages.size
    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val timmiles = message.timestamp
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date =  Date(timmiles)
        val formattime = sdf.format(date)

        if (holder is SenderViewHolder) {
            var isExpand = false
            holder.binding.sendertext.setOnClickListener {
                if (isExpand) {
                    holder.binding.sendertext.maxLines = 4
                    isExpand = false
                } else {
                    holder.binding.sendertext.maxLines = Int.MAX_VALUE
                    isExpand = true
                }
            }
        if(message.read=="true"){
                holder.binding.seenicon.isVisible = true
            } else holder.binding.seenicon.isVisible = false
            holder.binding.sendertext.text = message.message
            holder.binding.sendertime.text = formattime
            holder.itemView.setOnLongClickListener {
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("Are you sure to Delete?")
                    .setPositiveButton("Yes",{Dialog,which->
                        FirebaseDatabase.getInstance().reference.child("chats").
                        child(senderid+messages[position].receiverId).
                        child(messages[position].messageId).removeValue().addOnSuccessListener {
                            messages.removeAt(position)
                            notifyDataSetChanged()
                        }
                    })
                    .setNegativeButton("No",{Dialog,which->
                        Dialog.dismiss()
                    })
                alertDialog.create()
                alertDialog.show()

                true
            }
        } else if (holder is ReceiverViewHolder) {
            holder.binding.receivertext.text = message.message
            holder.binding.receivertime.text = formattime
            var isExpand = false
            holder.binding.receivertext.setOnClickListener {
                if (isExpand) {
                    holder.binding.receivertext.maxLines = 4
                    isExpand = false
                } else {
                    holder.binding.receivertext.maxLines = Int.MAX_VALUE
                    isExpand = true
                }
            }
        }
    }
}
