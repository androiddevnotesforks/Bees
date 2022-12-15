package com.iemkol.beez.presentation.homeFeed.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.iemkol.beez.data.repository.FeedsRepositoryImpl
import com.iemkol.beez.data.repository.UserListRepoImpl
import com.iemkol.beez.data.repository.UserRepositoryImpl
import com.iemkol.beez.domain.model.Comment
import com.iemkol.beez.domain.model.Feed
import com.iemkol.beez.domain.model.NSFWResponse
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.domain.repository.FeedsRepository
import com.iemkol.beez.domain.repository.UserListRepository
import com.iemkol.beez.domain.repository.UserRepository
import com.iemkol.beez.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    private val repository:FeedsRepository,
    private val userRepository:UserRepository,
    private val userListRepository: UserListRepository
) : ViewModel() {

    val homeFeeds = MutableLiveData<List<Feed>>()
    val isNSFWResponse = MutableLiveData<Response<NSFWResponse>>()

    val listOfUsers = MutableLiveData<Resource<List<User>>>()
    val currentUserDetails = MutableLiveData<Resource<User>>()

    fun fetchAllHomeFeeds() {
        repository.getAllFeeds(homeFeeds)
    }

    fun createNewFeed(feed: Feed) {
        repository.createNewFeed(feed)
    }

    fun setPostLikedByUser(pId:String, uId:String) {
        repository.setLikeOnPost(pId, uId)
    }

    fun setPostNotLikedByUser(pId:String, uId:String) {
        repository.setDislikeOnPost(pId, uId)
    }

    fun getUserDetails(uId:String):Task<DataSnapshot> {
        return userRepository.getUser(uId)
    }

    fun updateUserDetails(user: User) {
        userRepository.storeUserDetails(user)
    }

    fun createNewComment(pId:String, comment:Comment) {
        val cid = comment.cid
        if (cid != null) {
            repository.createNewComment(pId, cid, comment)
        }
    }

    fun editSelfComment(pId:String, comment:Comment) {
        val cid = comment.cid
        if (cid != null) {
            repository.createNewComment(pId, cid, comment)
        }
    }

    fun deleteSelfComment(comment: Comment) {
        viewModelScope.launch(Dispatchers.IO) {
            if(comment.pid?.isNotEmpty() == true && comment.cid?.isNotEmpty() == true)
                repository.deleteSelfComment(comment.pid, comment.cid)
        }
    }

    fun editNewPost(feed:Feed) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.editPost(feed)
        }
    }

    fun deletePost(pId:String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePost(pId)
        }
    }

    fun checkNSFWResponse(imageUrl:String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.checkNSFWContent(imageUrl)
            isNSFWResponse.postValue(response)
        }
    }

    fun getAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            userListRepository.getUserList(listOfUsers)
        }
    }

    fun setPostNotVisibleTo(pId: String, uId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setPostNotVisibleTo(pId, uId)
        }
    }
}