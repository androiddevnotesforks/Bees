package com.iemkol.beez.domain.repository

import androidx.lifecycle.MutableLiveData
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.util.Resource

interface UserListRepository {
    suspend fun getUserList(listOfUsers:MutableLiveData<Resource<List<User>>>)
}