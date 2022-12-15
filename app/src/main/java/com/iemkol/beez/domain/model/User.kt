package com.iemkol.beez.domain.model

data class User(
    val name:String?="",
    var profilePicUrl:String?="",
    val uid:String?="",
    val username:String?="",
    val blockedUsers:Map<String, Boolean>?= emptyMap(),
    val reportedUsers:Map<String, String>?= emptyMap()
)