package com.iemkol.beez.presentation.homeFeed.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iemkol.beez.databinding.ItemTagUsernameBinding
import com.iemkol.beez.domain.model.User

class TaggedUserAdapter(
    private val context:Context,
    private val taggedUserItemClickListener: TaggedUserItemClickListener
):RecyclerView.Adapter<TaggedUserAdapter.TaggedUsersViewHolder>() {

    private val taggedUserData = mutableListOf<User>()

    class TaggedUsersViewHolder(val binding:ItemTagUsernameBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaggedUsersViewHolder {
        return TaggedUsersViewHolder((ItemTagUsernameBinding.inflate(LayoutInflater.from(parent.context), parent, false)))
    }

    override fun onBindViewHolder(holder: TaggedUsersViewHolder, position: Int) {
        val currentTaggedUser = taggedUserData[position]

        if (!currentTaggedUser.profilePicUrl.isNullOrEmpty()) {
            Glide.with(context).load(currentTaggedUser.profilePicUrl).circleCrop().into(holder.binding.taggedUserImageView)
        }

        if(!currentTaggedUser.name.isNullOrEmpty()) {
            holder.binding.taggedUserFullnameView.text = currentTaggedUser.name
        }

        if(!currentTaggedUser.username.isNullOrEmpty()) {
            holder.binding.taggedUserUsernameView.text = currentTaggedUser.username
        }

        holder.binding.tagUsernameCheckBox.setOnClickListener {
            if(!holder.binding.tagUsernameCheckBox.isChecked) {
                taggedUserItemClickListener.onUserUnTagged(currentTaggedUser.username!!)
                // holder.binding.tagUsernameCheckBox.isChecked = true
            } else {
                taggedUserItemClickListener.onUserTagged(currentTaggedUser.username!!)


            }
        }

    }

    override fun getItemCount(): Int {
        return taggedUserData.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTaggedUserItems(taggedUserList:List<User>) {
        this.taggedUserData.clear()
        this.taggedUserData.addAll(taggedUserList)
        notifyDataSetChanged()
    }
}

interface TaggedUserItemClickListener {
    fun onUserTagged(username:String)
    fun onUserUnTagged(username:String)
}