package com.iemkol.beez.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.domain.repository.UserListRepository
import com.iemkol.beez.util.Resource
import javax.inject.Inject

class UserListRepoImpl @Inject constructor():UserListRepository {
    private val database = Firebase.database
    private val usersDatabase = database.getReference("users")

    override suspend fun getUserList(listOfUsers: MutableLiveData<Resource<List<User>>>) {
        usersDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersList = snapshot.children.map { dataSnapshot ->
                    dataSnapshot.getValue(User::class.java)!!
                }
                Log.d(TAG, "getUserList(): $usersList")
                listOfUsers.postValue(Resource.Success(usersList))
            }

            override fun onCancelled(error: DatabaseError) {
                listOfUsers.postValue(Resource.Error(error.message))
            }
        })
    }


    companion object {
        private const val TAG = "UserListRepoImpl"
    }
}