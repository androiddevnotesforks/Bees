package com.iemkol.beez.presentation.userAuthentication

import com.iemkol.beez.R
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.iemkol.beez.databinding.ActivityLoginBinding
/*import com.iem.bees.databinding.ActivityLoginBinding*/
import com.iemkol.beez.presentation.homeFeed.HomeActivity
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private var currentUser:FirebaseUser?=null
    private val TAG = "LoginActivity"

//    lateinit var storedVerificationId:String
//    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Bees)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        binding.loginBtn.setOnClickListener {
            if(binding.emailIdLogin.text.toString().isEmpty() || binding.passwordLogin.text.toString().length<6) {
                Snackbar.make(binding.root, "Invalid e-mail and/or password!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = binding.emailIdLogin.text.toString()
            val password = binding.passwordLogin.text.toString()
            binding.loginBtn.visibility = View.GONE
            binding.loginProgressBar.visibility = View.VISIBLE
            signInUserWithEmailPassword(email, password)
        }

        binding.registerBtn.setOnClickListener {
            startActivity(Intent(this@Login, CreateUser::class.java))
        }

        binding.forgotPasswordBtn.setOnClickListener {
            startActivity(Intent(this@Login, ForgotPassword::class.java))
        }

        binding.termsAndConditions.setOnClickListener{
            openTermsAndConditions()
        }

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = firebaseAuth.currentUser
        Log.d(TAG, "onStart(): $currentUser")
        currentUser?.let { updateUI(it) }
    }

//    private fun sendVerificationCode(number: String) {
//        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
//            .setPhoneNumber(number) // Phone number to verify
//            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//            .setActivity(this) // Activity (for callback binding)
//            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
//            .build()
//        PhoneAuthProvider.verifyPhoneNumber(options)
//    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@Login,"Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this , HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this,"Wrong OTP entered!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun signInUserWithEmailPassword(email:String, password:String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task->
                if(task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        updateUI(user)
                    } else {
                        binding.loginBtn.visibility = View.VISIBLE
                        binding.loginProgressBar.visibility = View.GONE
                        Snackbar.make(binding.root, "Please try again!", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    /*Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()*/
                    binding.loginBtn.visibility = View.VISIBLE
                    binding.loginProgressBar.visibility = View.GONE
                    Snackbar.make(binding.root, task.exception?.message.toString(), Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    private fun openTermsAndConditions() {
        val openUrl = Intent(android.content.Intent.ACTION_VIEW)
        openUrl.data = Uri.parse("https://docs.google.com/document/d/1yrMjOxLh_9YPAZKNiC-r7P4Pt0GlW-i3V9MNfEjuggQ/edit?usp=sharing")
        startActivity(openUrl)
    }

//    private fun checkUserDetails() {
//        userViewModel.getUserDetails(firebaseAuth.uid.toString())
//            .addOnCompleteListener { task->
//                if(task.result.exists()) {
//                    startActivity(Intent(this@Login, HomeActivity::class.java))
//                    finish()
//                } else {
//                    startActivity(Intent(this@Login, Register::class.java))
//                    finish()
//                }
//            }
//    }

    private fun updateUI(firebaseUser: FirebaseUser) {
        if(firebaseUser.isEmailVerified) {
            startActivity(Intent(this@Login, HomeActivity::class.java))
            finish()
        }
    }
}