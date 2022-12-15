package com.iemkol.beez.presentation.homeFeed.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.iemkol.beez.R
import com.iemkol.beez.databinding.ItemCommentBinding
import com.iemkol.beez.domain.model.Comment
import com.iemkol.beez.domain.model.Feed

class CommentAdapter(
    private val context: Context,
    private val commentItemClickListener: CommentItemClickListener
): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val commentList = mutableListOf<Comment>()
    private var currentUID = ""
    private var postUID = ""

    class CommentViewHolder(val binding: ItemCommentBinding):RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val currentComment = commentList[position]

        Log.d("Comment Adapter", "current comment: $currentComment, uid: $currentUID")
        if(currentComment.cid?.contains(currentUID) == true || currentUID == postUID) {
            /*holder.binding.editCommentBtn.visibility = View.VISIBLE
            holder.binding.deleteCommentBtn.visibility = View.VISIBLE*/
            holder.binding.moreCommentOptionsBtn.visibility = View.VISIBLE
        } else {
            /*holder.binding.editCommentBtn.visibility = View.GONE
            holder.binding.deleteCommentBtn.visibility = View.GONE*/
            holder.binding.moreCommentOptionsBtn.visibility = View.GONE
        }

        holder.binding.sendEditedCommentBtn.setOnClickListener {
            val commentContent = holder.binding.editCommentEdittext.text.toString()
            if(commentContent.isNotEmpty()) {
                commentItemClickListener.editComment(
                    Comment(
                        pid = currentComment.pid,
                        cid = currentComment.cid,
                        username = currentComment.username,
                        comment = commentContent
                    ))
                holder.binding.editCommentEdittext.visibility = View.GONE
                holder.binding.sendEditedCommentBtn.visibility = View.GONE

                holder.binding.moreCommentOptionsBtn.visibility = View.VISIBLE
                holder.binding.commentTV.visibility = View.VISIBLE
            }
            else
                Toast.makeText(context, "Comment cannot be empty!", Toast.LENGTH_SHORT).show()
        }

        holder.binding.moreCommentOptionsBtn.setOnClickListener { onOptionsMenuClicked(currentComment, holder) }

        holder.binding.userNameOfComment.text = currentComment.username
        holder.binding.commentTV.text = currentComment.comment
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCommentItems(commentListData:Map<String, Comment>, currentUID:String, postUID:String) {
        this.commentList.clear()
        this.commentList.addAll(commentListData.values.toList())
        this.currentUID = currentUID
        this.postUID = postUID
        notifyDataSetChanged()
    }

    private fun onOptionsMenuClicked(currentComment: Comment, holder: CommentAdapter.CommentViewHolder) {
        val popupMenu = PopupMenu(context, holder.binding.moreCommentOptionsBtn)
        popupMenu.inflate(R.menu.comment_options_menu)

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when(item?.itemId){
                    R.id.edit_comment_option -> {
                        if (currentComment.cid?.contains(currentUID) == true) {
                            holder.binding.moreCommentOptionsBtn.visibility = View.GONE
                            holder.binding.commentTV.visibility = View.GONE

                            holder.binding.editCommentEdittext.visibility = View.VISIBLE
                            holder.binding.sendEditedCommentBtn.visibility = View.VISIBLE

                            holder.binding.editCommentEdittext.setText(holder.binding.commentTV.text.toString(), TextView.BufferType.NORMAL)
                        } else {
                            Toast.makeText(context, "You don't have permissions to edit this comment!", Toast.LENGTH_SHORT).show()
                        }

                        return true
                    }
                    // in the same way you can implement others
                    R.id.delete_comment_option -> {
                        commentItemClickListener.deleteComment(currentComment)
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

}

interface CommentItemClickListener{
    fun editComment(comment:Comment)
    fun deleteComment(comment: Comment)
}