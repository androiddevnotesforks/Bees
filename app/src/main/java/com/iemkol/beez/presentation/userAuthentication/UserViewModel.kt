package com.iemkol.beez.presentation.userAuthentication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.iemkol.beez.data.repository.UserRepositoryImpl
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.domain.repository.UserListRepository
import com.iemkol.beez.domain.repository.UserRepository
import com.iemkol.beez.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userListRepository: UserListRepository
) :ViewModel() {

    val listOfUsers = MutableLiveData<Resource<List<User>>>()
    val userDetail = MutableLiveData<Resource<User>>()

    fun getUserDetails(uid:String): Task<DataSnapshot> {
        return userRepository.getUser(uid)
    }

    fun uploadUserDetails(user: User) {
        userRepository.storeUserDetails(user)
    }

    fun removeBlockedUser(currUID:String, uid:String) {
        userRepository.removeBlockedUser(currUID, uid)
    }

    fun getAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            userListRepository.getUserList(listOfUsers)
        }
    }

    fun getUserLive(uid:String) {
        userRepository.getUserLive(uid, userDetail)
    }
}