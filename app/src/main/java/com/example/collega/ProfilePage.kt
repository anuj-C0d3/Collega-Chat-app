package com.example.collega

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.android.car.ui.AlertDialogBuilder
import com.example.collega.data.UserData
import com.example.collega.databinding.ActivityProfilePageBinding
import com.example.collega.databinding.ProfileviewBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import java.sql.Struct

class ProfilePage : AppCompatActivity() {
    private lateinit var binding: ActivityProfilePageBinding
    private lateinit var profileurl:String
    private lateinit var work:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profileprogressbar.isVisible = false
        work = ""
        binding.username.setText(intent.getStringExtra("username"))
        binding.email.setText(intent.getStringExtra("email"))
        binding.graduation.setText(intent.getStringExtra("grad"))
        binding.address.setText(intent.getStringExtra("address"))
        binding.taglin.setText(intent.getStringExtra("tagline"))
        binding.work.setText(intent.getStringExtra("work"))
        binding.College.setText(intent.getStringExtra("college"))
        val uid = intent.getStringExtra("uid")
        val user = FirebaseAuth.getInstance().currentUser
        val work = intent.getStringExtra("work")
        if(uid!="" && work!=""&& !work.equals("admin", ignoreCase = true) || user?.uid!= uid){
            binding.adminpnl.isVisible = false
        }
        profileurl = intent.getStringExtra("profilepic").toString()
        if(profileurl!=""){
        Picasso.get().load(profileurl).into(binding.profileimg)
            binding.profileimg.setOnClickListener {
                val itbinding = ProfileviewBinding.inflate(layoutInflater)
                val alertdial = AlertDialog.Builder(this).setView(itbinding.root)
                Picasso.get().load(profileurl).into(itbinding.imageView4)
                alertdial.create()
                alertdial.show()
                alertdial.setInverseBackgroundForced(true)
                alertdial.setCancelable(true)
            }
        }else {
            binding.profileimg.setImageResource(R.drawable.defaultuserimg)
            binding.profileimg.setOnClickListener {
                Toast.makeText(this, "No profile photo", Toast.LENGTH_SHORT).show()
            }
        }
        binding.logout.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure to logout?")
                .setIcon(R.drawable.logouticon)
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, OnboardingPage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                })
                .create()
                .show()
        }
        val dataref = FirebaseDatabase.getInstance().reference

        if(user!!.uid!=intent.getStringExtra("uid")){
            binding.editprof.isVisible = false
            binding.logout.isVisible =false
            binding.savebtn.isVisible = false
        }
        binding.editprof.setOnClickListener {

            ImagePicker.with(this)
                .compress(800)
                .provider(ImageProvider.BOTH)
                .cropSquare()
                .createIntent {
                    LauncherImage.launch(it)
                }

        }

        binding.savebtn.setOnClickListener {
            binding.profileprogressbar.isVisible = true
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val addr = binding.address.text.toString()
            val grad = binding.graduation.text.toString()
            val taglin = binding.taglin.text.toString()
            val college = binding.College.text.toString()
            val work = binding.work.text.toString()
            val data  = UserData(user.uid,username,email,grad,work,taglin,addr,college,profileurl)
            dataref.child("users").child(user.uid).setValue(data).addOnSuccessListener {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                binding.profileprogressbar.isVisible = false
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }

        }

        binding.adminpnl.setOnClickListener {

            val intent = Intent(this, AdminPanel::class.java)
            intent.putExtra("username",binding.username.text.toString())
            intent.putExtra("profilepic",profileurl)
               startActivity(intent)
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

                        val ref = Firebase.storage.reference.child("profilepic/" + user.uid)
                        ref.putFile(it.data!!.data!!).addOnProgressListener {
                            binding.profileprogressbar.isVisible = true
                        }
                            .addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener {
                                    profileurl = it.toString()
                                    Picasso.get().load(it.toString()).into(binding.profileimg)
                                    binding.profileprogressbar.isVisible = false
                                }
                            }
                    }

                }
            }
        }

}
