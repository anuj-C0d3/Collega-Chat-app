package com.example.collega.data

data class GroupData(val groupname:String, val groupId:String, val groupProfile:String,val des:String,val admin:String)

{
    constructor() : this(
        "","","","",""
    )
}

