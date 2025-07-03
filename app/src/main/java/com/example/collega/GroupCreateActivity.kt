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
import com.example.collega.data.GroupData
import com.example.collega.databinding.ActivityGroupCreateBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso

class GroupCreateActivity : AppCompatActivity() {
    var profileurl =""
    var groupId = ""
    private lateinit var binding:ActivityGroupCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.setprofile.setOnClickListener {
            ImagePicker.with(this)
                .compress(500)
                .provider(ImageProvider.BOTH)
                .cropSquare()
                .createIntent {
                    LauncherImage.launch(it)
                }
        }
        val ref = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser
        binding.crate.setOnClickListener {
            val grouname = binding.groupname.text.toString()
            val des = binding.groupdes.text.toString()
            groupId =   ref.child("groups").push().key!!
            val group = GroupData(grouname,groupId,profileurl,des,user!!.uid)
            ref.child("groups").child(groupId).setValue(group).addOnSuccessListener {
                Toast.makeText(this, "Group created Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,AdminPanel::class.java))

            }
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
                        val ref = Firebase.storage.reference.child("groupprofiles/" + user?.uid+System.currentTimeMillis())
                        ref.putFile(it.data!!.data!!)
                            .addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener {
                                    profileurl = it.toString()
                                    Picasso.get().load(it.toString()).into(binding.profile)
                                }
                            }
                    }

                }
            }
        }
}
