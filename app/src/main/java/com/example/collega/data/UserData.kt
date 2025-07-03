package com.example.collega.data


data class UserData (
  val userId: String,
    val username:String,
    val email:String,
   val graduation:String,
  val work: String,
  val tagline:String,
  val address:String,
  val college:String,
    val profilePic:String

) {
    constructor() : this("","","","","","","","","")
}
