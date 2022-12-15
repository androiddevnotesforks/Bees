package com.iemkol.beez.presentation.blockedUsers

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iemkol.beez.databinding.ItemBlockedUserBinding
import com.iemkol.beez.domain.model.User

class BlockedUsersItemAdapter(
    private val context: Context,
    private val onUnBlockClicked: OnUnBlockClicked
):RecyclerView.Adapter<BlockedUsersItemAdapter.BlockedUsersItemViewHolder>() {
    private val blockedUsers = mutableListOf<User>()

    class BlockedUsersItemViewHolder(val binding: ItemBlockedUserBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUsersItemViewHolder {
        return BlockedUsersItemViewHolder(ItemBlockedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BlockedUsersItemViewHolder, position: Int) {
        val currBlockedUser = blockedUsers[position]
        if (currBlockedUser.profilePicUrl?.isNotEmpty() == true) {
            holder.binding.blockedUserImageView.visibility = View.VISIBLE
            Glide.with(context).load(currBlockedUser.profilePicUrl).circleCrop().into(holder.binding.blockedUserImageView)
        }
        holder.binding.blockedUserFullnameView.text = currBlockedUser.name
        holder.binding.taggedUserUsernameView.text = currBlockedUser.username
        holder.binding.unblockUserBtn.setOnClickListener { currBlockedUser.uid?.let { it1 ->
            onUnBlockClicked.unblockUser(
                it1
            )
        } }
    }

    override fun getItemCount(): Int {
        return blockedUsers.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(blockedUsersList:List<User>) {
        blockedUsers.clear()
        blockedUsers.addAll(blockedUsersList)
        notifyDataSetChanged()
    }
}

interface OnUnBlockClicked{
    fun unblockUser(uid:String)
}