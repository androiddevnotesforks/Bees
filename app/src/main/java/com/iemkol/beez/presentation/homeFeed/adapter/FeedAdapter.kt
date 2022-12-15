package com.iemkol.beez.presentation.homeFeed.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iemkol.beez.R
import com.iemkol.beez.databinding.ItemHomeFeedsBinding
import com.iemkol.beez.domain.model.Comment
import com.iemkol.beez.domain.model.Feed

class FeedAdapter(
    private val context: Context,
    private val feedItemClickListener: FeedItemClickListener
    ): RecyclerView.Adapter<FeedAdapter.FeedViewHolder>(), CommentItemClickListener {

    private val homeFeedData = mutableListOf<Feed>()
    private var currentUser = ""
    private var currentUID = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(ItemHomeFeedsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val currentFeed = homeFeedData[position]
        holder.binding.commentsContainer.visibility = View.GONE

        holder.binding.commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        val commentAdapter = CommentAdapter(context, this)
        println("currentFeed.comments: ${currentFeed.comments}")
        currentFeed.comments?.let { currentFeed.uid?.let { it1 ->
            commentAdapter.setCommentItems(it, currentUID,
                it1
            )
        } }
        holder.binding.commentsRecyclerView.adapter = commentAdapter

        holder.binding.comment.setOnClickListener {
            if(holder.binding.commentsContainer.isVisible) {
                holder.binding.comment.setBackgroundResource(R.drawable.comment)
                holder.binding.commentsContainer.visibility = View.GONE
            }
            else {
                holder.binding.comment.setBackgroundResource(R.drawable.commenttapped)
                holder.binding.commentsContainer.visibility = View.VISIBLE
            }
        }

        if(currentFeed.profilePicUrl?.isNotEmpty() == true) {
            holder.binding.profileImage.visibility = View.VISIBLE
            Glide.with(context).load(currentFeed.profilePicUrl).circleCrop().into(holder.binding.profileImage)
        }

        holder.binding.nameView.text = currentFeed.name
        holder.binding.usernameView.text = currentFeed.username
        holder.binding.postCaptionView.text = currentFeed.caption

        if(currentFeed.postPicUrl?.isNotEmpty() == true) {
            holder.binding.postPicView.visibility = View.VISIBLE
            Glide.with(context).load(currentFeed.postPicUrl).fitCenter().into(holder.binding.postPicView)
        } else {
            holder.binding.postPicView.visibility = View.GONE
        }

        holder.binding.likeCount.text = currentFeed.likedByUsers?.count().toString()
        holder.binding.commentCount.text = currentFeed.comments?.count().toString()
        holder.binding.share.setOnClickListener {
            feedItemClickListener.repost(currentFeed)
        }

        holder.binding.userNameOfComment.text = currentUser

        val isPostLiked = feedItemClickListener.isPostLikedByCurrentUser(position, currentFeed.likedByUsers!!)
        if(isPostLiked) holder.binding.like.setBackgroundResource(R.drawable.ic_baseline_thumb_up_24)
        else holder.binding.like.setBackgroundResource(R.drawable.thumb_up_not_like)

        val isFeedPostedByCurrentUser = feedItemClickListener.isFeedPostedByCurrentUser(currentFeed.pid!!)
        if(isFeedPostedByCurrentUser) {
            holder.binding.editPostBtn.visibility = View.VISIBLE
            holder.binding.deletePostBtn.visibility = View.VISIBLE
            holder.binding.moreOptionsBtn.visibility = View.GONE
        } else {
            holder.binding.editPostBtn.visibility = View.GONE
            holder.binding.deletePostBtn.visibility = View.GONE
            holder.binding.moreOptionsBtn.visibility = View.VISIBLE
        }

        holder.binding.deletePostBtn.setOnClickListener {
            feedItemClickListener.deletePost(currentFeed.pid)
        }
        holder.binding.editPostBtn.setOnClickListener {
            feedItemClickListener.editPost(currentFeed)
        }

        holder.binding.commentButton.setOnClickListener {
            if(holder.binding.contentOfNewComment.text.isEmpty() || holder.binding.contentOfNewComment.text.isBlank())
                Toast.makeText(context, "Comment cannot be empty!", Toast.LENGTH_SHORT).show()
            else {
                val commentContent = holder.binding.contentOfNewComment.text.toString()
                holder.binding.contentOfNewComment.text.clear()
                feedItemClickListener.onNewComment(currentFeed.pid, commentContent)
            }
        }

        holder.binding.like.setOnClickListener {
            if(isPostLiked) currentFeed.pid.let { it1 -> feedItemClickListener.setPostNotLiked(it1) }
            else currentFeed.pid.let { it1 -> feedItemClickListener.setPostLiked(it1) }
        }

        holder.binding.moreOptionsBtn.setOnClickListener { onOptionsMenuClicked(currentFeed, holder) }
    }

    override fun getItemCount(): Int {
        return homeFeedData.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFeedItems(homeFeedList:List<Feed>, currentUser:String, currentUID:String) {
        this.homeFeedData.clear()
        this.homeFeedData.addAll(homeFeedList)
        this.homeFeedData.reverse()
        this.currentUser = currentUser
        this.currentUID = currentUID
        notifyDataSetChanged()
    }

    private fun onOptionsMenuClicked(currentFeed: Feed, holder: FeedViewHolder) {
        val popupMenu = PopupMenu(context , holder.binding.moreOptionsBtn)
        popupMenu.inflate(R.menu.options_menu)

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when(item?.itemId){
                    R.id.block_post -> {
                        currentFeed.pid?.let { feedItemClickListener.onBlockPost(it) }
                        return true
                    }
                    // in the same way you can implement others
                    R.id.block_user -> {
                        currentFeed.uid?.let { feedItemClickListener.onBlockUser(it) }
                        return true
                    }
                    R.id.report_post -> {
                        feedItemClickListener.onReportPost(currentFeed)
                        return true
                    }
                    R.id.report_user -> {
                        currentFeed.uid?.let { feedItemClickListener.onReportUser(it) }
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    class FeedViewHolder(val binding:ItemHomeFeedsBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun editComment(comment: Comment) {
        println("Comment: $comment")
        comment.pid?.let { feedItemClickListener.onSelfCommentEdit(it, comment) }
    }

    override fun deleteComment(comment: Comment) {
        feedItemClickListener.onSelfCommentDelete(comment)
    }

}



interface FeedItemClickListener{
    fun onNewComment(pId:String, commentContent:String)
    fun onSelfCommentEdit(pId:String, comment:Comment)
    fun onSelfCommentDelete(comment: Comment)
    fun isPostLikedByCurrentUser(position: Int, likedByUsers:Map<String, String>):Boolean
    fun setPostLiked(pId:String)
    fun setPostNotLiked(pId:String)
    fun repost(currentFeed:Feed)
    fun isFeedPostedByCurrentUser(pId:String):Boolean
    fun deletePost(pId:String)
    fun editPost(currentFeed: Feed)
    fun onBlockPost(pId: String)
    fun onBlockUser(uid:String)
    fun onReportPost(feed: Feed)
    fun onReportUser(uid:String)
}