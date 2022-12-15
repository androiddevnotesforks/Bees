package com.iemkol.beez.presentation.homeFeed.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.iemkol.beez.R
import com.iemkol.beez.databinding.FragmentHomeFeedBinding
import com.iemkol.beez.domain.model.Comment
import com.iemkol.beez.domain.model.Feed
import com.iemkol.beez.domain.model.User
import com.iemkol.beez.presentation.homeFeed.adapter.FeedAdapter
import com.iemkol.beez.presentation.homeFeed.adapter.FeedItemClickListener
import com.iemkol.beez.presentation.homeFeed.adapter.TaggedUserAdapter
import com.iemkol.beez.presentation.homeFeed.adapter.TaggedUserItemClickListener
import com.iemkol.beez.presentation.userAuthentication.Login
import com.iemkol.beez.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class HomeFeed : Fragment(R.layout.fragment_home_feed), FeedItemClickListener, TaggedUserItemClickListener {

    companion object {
        fun newInstance() = HomeFeed()
        private const val TAG = "HomeFeedFragment"
    }

    private var _binding: FragmentHomeFeedBinding? = null
    private val binding get() = _binding!!

    private var storageRef: StorageReference? = null

    /*private lateinit var viewModel: HomeFeedViewModel*/
    private val viewModel by viewModels<HomeFeedViewModel>()

    private lateinit var detailsOfUser:User

    private var isNewPost = true

    private lateinit var feedAdapter: FeedAdapter
    private lateinit var taggedUserAdapter: TaggedUserAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
        when (it) {
            true -> { println("Permission has been granted by user") }
            false -> {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                //show your custom dialog and navigate to Permission settings
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detailsOfUser = User("","","","")

        val storage = Firebase.storage
        storageRef = storage.reference

        /*val uid = firebaseAuth.uid*/
        Log.d(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentHomeFeedBinding.inflate(inflater, container, false)

        firebaseAuth = Firebase.auth

        viewModel.getUserDetails(firebaseAuth.uid.toString())
            .addOnCompleteListener { task->
                if (task.result.exists()) {
                    detailsOfUser = task.result.getValue(User::class.java)!!
                    initNewPost(detailsOfUser)
                    viewModel.fetchAllHomeFeeds()
                    viewModel.getAllUsers()
                    Log.d(TAG, detailsOfUser.toString())
                }
            }

        taggedUserAdapter = TaggedUserAdapter(requireContext(), this@HomeFeed)
        binding.tagUsernameRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.tagUsernameRecyclerView.adapter = taggedUserAdapter
        viewModel.listOfUsers.observe(viewLifecycleOwner) { resource->
            if(resource is Resource.Success) {
                resource.data?.let { taggedUserAdapter.setTaggedUserItems(it) }
            } else {
                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
            }
        }

        feedAdapter = FeedAdapter(requireContext(), this@HomeFeed)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.feedRecyclerView.adapter = feedAdapter
        viewModel.homeFeeds.observe(viewLifecycleOwner) { listFeeds->
            val filteredListFeeds = mutableListOf<Feed>()
            listFeeds.forEach { feed ->
                if (showPostToCurrentUser(feed)) filteredListFeeds.add(feed)
            }
            feedAdapter.setFeedItems(filteredListFeeds, detailsOfUser.username.toString(), detailsOfUser.uid.toString())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
    }

    // To avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        viewModel.getUserDetails(firebaseAuth.uid.toString())
            .addOnCompleteListener { task->
                if (task.result.exists()) {
                    detailsOfUser = task.result.getValue(User::class.java)!!
                    initNewPost(detailsOfUser)
                    Log.d(TAG, detailsOfUser.toString())
                }
            }
    }

    override fun onResume() {
        super.onResume()

        viewModel.getUserDetails(firebaseAuth.uid.toString())
            .addOnCompleteListener { task->
                if (task.result.exists()) {
                    detailsOfUser = task.result.getValue(User::class.java)!!
                    initNewPost(detailsOfUser)
                    viewModel.fetchAllHomeFeeds()
                    viewModel.getAllUsers()
                    Log.d(TAG, detailsOfUser.toString())
                }
            }
    }

    private fun initNewPost(userDetail:User) {

        binding.newBuzzButton.setOnClickListener {
            binding.newPostContainer.visibility = View.VISIBLE
            binding.newBuzzButton.visibility = View.GONE
        }

        if(userDetail.profilePicUrl?.isNotEmpty() == true) {
            Glide.with(this).load(userDetail.profilePicUrl).into(binding.newPostUserProfileImage)
        }
        binding.newPostFullnameView.text = userDetail.name
        binding.newPostUsernameView.text = userDetail.username
        binding.cancelNewPost.setOnClickListener {
            binding.newPostCaptionContent.text?.clear()
            binding.newPostGalleryImg.setImageDrawable(null)

            binding.newPostContainer.visibility = View.GONE
            binding.newBuzzButton.visibility = View.VISIBLE
        }

        binding.uploadImgFromGalleryBtn.setOnClickListener{
            selectImageFromGallery()
        }

        binding.uploadImgFromCameraBtn.setOnClickListener {
            if(checkCameraPermissions())
                captureImageFromCamera()
            else
                permissionsResultCallback.launch(Manifest.permission.CAMERA)
        }

        binding.tagUsersBtn.setOnClickListener {
            if(!binding.tagUsernameRecyclerView.isVisible)
                binding.tagUsernameRecyclerView.visibility = View.VISIBLE
            else
                binding.tagUsernameRecyclerView.visibility = View.GONE
        }

        binding.newPostBtn.setOnClickListener {
            binding.newPostContainer.visibility = View.GONE
            binding.newBuzzButton.visibility = View.VISIBLE
            if((binding.newPostCaptionContent.text?.isEmpty() == true) || (binding.newPostCaptionContent.text?.isBlank() == true)) {
                Toast.makeText(context, "Caption cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "Preparing to upload post....", Toast.LENGTH_LONG).show()
            createNewPost(userDetail)
        }
    }

    private fun initEditPost(feed: Feed) {
        if(detailsOfUser.profilePicUrl?.isNotEmpty() == true) {
            Glide.with(this).load(detailsOfUser.profilePicUrl).into(binding.editPostUserProfileImage)
        }
        binding.editPostFullnameView.text = detailsOfUser.name
        binding.editPostUsernameView.text = detailsOfUser.username
        binding.cancelEditPost.setOnClickListener {
            binding.editPostCaptionContent.text?.clear()
            binding.editPostGalleryImg.setImageDrawable(null)

            binding.editPostContainer.visibility = View.GONE
            binding.newBuzzButton.visibility = View.VISIBLE
        }

        binding.editImgFromGalleryBtn.setOnClickListener {
            selectImageForEditFromGallery()
        }

        binding.editImgFromCameraBtn.setOnClickListener {
            if (checkCameraPermissions())
                captureImageFromEditCamera()
            else
                permissionsResultCallback.launch(Manifest.permission.CAMERA)
        }

        binding.editPostBtn.setOnClickListener {
            binding.editPostContainer.visibility = View.GONE
            binding.newBuzzButton.visibility = View.VISIBLE
            if((binding.editPostCaptionContent.text?.isEmpty() == true) || (binding.editPostCaptionContent.text?.isBlank() == true)) {
                Toast.makeText(context, "Caption cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "Preparing to edit post....", Toast.LENGTH_LONG).show()
            editNewPost(feed)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun editNewPost(feed: Feed) {
        if(binding.editPostGalleryImg.drawable != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val drawable = binding.editPostGalleryImg.drawable as BitmapDrawable
            val bitmap: Bitmap = drawable.bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val buzzImageByteArray = byteArrayOutputStream.toByteArray()
            val time = System.currentTimeMillis()
            val imagePath =
                storageRef!!.child(time.toString() + "logo.jpg").putBytes(buzzImageByteArray)
            imagePath.addOnCompleteListener {
                Log.d("PostImageUploaded", it.isSuccessful.toString())
                GlobalScope.launch(Dispatchers.IO) {

                    val buzzImageUrl =
                        imagePath.result.metadata!!.reference!!.downloadUrl.await()
                    Log.d("HomeFeed", "createNewPost(): $buzzImageUrl")

                    viewModel.checkNSFWResponse(buzzImageUrl.toString())
                    withContext(Dispatchers.Main) {
                        viewModel.isNSFWResponse.observe(viewLifecycleOwner, Observer { response->
                            Log.d(TAG, "editNewPost: ${response.body()}")
                            if(response.isSuccessful) {
                                val result = response.body()?.results?.get(0)
                                Log.d(TAG, "editNewPost successful: $result")
                                if (result != null) {
                                    if(result.status.code == "ok") {
                                        val nsfw = result.entities[0].classes.nsfw.toDouble()
                                        Log.d(TAG, "editNewPost nsfw: $nsfw")
                                        if(nsfw>0.3) {
                                            Toast.makeText(context, "You are not allowed to post NSFW content!", Toast.LENGTH_SHORT).show()
                                            binding.newPostGalleryImg.setImageDrawable(null)
                                        } else if(binding.editPostCaptionContent.text.toString().isNotEmpty()){
                                            Log.d(TAG, "editNewPost: ${response.body()?.results}")
                                            viewModel.editNewPost(
                                                Feed(
                                                    pid = feed.pid,
                                                    uid = feed.uid,
                                                    name = feed.name,
                                                    username = feed.username,
                                                    profilePicUrl = feed.profilePicUrl,
                                                    postPicUrl = buzzImageUrl.toString(),
                                                    caption = binding.editPostCaptionContent.text.toString(),
                                                    comments = feed.comments,
                                                    likedByUsers = feed.likedByUsers,
                                                    postNotVisibleTo = feed.postNotVisibleTo,
                                                    reportCount = feed.reportCount
                                                )
                                            )

                                            binding.editPostCaptionContent.text?.clear()
                                            binding.editPostGalleryImg.setImageDrawable(null)
                                        }
                                    } else {
                                        Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                                    }
                                    Log.d(TAG, "editNewPost 200: ${result.status.code}")
                                }
                                Log.d(TAG, "editNewPost null: $result")
                            } else {
                                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, response.message())
                            }
                        })
                    }
                }
            }
        }else{
            viewModel.editNewPost(
                Feed(
                    pid = feed.pid,
                    uid = feed.uid,
                    name = feed.name,
                    username = feed.username,
                    profilePicUrl = feed.profilePicUrl,
                    postPicUrl = "",
                    caption = binding.editPostCaptionContent.text.toString(),
                    comments = feed.comments,
                    likedByUsers = feed.likedByUsers,
                    postNotVisibleTo = feed.postNotVisibleTo,
                    reportCount = feed.reportCount
                )
            )
            binding.editPostCaptionContent.text?.clear()
            binding.editPostGalleryImg.setImageDrawable(null)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun createNewPost(userDetail: User) {
        if(binding.newPostGalleryImg.drawable != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val drawable = binding.newPostGalleryImg.drawable as BitmapDrawable
            val bitmap: Bitmap = drawable.bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val buzzImageByteArray = byteArrayOutputStream.toByteArray()
            val time = System.currentTimeMillis()
            val imagePath =
                storageRef!!.child(time.toString() + "logo.jpg").putBytes(buzzImageByteArray)
            imagePath.addOnCompleteListener {
                Log.d("PostImageUploaded", it.isSuccessful.toString())
                GlobalScope.launch(Dispatchers.IO) {

                    val buzzImageUrl =
                        imagePath.result.metadata!!.reference!!.downloadUrl.await()
                    Log.d("HomeFeed", "createNewPost(): $buzzImageUrl")

                    viewModel.checkNSFWResponse(buzzImageUrl.toString())
                    isNewPost = true

                    withContext(Dispatchers.Main) {

                        viewModel.isNSFWResponse.observe(viewLifecycleOwner, Observer { response->
                            if(response.isSuccessful) {
                                val result = response.body()?.results?.get(0)
                                if (result != null) {
                                    if(result.status.code == "ok") {
                                        val nsfw = result.entities[0].classes.nsfw.toDouble()
                                        if(nsfw>0.3) {
                                            Toast.makeText(context, "You are not allowed to post NSFW content!", Toast.LENGTH_SHORT).show()
                                            binding.newPostGalleryImg.setImageDrawable(null)
                                        } else if(binding.newPostCaptionContent.text.toString().isNotEmpty()){
                                            viewModel.createNewFeed(Feed(
                                                pid = System.currentTimeMillis().toString()+userDetail.uid,
                                                uid = userDetail.uid,
                                                name = userDetail.name,
                                                username = userDetail.username,
                                                profilePicUrl = userDetail.profilePicUrl,
                                                postPicUrl = buzzImageUrl.toString(),
                                                caption = binding.newPostCaptionContent.text.toString() + "\n" + binding.newPostTaggedUsernameView.text.toString(),
                                                comments = emptyMap(),
                                                likedByUsers = emptyMap(),
                                                postNotVisibleTo = emptyMap(),
                                                reportCount = 0
                                            ))

                                            binding.newPostCaptionContent.text?.clear()
                                            binding.newPostGalleryImg.setImageDrawable(null)
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, response.message())
                            }
                        })
                    }
                }
            }
        }else{
            viewModel.createNewFeed(Feed(
                pid = System.currentTimeMillis().toString()+userDetail.uid,
                uid = userDetail.uid,
                name = userDetail.name,
                username = userDetail.username,
                profilePicUrl = userDetail.profilePicUrl,
                postPicUrl = "",
                caption = binding.newPostCaptionContent.text.toString() + "\n" + binding.newPostTaggedUsernameView.text.toString(),
                comments = emptyMap(),
                likedByUsers = emptyMap(),
                postNotVisibleTo = emptyMap(),
                reportCount = 0
            ))
            binding.newPostCaptionContent.text?.clear()
            binding.newPostGalleryImg.setImageDrawable(null)
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    private fun selectImageForEditFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }

    private fun captureImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 102)
    }

    private fun captureImageFromEditCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 103)
    }

    private fun checkCameraPermissions():Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPostToCurrentUser(feed: Feed):Boolean {
        if (feed.postNotVisibleTo?.containsKey(firebaseAuth.uid.toString()) == true) return false
        Log.d(TAG, "showPostToCurrentUser(): $detailsOfUser")
        Log.d(TAG, "showPostToCurrentUser(): ${detailsOfUser.blockedUsers}\n${feed.uid}")
        Log.d(TAG, "showPostToCurrentUser(): ${detailsOfUser.reportedUsers}\n${feed.uid}")
        if (detailsOfUser.blockedUsers?.containsKey(feed.uid) == true) return false
        /*if (detailsOfUser.reportedUsers?.containsKey(feed.uid) == true) return false*/
        return true
    }

    private fun showDeletePostDialog(pId:String) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_post_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val deleteBtn = dialog.findViewById<AppCompatTextView>(R.id.delete_post_dialog_btn)
        val cancelBtn = dialog.findViewById<AppCompatTextView>(R.id.cancel_delete_post_dialog_btn)
        deleteBtn.setOnClickListener {
            viewModel.deletePost(pId)
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showBlockPostDialog(pId:String) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.block_post_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val deleteBtn = dialog.findViewById<AppCompatTextView>(R.id.block_post_dialog_btn)
        val cancelBtn = dialog.findViewById<AppCompatTextView>(R.id.cancel_block_post_dialog_btn)
        deleteBtn.setOnClickListener {
            viewModel.setPostNotVisibleTo(pId, firebaseAuth.uid.toString())
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showReportPostDialog(feed: Feed) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.report_post_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val deleteBtn = dialog.findViewById<AppCompatTextView>(R.id.report_post_dialog_btn)
        val cancelBtn = dialog.findViewById<AppCompatTextView>(R.id.cancel_report_post_dialog_btn)
        deleteBtn.setOnClickListener {
            val reportCount = feed.reportCount?.plus(1)
            if (reportCount != null) {
                if (reportCount > 5) {
                    feed.pid?.let { it1 -> viewModel.deletePost(it1) }
                } else {
                    val postNotVisibleTo = mutableMapOf<String, String>()
                    feed.postNotVisibleTo?.let { it1 ->
                        postNotVisibleTo.putAll(it1)
                        if (postNotVisibleTo.containsKey(firebaseAuth.uid.toString())) {
                            Toast.makeText(context, "You have already reported the post!", Toast.LENGTH_SHORT).show()
                        } else {
                            postNotVisibleTo[firebaseAuth.uid.toString()] = firebaseAuth.uid.toString()

                            viewModel.editNewPost(Feed(
                                pid = feed.pid,
                                uid = feed.uid,
                                name = feed.name,
                                username = feed.username,
                                profilePicUrl = feed.profilePicUrl,
                                postPicUrl = feed.postPicUrl,
                                caption = feed.caption,
                                comments = feed.comments,
                                likedByUsers = feed.likedByUsers,
                                postNotVisibleTo = postNotVisibleTo,
                                reportCount = reportCount
                            ))
                        }
                    }
                }
            }
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showBlockUserDialog(uid:String) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.block_user_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val deleteBtn = dialog.findViewById<AppCompatTextView>(R.id.block_user_dialog_btn)
        val cancelBtn = dialog.findViewById<AppCompatTextView>(R.id.cancel_block_user_dialog_btn)
        deleteBtn.setOnClickListener {
            val blockedUsers = mutableMapOf<String, Boolean>()
            detailsOfUser.blockedUsers?.let { blockedUsers.putAll(it) }
            if (blockedUsers.containsKey(uid)) {
                Toast.makeText(context, "You have already reported the User!", Toast.LENGTH_SHORT).show()
            } else {
                blockedUsers[uid] = false

                viewModel.updateUserDetails(User(
                    name = detailsOfUser.name,
                    profilePicUrl = detailsOfUser.profilePicUrl,
                    uid = detailsOfUser.uid,
                    username = detailsOfUser.username,
                    blockedUsers = blockedUsers,
                    reportedUsers = detailsOfUser.reportedUsers
                ))
                detailsOfUser = User(
                    name = detailsOfUser.name,
                    profilePicUrl = detailsOfUser.profilePicUrl,
                    uid = detailsOfUser.uid,
                    username = detailsOfUser.username,
                    blockedUsers = blockedUsers,
                    reportedUsers = detailsOfUser.reportedUsers
                )
                Log.d(TAG, detailsOfUser.toString())
                viewModel.fetchAllHomeFeeds()
            }
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showReportUserDialog(uid:String) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.report_user_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val deleteBtn = dialog.findViewById<AppCompatTextView>(R.id.report_user_dialog_btn)
        val cancelBtn = dialog.findViewById<AppCompatTextView>(R.id.cancel_report_user_dialog_btn)
        deleteBtn.setOnClickListener {
            val otherReportedUsers = mutableMapOf<String, String>()
            val blockedUsers = mutableMapOf<String, Boolean>()

            viewModel.getUserDetails(uid).addOnCompleteListener { task->
                if (task.result.exists()) {
                    val detailsOfOtherUser = task.result.getValue(User::class.java)!!

                    detailsOfOtherUser.reportedUsers?.let { otherReportedUsers.putAll(it) }
                    detailsOfUser.blockedUsers?.let { blockedUsers.putAll(it) }

                    if (otherReportedUsers.containsKey(firebaseAuth.uid.toString())) {
                        Toast.makeText(context, "You have already reported the user!", Toast.LENGTH_SHORT).show()
                    } else {
                        otherReportedUsers[firebaseAuth.uid.toString()] = firebaseAuth.uid.toString()
                        blockedUsers[uid] = true

                        viewModel.updateUserDetails(User(
                            name = detailsOfOtherUser.name,
                            profilePicUrl = detailsOfOtherUser.profilePicUrl,
                            uid = detailsOfOtherUser.uid,
                            username = detailsOfOtherUser.username,
                            blockedUsers = detailsOfOtherUser.blockedUsers,
                            reportedUsers = otherReportedUsers
                        ))
                        viewModel.updateUserDetails(User(
                            name = detailsOfUser.name,
                            profilePicUrl = detailsOfUser.profilePicUrl,
                            uid = detailsOfUser.uid,
                            username = detailsOfUser.username,
                            blockedUsers = blockedUsers,
                            reportedUsers = detailsOfUser.reportedUsers
                        ))
                        detailsOfUser = User(
                            name = detailsOfUser.name,
                            profilePicUrl = detailsOfUser.profilePicUrl,
                            uid = detailsOfUser.uid,
                            username = detailsOfUser.username,
                            blockedUsers = blockedUsers,
                            reportedUsers = detailsOfUser.reportedUsers
                        )
                        Log.d(TAG, detailsOfUser.toString())
                        viewModel.fetchAllHomeFeeds()
                    }
                }
                dialog.dismiss()
            }
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> {
                    binding.newPostGalleryImg.setImageURI(data?.data)
                }
                101 -> {
                    binding.editPostGalleryImg.setImageURI(data?.data)
                }
                102 -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    binding.newPostGalleryImg.setImageBitmap(bitmap)
                }
                103 -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    binding.editPostGalleryImg.setImageBitmap(bitmap)
                }
                else -> {}
            }
        }
    }

    override fun onNewComment(pId: String, commentContent: String) {
        val cid = System.currentTimeMillis().toString() + detailsOfUser.uid
        val comment = Comment(pid = pId, cid = cid, username = detailsOfUser.username, comment = commentContent)
        viewModel.createNewComment(pId, comment)
    }

    override fun onSelfCommentEdit(pId: String, comment: Comment) {
        viewModel.editSelfComment(pId, comment)
    }

    override fun onSelfCommentDelete(comment: Comment) {
        viewModel.deleteSelfComment(comment)
    }

    override fun isPostLikedByCurrentUser(position: Int, likedByUsers:Map<String, String>): Boolean {
        if(likedByUsers.isEmpty()) return false

        if(likedByUsers.containsKey(firebaseAuth.uid.toString())) return true;

        return false;
    }

    override fun setPostLiked(pId: String) {
        Log.d(TAG, "setPostLiked(): $pId")
        viewModel.setPostLikedByUser(pId, firebaseAuth.uid.toString())
    }

    override fun setPostNotLiked(pId: String) {
        viewModel.setPostNotLikedByUser(pId, firebaseAuth.uid.toString())
    }

    override fun repost(currentFeed: Feed) {
        binding.newBuzzButton.visibility = View.GONE
        binding.newPostContainer.visibility = View.VISIBLE

        val newRepostContent = "[Re-Post]\n[${currentFeed.name} - ${currentFeed.username}]\n${currentFeed.caption}"
        if(currentFeed.postPicUrl?.isNotEmpty() == true) {
            context?.let { Glide.with(it).load(currentFeed.postPicUrl).into(binding.newPostGalleryImg) }
        }
        binding.newPostCaptionContent.setText(newRepostContent, TextView.BufferType.NORMAL)
        // binding.fullName.setText(userDetails.name, TextView.BufferType.EDITABLE)
    }

    override fun isFeedPostedByCurrentUser(pId:String):Boolean {
        val uid = firebaseAuth.uid.toString()

        return (pId.contains(uid))
    }

    override fun deletePost(pId: String) {
        showDeletePostDialog(pId)
    }

    override fun editPost(currentFeed: Feed) {
        initEditPost(currentFeed)

        binding.newBuzzButton.visibility = View.GONE
        binding.editPostContainer.visibility = View.VISIBLE

        val editPostContent = currentFeed.caption
        if(currentFeed.postPicUrl?.isNotEmpty() == true) {
            context?.let { Glide.with(it).load(currentFeed.postPicUrl).into(binding.editPostGalleryImg) }
        }
        binding.editPostCaptionContent.setText(editPostContent, TextView.BufferType.NORMAL)
    }

    override fun onBlockPost(pId: String) {
        showBlockPostDialog(pId)
    }

    override fun onBlockUser(uid: String) {
        showBlockUserDialog(uid)
    }

    override fun onReportPost(feed: Feed) {
        showReportPostDialog(feed)
    }

    override fun onReportUser(uid: String) {
        showReportUserDialog(uid)
    }

    override fun onUserTagged(username: String) {
        val currList = binding.newPostTaggedUsernameView.text.toString()
        binding.newPostTaggedUsernameView.text = "$currList@$username\n"
    }

    override fun onUserUnTagged(username: String) {
        var currList = binding.newPostTaggedUsernameView.text.toString()
        currList = currList.replace("@$username\n", "")
        binding.newPostTaggedUsernameView.text = currList
    }
}