package com.example.societyguru.activity

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.societyguru.databinding.ActivityForgotPasswordBinding
import com.example.societyguru.enums.FirebaseCollectionName
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.MyUtils.dismissProgress
import com.example.societyguru.utils.MyUtils.getEDTText

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var screen: ActivityForgotPasswordBinding

    private val email get() = getEDTText(screen.edtEmail)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(screen.root)
        screen.btnSendEmail.setOnClickListener { validateAndSendEmail() }
        screen.tvLogin.setOnClickListener { finish() }
    }

    private fun validateAndSendEmail() {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            MyUtils.showProgress(this, "Sending Reset Link to Email", null, false)
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
                .document(email)
                .get()
                .addOnSuccessListener {
                    val user = it.toObject(UserModel::class.java)
                    if (user == null) {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "User did not registered.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else sendEmail()
                }
                .addOnFailureListener {
                    dismissProgress()
                    Toast.makeText(this@ForgotPasswordActivity, "${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else screen.edtEmail.error = "Invalid Email Id"
    }

    private fun sendEmail() {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener {
            dismissProgress()
            Toast.makeText(
                this@ForgotPasswordActivity,
                "Password reset Link sent.\nPlease check your mail.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this@ForgotPasswordActivity, "${it.message}", Toast.LENGTH_SHORT).show()
            dismissProgress()
        }
    }

}