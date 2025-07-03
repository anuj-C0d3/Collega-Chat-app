package com.example.collega.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseInstances {
    val auth:FirebaseAuth = FirebaseAuth.getInstance()
    val dbref = FirebaseDatabase.getInstance().reference

    fun getcurrentuser(): FirebaseUser? {
        return auth.currentUser
    }
    fun realtimedb(): DatabaseReference {
        return dbref
    }


}