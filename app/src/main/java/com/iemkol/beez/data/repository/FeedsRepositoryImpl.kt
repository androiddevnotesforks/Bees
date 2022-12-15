package com.iemkol.beez.data.repository


import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iemkol.beez.data.api.NSFWApi
import com.iemkol.beez.domain.model.Comment
import com.iemkol.beez.domain.model.Feed
import com.iemkol.beez.domain.model.NSFWResponse
import com.iemkol.beez.domain.repository.FeedsRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

class FeedsRepositoryImpl @Inject constructor(
    private val nsfwApi: NSFWApi
) :FeedsRepository {
    private val database = Firebase.database
    private val databaseReference = database.getReference("all_feeds")

    companion object {
        private const val TAG = "FeedsRepositoryImpl"
    }

    override fun getAllFeeds(liveData: MutableLiveData<List<Feed>>) {
        databaseReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newFeeds = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(Feed::class.java)!!
                    }
                    Log.d(TAG, "getAllFeeds(): $newFeeds")
                    liveData.postValue(newFeeds)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun createNewFeed(feed: Feed) {
        feed.pid?.let {
            databaseReference.child(it).setValue(feed).addOnSuccessListener {
                Log.d("FeedsRepositoryImpl", "createNewFeed(): Success")
            }.addOnFailureListener {
                Log.e("FeedsRepositoryImpl", "createNewFeed(): Failure")
            }
        }
    }

    override fun setLikeOnPost(pId: String, uId: String) {
        databaseReference.child(pId).child("likedByUsers").child(uId).setValue(uId).addOnSuccessListener {
            Log.d("FeedsRepositoryImpl", "setLikeOnPost(): Success")
        }.addOnFailureListener {
            Log.d("FeedsRepositoryImpl", "setLikeOnPost(): Failure")
        }

        // pId/likedByUsers/uId-uId
    }

    override fun setDislikeOnPost(pId: String, uId: String) {
        databaseReference.child(pId).child("likedByUsers").child(uId).removeValue()
    }

    override fun setPostNotVisibleTo(pId: String, uId:String) {
        databaseReference.child(pId).child("postNotVisibleTo").child(uId).setValue(uId).addOnSuccessListener {
            Log.d("FeedsRepositoryImpl", "setPostNotVisibleTo(): Success")
        }.addOnFailureListener {
            Log.d("FeedsRepositoryImpl", "setPostNotVisibleTo(): Failure")
        }
    }

    override fun createNewComment(pId: String, cid:String, comment: Comment) {
        databaseReference.child(pId).child("comments").child(cid).setValue(comment).addOnSuccessListener {
            Log.d("FeedsRepositoryImpl", "createNewComment(): Success")
        }.addOnFailureListener {
            Log.e("FeedsRepositoryImpl", "createNewComment(): Failure: ${it.message}")
        }
    }

    override fun deleteSelfComment(pId: String, cid: String) {
        databaseReference.child(pId).child("comments").child(cid).removeValue().addOnSuccessListener {
            Log.d("FeedsRepositoryImpl", "deleteSelfComment(): Success")
        }
        .addOnFailureListener {
            Log.e("FeedsRepositoryImpl", "deleteSelfComment(): Failure: ${it.message}")
        }
    }

    override fun deletePost(pId: String) {
        databaseReference.child(pId).removeValue().addOnSuccessListener {
            Log.d("FeedsRepositoryImpl", "deletePost(): Success")
        }
        .addOnFailureListener {
            Log.e("FeedsRepositoryImpl", "deletePost(): Failure: ${it.message}")
        }
    }

    override fun editPost(feed: Feed) {
        feed.pid?.let {
            databaseReference.child(it).setValue(feed).addOnSuccessListener {
                Log.d("FeedsRepositoryImpl", "editPost(): Success")
            }
            .addOnFailureListener {
                Log.e("FeedsRepositoryImpl", "editPost(): Failure: ${it.message}")
            }
        }
    }

    override suspend fun checkNSFWContent(imageUrl: String): Response<NSFWResponse> {
        val response = nsfwApi.checkNSFWImage(imageUrl)
        Log.d(TAG, response.toString())
        return response
    }
}