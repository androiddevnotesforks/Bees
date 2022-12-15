package com.iemkol.beez.presentation.userAuthentication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.iemkol.beez.R
import com.iemkol.beez.databinding.ActivityForgotPasswordBinding
import com.iemkol.beez.databinding.ActivityRegisterBinding

class ForgotPassword : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Bees)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        binding.sendForgotPassBtn.setOnClickListener {
            if(binding.emailIdLogin.text.toString().isEmpty()) {
                Snackbar.make(binding.root, "Email cannot be empty!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = binding.emailIdLogin.text.toString()
            binding.sendForgotPassBtn.visibility = View.GONE
            binding.forgotProgressBar.visibility = View.VISIBLE
            sendForgotPasswordRequest(email)
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

    private fun sendForgotPasswordRequest(email:String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task->
                if(task.isSuccessful) {
                    Toast.makeText(this@ForgotPassword, "Please check your e-mail (even your Spam/Archive folders) for reset password mail!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Snackbar.make(binding.root, task.exception?.message.toString(), Snackbar.LENGTH_LONG).show()
                }
                binding.sendForgotPassBtn.visibility = View.VISIBLE
                binding.forgotProgressBar.visibility = View.GONE
            }
    }
}