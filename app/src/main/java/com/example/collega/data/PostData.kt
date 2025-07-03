package com.example.collega.data

data class PostData(
    val username:String,
    val profilepic:String,
    val title:String,
    val description:String,
    val picture:String,
    val work:String,
    val fileType:String,
    val zone:String,
    val notkey:String
    )
{
    constructor() : this("","","","","","","","","")
}
