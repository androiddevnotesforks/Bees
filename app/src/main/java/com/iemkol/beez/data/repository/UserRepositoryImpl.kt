package com.iemkol.beez.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.domain.repository.UserRepository
import com.iemkol.beez.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

class UserRepositoryImpl @Inject constructor():UserRepository {
    private val database = Firebase.database
    private val usersDatabase = database.getReference("users")
    override fun getUser(uid: String):Task<DataSnapshot> {
        return usersDatabase.child(uid).get()
    }

    override fun getUserLive(uid: String, userLiveData: MutableLiveData<Resource<User>>) {
        usersDatabase.child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = snapshot.getValue(User::class.java)
                    userLiveData.postValue(Resource.Success(result))
                }

                override fun onCancelled(error: DatabaseError) {
                    userLiveData.postValue(Resource.Error(error.message))
                }

            })
    }

    override fun storeUserDetails(user: User) {
        user.uid?.let {
            usersDatabase.child(it).setValue(user)
                .addOnSuccessListener { Log.d(TAG, "storeUserDetails(): Success") }
                .addOnFailureListener { exception->
                    Log.e(TAG, exception.message.toString())
                }
        }
    }

    override fun removeBlockedUser(currUid:String, uid:String) {
        usersDatabase.child(currUid).child("blockedUsers").child(uid).removeValue()
            .addOnSuccessListener { Log.d(TAG, "removeBlockedUser(): Success") }
            .addOnFailureListener { Log.e(TAG, "removeBlockedUser(): ${it.message}") }
    }

    companion object {
        private const val TAG = "UserRepoImpl"
    }
}