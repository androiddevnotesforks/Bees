package com.iemkol.beez.presentation.userAuthentication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.iemkol.beez.R
import com.iemkol.beez.databinding.ActivityRegisterBinding
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.presentation.homeFeed.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var userDetails:User
    private var storageRef: StorageReference? = null
    private lateinit var profileImageUrl : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Bees)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storage = Firebase.storage
        storageRef = storage.reference

        firebaseAuth = Firebase.auth

        userViewModel.getUserDetails(firebaseAuth.uid.toString())
            .addOnCompleteListener { task->
                if (task.result.exists()) {
                    userDetails = task.result.getValue(User::class.java)!!
                    initUserDetails()
                }
            }

        binding.galleryProfilePicView.setOnClickListener{
            selectProfileImageFromGallery()
        }

        binding.btnSaveDetails.setOnClickListener{
            val name = binding.fullName.text.toString()
            val username = binding.userName.text.toString()

            if(name.isBlank() || name.isEmpty() || username.isEmpty() || username.isBlank()) {
                Toast.makeText(this, "Username or Name cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.registerProgressBar.visibility = View.VISIBLE
            binding.btnSaveDetails.visibility = View.GONE

            uploadProfileImage(name, username, Firebase.auth.uid.toString())
        }
    }

    private fun initUserDetails() {
        binding.fullName.setText(userDetails.name, TextView.BufferType.EDITABLE)
        binding.userName.setText(userDetails.username, TextView.BufferType.EDITABLE)
        if(userDetails.profilePicUrl?.isNotEmpty() == true) {
            Glide.with(this@Register).load(userDetails.profilePicUrl).into(binding.galleryProfilePicView)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadProfileImage(name:String, username: String, uid:String) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val drawable = binding.galleryProfilePicView.drawable as BitmapDrawable
        val bitmap : Bitmap = drawable.bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val buzzProfilePictureByteArray = byteArrayOutputStream.toByteArray()
        val time = System.currentTimeMillis()

        val imagePath = storageRef!!.child("profile_pictures").child(time.toString()+"logo.jpg").putBytes(buzzProfilePictureByteArray)
        imagePath.addOnCompleteListener{
            Log.d("PostImageUploaded", it.isSuccessful.toString())
            GlobalScope.launch(Dispatchers.IO) {
                profileImageUrl = imagePath.result.metadata!!.reference!!.downloadUrl.await()
                val user = User(
                    name,
                    profileImageUrl.toString(),
                    uid,
                    username
                )
                userDetails = user
                registerUser()
            }
            binding.registerProgressBar.visibility = View.GONE
        }
    }

    private fun selectProfileImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK);
        intent.type = "image/*"
        startActivityForResult(intent, 100);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 100) {
            binding.galleryProfilePicView.setImageURI(data?.data)
        }
    }

    private suspend fun registerUser() {
        val name = binding.fullName.text.toString()
        val username = binding.userName.text.toString()
        val uid = firebaseAuth.uid.toString()
        val profilePicUrl = profileImageUrl.toString()

        userViewModel.uploadUserDetails(User(
            name = name,
            profilePicUrl = profilePicUrl,
            uid = uid,
            username = username,
            blockedUsers = userDetails.blockedUsers,
            reportedUsers = userDetails.reportedUsers
        ))
        withContext(Dispatchers.Main) {
            binding.registerProgressBar.visibility = View.GONE
            binding.btnSaveDetails.visibility = View.VISIBLE
            startActivity(Intent(this@Register, HomeActivity::class.java))
            finish()
        }

    }

    override fun onBackPressed() {
        Toast.makeText(this@Register, "Uploading these details is mandatory!", Toast.LENGTH_LONG).show()
    }
}