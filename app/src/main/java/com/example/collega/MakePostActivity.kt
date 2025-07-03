package com.example.collega

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.withStarted
import com.example.collega.data.PostData
import com.example.collega.data.UserData
import com.example.collega.databinding.ActivityMakePostBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
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

class MakePostActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMakePostBinding
    var VIDEO_PICKER_CODE = 1001
    var FILE_PICKER_REQUEST = 1002
    private lateinit var urid :String
    private lateinit var filetype:String
    private lateinit var zone:String
    private lateinit var notkey:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMakePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val render = Render(this)
        urid = ""
        filetype ="pdf"
        zone = ""
        notkey = ""
        binding.titlete.setText(intent.getStringExtra("title"))
        binding.descrip.setText(intent.getStringExtra("des"))
        val user =FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference
        binding.postprogress.isVisible = false
        binding.postprogressper.isVisible = false
        binding.uploadcard.isVisible = false
        binding.postVideo.isVisible = false
        binding.postimg.isVisible = false
        binding.imagepickicon.setOnClickListener {
            if (!binding.uploadcard.isVisible) {
                binding.uploadcard.isVisible = true
                render.setAnimation(Bounce().InUp(binding.uploadcard))
                render.setDuration(200)
                render.start()
            } else {
                binding.uploadcard.isVisible = false
            }
        }
        binding.filePick.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // Allow all file types
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICKER_REQUEST)
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
        val members = ArrayList<String>()
        ref.child("users").addValueEventListener(
            object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children){
                        val value = snap.getValue(UserData::class.java)
                        value?.let {
                            members.add(value.userId)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }
        )
        members.forEachIndexed { index, s ->
            ref.child("usertokens").child(s).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val value = snap.getValue(String::class.java)
                        value?.let {
                            members.add(it)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        val spinnerlist = arrayOf("Select the zone","Study", "Game","Event")
        val spinner = binding.spinner2
        if(spinner !=null){
        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,spinnerlist)
        spinner.adapter = adapter
        }
        spinner.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                zone = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        var userinfo = UserData()
        ref.child("users").child(user!!.uid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val info = snapshot.getValue(UserData::class.java)
                info?.let {
                    userinfo = it
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        binding.save.setOnClickListener {
            val title = binding.titlete.text.toString()
            val descrip = binding.descrip.text.toString()
            if(title.isNotEmpty() && descrip.isNotEmpty()){
                if(zone.equals("Select the zone", ignoreCase = true)){
                    Toast.makeText(this, "Select the zone.", Toast.LENGTH_SHORT).show()
                } else if(filetype==""){
                    filetype = "file"
                } else{
                    if(notkey==""){notkey = ref.child("posts").child(zone).push().key.toString()}
                    else notkey = intent.getStringExtra("notkey")!!
                    val postdata = PostData(userinfo.username,userinfo.profilePic,title,descrip,urid,userinfo.userId,filetype,zone,notkey)
                    ref.child("posts").child(zone).child(notkey).setValue(postdata).addOnSuccessListener {
                        Toast.makeText(this, "Post uploaded", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
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
                                                sendNotificationToUser(title,accessToken, token, descrip)
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
                }
            } else {
                Toast.makeText(this, "Please fill title and description.", Toast.LENGTH_SHORT).show()
            }

        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        Toast.makeText(this, "Don't press back it will take some time", Toast.LENGTH_SHORT).show()
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            val datauri = data?.data
            if (datauri != null) {
                binding.postVideo.isVisible = true
                binding.postimg.isVisible =false
                val player = ExoPlayer.Builder(this).build()
                val playerView = binding.postVideo
                playerView.player = player
                val mediaItem = MediaItem.fromUri(datauri)
                player.setMediaItem(mediaItem)
                player.prepare()

                val ref =
                    Firebase.storage.reference.child("posts/videos/"  + System.currentTimeMillis())
                val uploadTask = ref.putFile(datauri)
                binding.postprogress.visibility = View.VISIBLE
                binding.postprogressper.visibility = View.VISIBLE
                    uploadTask.addOnProgressListener { taskSnapshot->
                        binding.save.isVisible = false
                        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                        binding.postprogressper.text = "Uploading: ${progress.toInt()}%"
                    }
                    .addOnSuccessListener {
                        binding.save.isVisible = true
                        ref.downloadUrl.addOnSuccessListener {
                            urid = it.toString()
                            filetype ="video"
                            binding.postprogress.visibility = View.GONE
                            binding.postprogressper.visibility = View.GONE
                        }
                    }
            }
        }
        if (requestCode == FILE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            if (fileUri != null) {
                val ref = Firebase.storage.reference.child("posts/files/" + System.currentTimeMillis())
                val uploadTask = ref.putFile(fileUri)
                binding.postprogress.visibility = View.VISIBLE
                binding.postprogressper.visibility = View.VISIBLE
                uploadTask.addOnProgressListener { taskSnapshot->
                    binding.save.isVisible = false
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    binding.postprogressper.text = "Uploading: ${progress.toInt()}%"
                }
                    .addOnSuccessListener {
                        binding.save.isVisible = true
                        ref.downloadUrl.addOnSuccessListener {
                            urid = it.toString()
                            binding.descrip.setText(urid)
                            filetype = "file" // Set file type
                            binding.postprogress.visibility = View.GONE
                            binding.postprogressper.visibility = View.GONE
                            Toast.makeText(this, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "File upload failed!", Toast.LENGTH_SHORT).show()
                    }
            }
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
    val LauncherImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data != null) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    binding.postVideo.isVisible = false
                    binding.postimg.isVisible = true
                    val datauri = it.data?.data
                    binding.postimg.setImageURI(datauri)
                    Toast.makeText(this, "Don't press back it will take time!", Toast.LENGTH_SHORT).show()
                    currentUser?.let { user ->
                        val ref = Firebase.storage.reference.child("posts/pictures/" +System.currentTimeMillis())
                        val uploadTask = ref.putFile(it.data!!.data!!)
                        binding.postprogress.visibility = View.VISIBLE
                        binding.postprogressper.visibility = View.VISIBLE
                        uploadTask.addOnProgressListener { taskSnapshot->
                            binding.save.isVisible = false
                            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                            binding.postprogressper.text = "Uploading: ${progress.toInt()}%"
                        }
                            .addOnSuccessListener {
                                binding.save.isVisible = true
                                ref.downloadUrl.addOnSuccessListener {img->
                                    urid = img.toString()

                                    filetype = "image"
                                    binding.postprogress.visibility = View.GONE
                                    binding.postprogressper.visibility = View.GONE
                                }
                            }
                    }

                }
            }

        }

}