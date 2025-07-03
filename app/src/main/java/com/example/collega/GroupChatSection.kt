package com.example.collega

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collega.adapters.ChatSectionAdapter
import com.example.collega.data.messageschema
import com.example.collega.databinding.ActivityGroupChatSectionBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import render.animations.Bounce
import render.animations.Render
import java.io.IOException
import java.util.Date

class GroupChatSection : AppCompatActivity() {
    private lateinit var binding:ActivityGroupChatSectionBinding
    private lateinit var messages: ArrayList<messageschema>
    private lateinit var adapter: ChatSectionAdapter
val VIDEO_PICKER_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupChatSectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.uploadcard.isVisible = false
        val render = Render(this)
        binding.uploadprog.isVisible = false
        val ref= FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser
        binding.attachmenticon.setOnClickListener {
            if (!binding.uploadcard.isVisible) {
                binding.uploadcard.isVisible = true
                render.setAnimation(Bounce().InUp(binding.uploadcard))

                render.setDuration(200)
                render.start()
            } else {
                binding.uploadcard.isVisible = false
            }
        }

        binding.videopicl.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            binding.uploadcard.isVisible = false
            intent.type = "video/*"
            startActivityForResult(intent, VIDEO_PICKER_CODE)
        }
        binding.imagepick.setOnClickListener {
            ImagePicker.with(this)

                .provider(ImageProvider.BOTH)
                .createIntent {
                    LauncherImage.launch(it)
                }
            binding.uploadcard.isVisible = false
        }
        val profile = intent.getStringExtra("grouprofile")
        val grpname = intent.getStringExtra("groupname")
        val admin = intent.getStringExtra("userid")
        val groupid = intent.getStringExtra("groupid")
        val userid = intent.getStringExtra("uid")
        val members = ArrayList<String>()
        ref.child("groupmembers").child(groupid!!).child("members").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children){
                    val value = snap.getValue(String::class.java)
                    value?.let {
                        members.add(it)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        binding.usernamereceiver.text = grpname
        if(profile!=null) {
            Picasso.get().load(profile).into(binding.profilepic)

        }else {
            binding.profilepic.setImageResource(R.drawable.defaultuserimg)
        }
        messages = ArrayList()
        adapter = ChatSectionAdapter(this, messages)
        val recyclerView = binding.chatsectionrv
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(adapter.itemCount - 1)
        ref.child("groupschats").child(groupid!!).child("messages").addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (snap in snapshot.children) {
                    val msgModel = snap.getValue(messageschema::class.java)
                    msgModel?.let { messages.add(it) }
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        binding.send.setOnClickListener {
            val msg = binding.messageField.text.toString().trim()
            if (msg.isNotEmpty()) {
                binding.messageField.setText("")
                val msgId =
                    ref.child("groupschats").child(groupid!!).child("messages").push().key ?: return@setOnClickListener
                val timestamp = Date().time
                val messageSchema = messageschema(
                    msgId,
                    userid!!,
                    "",
                    msg,
                    timestamp,
                    "false",
                    "String"
                )
                ref.child("groupschats").child(groupid).child("messages").child(msgId).setValue(messageSchema)
                    .addOnSuccessListener {
                        var token = ""
                members.forEachIndexed { index, s ->
                    ref.child("usertokens").child(s).child("token").addListenerForSingleValueEvent(object :
                            ValueEventListener
                        {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                token = snapshot.getValue(String::class.java)!!
                                if(token!=null){
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val accessToken = getAccessToken()
                                        if (accessToken != null) {
                                            sendNotificationToUser(grpname!!,accessToken, token, msg)
                                        } else {
                                            Log.e("Error", "Failed to fetch Access Token")
                                        }
                                    }
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })


                    }
                }

//
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {  // Runs in Background Thread
            try {
                val inputStream = applicationContext.assets.open("service_account.json")
                val credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
                credentials.refreshIfExpired()
                credentials.accessToken.tokenValue
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    fun sendNotificationToUser(senderid:String,accessToken:String,userToken:String,message:String){
        val url = "https://fcm.googleapis.com/v1/projects/collega-45945/messages:send"
        val json  = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token",userToken)
                put("notification", JSONObject().apply {
                    put("title",senderid)
                    put("body",message)
                })
            })
        }
        val client = OkHttpClient()
        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization","Bearer $accessToken")
            .addHeader("Content-Type","application/json")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.uploadprog.isVisible = true
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        Toast.makeText(this, "Dont press back it will take some time", Toast.LENGTH_SHORT).show()
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            val datauri = data?.data
            if (datauri != null) {
                val ref =
                    Firebase.storage.reference.child("videos/" + userid + System.currentTimeMillis())
                ref.putFile(datauri)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            binding.messageField.setText(it.toString())
                            binding.uploadprog.isVisible = false
                        }
                    }
            }
        }

    }
    val LauncherImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data != null) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    binding.uploadprog.isVisible = true
                    Toast.makeText(this, "Don't press back it will take time!", Toast.LENGTH_SHORT).show()
                    currentUser?.let { user ->

                        val ref = Firebase.storage.reference.child("pictures/" +user?.uid+System.currentTimeMillis())
                        ref.putFile(it.data!!.data!!)
                            .addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener {
                                    binding.messageField.setText(it.toString())
                                    binding.uploadprog.isVisible = false
                                }
                            }
                    }
                }
            }

        }

}