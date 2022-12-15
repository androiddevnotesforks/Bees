package com.iemkol.beez.domain.repository

import androidx.lifecycle.MutableLiveData
import com.iemkol.beez.domain.model.Comment
import com.iemkol.beez.domain.model.Feed
import com.iemkol.beez.domain.model.NSFWResponse
import retrofit2.Response


interface FeedsRepository {
    fun getAllFeeds(
        liveData: MutableLiveData<List<Feed>>
    )

    fun createNewFeed(
        feed: Feed
    )

    fun setLikeOnPost(pId:String, uId:String)

    fun setDislikeOnPost(pId:String, uId:String)

    fun createNewComment(pId:String, cid:String, comment:Comment)

    fun deleteSelfComment(pId:String, cid: String)

    fun deletePost(pId:String)

    fun editPost(feed: Feed)

    suspend fun checkNSFWContent(imageUrl:String):Response<NSFWResponse>

    fun setPostNotVisibleTo(pId: String, uId:String)
}