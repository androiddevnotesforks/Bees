package com.iemkol.beez.presentation.homeFeed

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.iemkol.beez.R
import com.iemkol.beez.databinding.ActivityHomeFeedsBinding
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.presentation.blockedUsers.BlockedUsers
import com.iemkol.beez.presentation.homeFeed.fragments.HomeFeed
import com.iemkol.beez.presentation.userAuthentication.Login
import com.iemkol.beez.presentation.userAuthentication.Register
import com.iemkol.beez.presentation.userAuthentication.UserViewModel
import com.iemkol.beez.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeFeedsBinding
    private var backPressCount = 0
    private val viewModel by viewModels<UserViewModel>()
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var userDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Bees)

        if (!isNetworkAvailable()) {
            return
        }

        binding = ActivityHomeFeedsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = Firebase.auth

        viewModel.getUserLive(firebaseAuth.uid.toString())

        viewModel.getUserDetails(firebaseAuth.uid.toString())
            .addOnCompleteListener { task->
                if (task.result.exists()) {
                    userDetails = task.result.getValue(User::class.java)!!
                    val reportedUsers = userDetails.reportedUsers
                    if (reportedUsers != null) {
                        if (reportedUsers.size > 5) {
                            Firebase.auth.signOut()
                            Toast.makeText(this, "You have been banned from using Bees based on reports from other users!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@HomeActivity, Login::class.java))
                            finish()
                        }
                    }
                    initViews()
                } else {
                    startActivity(Intent(this@HomeActivity, Register::class.java))
                    finish()
                }
            }

        viewModel.userDetail.observe(this, Observer {
            if (it is Resource.Success) {
                if (it.data != null) {
                    userDetails = it.data
                    val reportedUsers = userDetails.reportedUsers
                    if (reportedUsers != null) {
                        if (reportedUsers.size > 5) {
                            Firebase.auth.signOut()
                            Toast.makeText(this, "You have been banned from using Bees based on reports from other users!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@HomeActivity, Login::class.java))
                            finish()
                        }
                    }
                }
            }
        })

        /*initViews()*/
    }

    fun initViews() {
        replaceFragment(HomeFeed())

        binding.drawerOpenerBtn.setOnClickListener {
            if(binding.drawer.isDrawerOpen(GravityCompat.START)) binding.drawer.closeDrawer(GravityCompat.START)
            else binding.drawer.openDrawer(GravityCompat.START)
        }

        val navHeader = binding.navView.getHeaderView(0)
        val profilePicView = navHeader.findViewById<ImageView>(R.id.nav_header_profile_pic)
        val fullNameView = navHeader.findViewById<TextView>(R.id.nav_header_fullname)
        val username = navHeader.findViewById<TextView>(R.id.nav_header_username)

        if(userDetails.profilePicUrl?.isNotEmpty() == true) {
            Glide.with(this@HomeActivity).load(userDetails.profilePicUrl).into(profilePicView)
        }
        fullNameView.text = userDetails.name
        username.text = userDetails.username

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.profile -> {
                    startActivity(Intent(this@HomeActivity, Register::class.java))
                }
                R.id.blocked_users_view -> {
                    startActivity(Intent(this@HomeActivity, BlockedUsers::class.java))
                }
                R.id.signOut-> {
                    Firebase.auth.signOut()
                    Toast.makeText(this, "Thanks for passing by! : )", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@HomeActivity, Login::class.java))
                    finish()
                }
                else -> {

                }
            }

            return@setNavigationItemSelectedListener true
        }
    }

    private fun setNoInternetPage() {
        Log.e("HomeActivity", "called : setNoInternetPage")
        setContentView(R.layout.no_internet_layout)
        findViewById<View>(R.id.tv_tryagain).setOnClickListener {
            Log.e("HomeActivity", "clicked")
            if (isNetworkAvailable()) {
                startActivity(Intent(this, Login::class.java))
            } else {
                Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
            .isConnected
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        if (!isNetworkAvailable()) {
            setNoInternetPage()
            return
        }
    }
}