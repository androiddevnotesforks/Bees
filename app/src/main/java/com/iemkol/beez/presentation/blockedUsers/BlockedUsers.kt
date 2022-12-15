package com.iemkol.beez.presentation.blockedUsers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.iemkol.beez.R
import com.iemkol.beez.databinding.ActivityBlockedUsersBinding
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.presentation.homeFeed.fragments.HomeFeed
import com.iemkol.beez.presentation.userAuthentication.UserViewModel
import com.iemkol.beez.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedUsers : AppCompatActivity(), OnUnBlockClicked {
    private lateinit var binding:ActivityBlockedUsersBinding
    private lateinit var blockedUsersItemAdapter: BlockedUsersItemAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private val userViewModel by viewModels<UserViewModel>()
    private val listOfBlockedUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Bees)
        binding = ActivityBlockedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = Firebase.auth

        fetchUserDetails()

        binding.walletHomeBackBtn.setOnClickListener { onBackPressed() }

        blockedUsersItemAdapter = BlockedUsersItemAdapter(this@BlockedUsers, this@BlockedUsers)
        binding.blockedUsersRecyclerView.layoutManager = LinearLayoutManager(this@BlockedUsers)
        binding.blockedUsersRecyclerView.adapter = blockedUsersItemAdapter
    }

    private fun fetchAllBlockedUsers(currUser: User) {
        userViewModel.listOfUsers.observe(this, Observer { resource->
            if (resource is Resource.Success) {
                listOfBlockedUsers.clear()
                val listOfAllUsers = resource.data
                // From a list of all users I need those users who are in currUser.blockedUsers and are not reported by currUser
                val mapOfBlockedUsers = currUser.blockedUsers
                listOfAllUsers?.forEach { user ->
                    /*val reportedCount = user.reportedUsers?.size*/

                    if (mapOfBlockedUsers != null) {
                        if (mapOfBlockedUsers.containsKey(user.uid) && mapOfBlockedUsers[user.uid] == false) {
                            listOfBlockedUsers.add(user)
                        }
                    }
                }
                blockedUsersItemAdapter.updateItems(listOfBlockedUsers)
            }
        })
    }

    private fun fetchUserDetails() {
        userViewModel.getUserDetails(firebaseAuth.uid.toString())
            .addOnCompleteListener { task->
                if (task.result.exists()) {
                    val detailsOfUser = task.result.getValue(User::class.java)!!
                    fetchAllBlockedUsers(detailsOfUser)
                }
            }

        userViewModel.getAllUsers()
    }

    override fun unblockUser(uid: String) {
        userViewModel.removeBlockedUser(firebaseAuth.uid.toString(), uid)
        fetchUserDetails()
    }
    companion object {
        private const val TAG = "BlockedUsers"
    }
}