package com.example.collega.data

data class messageschema(
    val messageId:String,
    val uid :String,
    val receiverId:String,
    val message:String,
    val timestamp:Long,
    val read:String,
    val messagetype:String
    ){
    constructor() : this("","","","",0,"","")

}
