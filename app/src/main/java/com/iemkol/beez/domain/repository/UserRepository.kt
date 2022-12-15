package com.iemkol.beez.domain.repository

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.util.Resource

interface UserRepository {
    fun getUser(uid:String):Task<DataSnapshot>
    fun storeUserDetails(user:User)
    fun removeBlockedUser(currUid:String, uid:String)
    fun getUserLive(uid: String, userLiveData: MutableLiveData<Resource<User>>)
}