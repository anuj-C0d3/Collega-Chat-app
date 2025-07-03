package com.example.collega

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collega.adapters.ChatSectionAdapter
import com.example.collega.data.messageschema
import com.example.collega.databinding.ActivityChatSectionBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import render.animations.Bounce
import render.animations.Render
import java.io.IOException
import java.util.*

class ChatSection : AppCompatActivity() {
    private lateinit var binding: ActivityChatSectionBinding
    private lateinit var messages: ArrayList<messageschema>
    private lateinit var adapter: ChatSectionAdapter
    private lateinit var seenListener: ValueEventListener
    private lateinit var chatRoom: String
    private lateinit var senderrm: String
    private val VIDEO_PICKER_CODE = 1001
    private val ref = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatSectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uploadcard.isVisible = false
        val render = Render(this)
        binding.uploadprog.isVisible = false

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

        val sendername = intent.getStringExtra("sendername")
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val grad = intent.getStringExtra("grad")
        val address = intent.getStringExtra("address")
        val tagline = intent.getStringExtra("tagline")
        val work = intent.getStringExtra("work")
        val college = intent.getStringExtra("college")
        val uid = intent.getStringExtra("uid") ?: return

        binding.imagepick.setOnClickListener {
            ImagePicker.with(this)
                .provider(ImageProvider.BOTH)
                .createIntent {
                    LauncherImage.launch(it)
                }
            binding.uploadcard.isVisible = false
        }

        val profilePic = intent.getStringExtra("profilepic")
        if (profilePic != "") {
            Picasso.get().load(profilePic).into(binding.profilepic)
        } else {
            binding.profilepic.setImageResource(R.drawable.defaultuserimg)
        }

        binding.profilepic.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("grad", grad)
            intent.putExtra("address", address)
            intent.putExtra("tagline", tagline)
            intent.putExtra("work", work)
            intent.putExtra("college", college)
            intent.putExtra("profilepic", profilePic)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        binding.usernamereceiver.text = username

        val receiverId = uid
        val senderId = FirebaseAuth.getInstance().currentUser!!.uid
        senderrm = senderId + receiverId
        chatRoom = receiverId + senderId

        messages = ArrayList()
        adapter = ChatSectionAdapter(this, messages)
        val recyclerView = binding.chatsectionrv
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(adapter.itemCount - 1)

        ref.child("chats").child(senderrm).addValueEventListener(object : ValueEventListener {
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
                val msgId = ref.child("chats").child(senderrm).push().key ?: return@setOnClickListener
                val timestamp = Date().time
                val messageSchema = messageschema(
                    msgId,
                    senderId,
                    receiverId,
                    msg,
                    timestamp,
                    "false",
                    "String"
                )

                ref.child("chats").child(senderrm).child(msgId).setValue(messageSchema)
                    .addOnSuccessListener {
                        ref.child("chats").child(chatRoom).child(msgId).setValue(messageSchema)

                        // ✅ Send FCM notification ONLY if receiver is NOT the sender
                        if (receiverId != senderId) {
                            ref.child("usertokens").child(receiverId).child("token")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val token = snapshot.getValue(String::class.java) ?: return
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val accessToken = getAccessToken()
                                            if (accessToken != null) {
                                                sendNotificationToUser(
                                                    sendername ?: "New Message",
                                                    accessToken,
                                                    token,
                                                    msg,
                                                    receiverId,
                                                    senderId
                                                )
                                            } else {
                                                Log.e("Error", "Failed to fetch Access Token")
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
            }
        }

        markMessagesAsSeen()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ref.child("chats").child(chatRoom).removeEventListener(seenListener)
                finish()
            }
        })
    }

    private fun markMessagesAsSeen() {
        seenListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val message = snap.getValue(messageschema::class.java)
                    if (message != null && message.read == "false" &&
                        message.receiverId == FirebaseAuth.getInstance().currentUser!!.uid
                    ) {
                        ref.child("chats").child(chatRoom).child(message.messageId).child("read")
                            .setValue("true")
                        ref.child("chats").child(senderrm).child(message.messageId).child("read")
                            .setValue("true")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        ref.child("chats").child(chatRoom).addValueEventListener(seenListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val userid = FirebaseAuth.getInstance().currentUser?.uid
        Toast.makeText(this, "Don't press back, it will take some time", Toast.LENGTH_SHORT).show()

        if (requestCode == VIDEO_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            val datauri = data?.data
            if (datauri != null) {
                val ref =
                    Firebase.storage.reference.child("videos/" + userid + System.currentTimeMillis())
                ref.putFile(datauri)
                    .addOnProgressListener {
                        binding.uploadprog.isVisible = true
                    }
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
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, "Don't press back, it will take some time", Toast.LENGTH_SHORT).show()
                currentUser?.let { user ->
                    val ref = Firebase.storage.reference.child("pictures/" + user.uid + System.currentTimeMillis())
                    ref.putFile(it.data!!.data!!)
                        .addOnProgressListener {
                            binding.uploadprog.isVisible = true
                        }
                        .addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener {
                                binding.messageField.setText(it.toString())
                                binding.uploadprog.isVisible = false
                            }
                        }
                }
            }
        }

    suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
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

    fun sendNotificationToUser(
        senderId: String,
        accessToken: String,
        userToken: String,
        message: String,
        receiverId: String,
        myId: String
    ) {
        val url = "https://fcm.googleapis.com/v1/projects/collega-45945/messages:send"
        val json = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", userToken)
                put("notification", JSONObject().apply {
                    put("title", senderId)
                    put("body", message)
                })
            })
        }

        val client = OkHttpClient()
        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {}
        })
    }
}
