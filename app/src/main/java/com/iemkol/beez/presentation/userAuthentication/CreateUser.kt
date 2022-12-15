package com.iemkol.beez.presentation.userAuthentication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.iemkol.beez.R
import com.iemkol.beez.databinding.ActivityCreateUserBinding

class CreateUser : AppCompatActivity() {
    private lateinit var binding:ActivityCreateUserBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private val TAG = "CreateUserActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Bees)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        binding.registerUserBtn.setOnClickListener {
            if(binding.emailIdLogin.text.toString().isEmpty()) {
                Snackbar.make(binding.root, "Email cannot be empty!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(binding.passwordLogin.text.toString().isEmpty()) {
                Snackbar.make(binding.root, "Password cannot be empty!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(binding.passwordLogin.text.toString() != binding.confirmPasswordLogin.text.toString()) {
                Snackbar.make(binding.root, "Passwords don't match!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = binding.emailIdLogin.text.toString()
            val password = binding.passwordLogin.text.toString()
            binding.registerUserBtn.visibility = View.GONE
            binding.registerUserProgressBar.visibility = View.VISIBLE
            createUserAndVerifyEmail(email, password)
        }

        binding.termsAndConditions.setOnClickListener{
            openTermsAndConditions()
        }
    }

    private fun openTermsAndConditions() {
        val openUrl = Intent(Intent.ACTION_VIEW)
        openUrl.data = Uri.parse("https://docs.google.com/document/d/1yrMjOxLh_9YPAZKNiC-r7P4Pt0GlW-i3V9MNfEjuggQ/edit?usp=sharing")
        startActivity(openUrl)
    }

    private fun createUserAndVerifyEmail(email:String, password:String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task->
                if(task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if(user!=null) {
                        user.sendEmailVerification()
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    Toast.makeText(this@CreateUser, "Please check your e-mail (even your Spam folder) for verification email!", Toast.LENGTH_LONG).show()
                                    finish()
                                }
                            }
                    } else {
                        Snackbar.make(binding.root, "Please try again!", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(binding.root, task.exception?.message.toString(), Snackbar.LENGTH_LONG).show()
                }
                binding.registerUserBtn.visibility = View.VISIBLE
                binding.registerUserProgressBar.visibility = View.GONE
            }
    }
}