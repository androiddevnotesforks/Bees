package com.iemkol.beez.domain.model

data class Feed (
    val pid:String?="",
    val uid:String?="",
    val name:String?="",
    val username:String?="",
    val profilePicUrl:String?="",
    val postPicUrl:String?="",
    val caption:String?="",
    val comments:Map<String, Comment>?= emptyMap(),
    val likedByUsers:Map<String, String>?= emptyMap(),
    val postNotVisibleTo:Map<String, String>?= emptyMap(),
    val reportCount:Int?=0
)