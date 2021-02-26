package com.example.societyguru.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.example.societyguru.activity.admin.AdminDashboardActivity
import com.example.societyguru.activity.chairman.ChairmanDashboardActivity
import com.example.societyguru.activity.member.dashboard.MemberDashboardActivity
import com.example.societyguru.databinding.ActivityLoginBinding
import com.example.societyguru.enums.FirebaseCollectionName
import com.example.societyguru.enums.UserStatus
import com.example.societyguru.enums.UserType
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.MyUtils.dismissProgress
import com.example.societyguru.utils.MyUtils.getEDTText
import com.example.societyguru.utils.SharedPreferenceUser

class LoginActivity : AppCompatActivity() {

    private lateinit var screen: ActivityLoginBinding

    private val email get() = getEDTText(screen.edtEmail)
    private val pass get() = getEDTText(screen.edtPass)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(screen.root)

        screen.btnLogin.setOnClickListener {
            validateAndLogin()
        }
        screen.tvRegister.setOnClickListener {
            startActivity(Intent(applicationContext, RegistrationActivity::class.java))
        }
        screen.tvForgotPass.setOnClickListener {
            startActivity(Intent(applicationContext, ForgotPasswordActivity::class.java))
        }
    }

    private fun validateAndLogin() {
        var b = true
        if (!MyUtils.isNetworkAvailable(applicationContext)) {
            b = false
            Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            screen.edtEmail.error = "Enter valid email."
            b = false
        }
        if (pass.isEmpty()) {
            screen.edtPass.error = "Enter Password"
            b = false
        }
        if (b) loginUser()
    }

    private fun loginUser() {
        MyUtils.showProgress(this, "Logging you in", null, false)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (email == "admin@gmail.com") {
                        dismissProgress()
                        Toast.makeText(applicationContext, "Login success.", Toast.LENGTH_SHORT)
                            .show()
                        SharedPreferenceUser.getInstance()
                            .userLogin(
                                UserModel("", "", email, "", pass, UserType.ADMIN.name),
                                this@LoginActivity
                            )
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                AdminDashboardActivity::class.java
                            )
                        )
                        finish()
                    } else getUserInfo()
                } else {
                    dismissProgress()
                    Toast.makeText(
                        applicationContext,
                        it.exception.toString(),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
    }

    private fun getUserInfo() {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(email)
            .get()
            .addOnSuccessListener {
                dismissProgress()
                if (it != null && it.exists()) validateUser(it)
                else {
                    Toast.makeText(
                        this@LoginActivity,
                        "User Data is not stored.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val b=Bundle()
                    b.putString("userEmail",screen.edtEmail.text.toString())
                    b.putString("userPass",screen.edtPass.text.toString())

                    startActivity(Intent(this,RegisterForNewSocietyActivity::class.java).putExtras(b))
                }
            }.addOnFailureListener {
                dismissProgress()
                Toast.makeText(this@LoginActivity, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateUser(it: DocumentSnapshot) {
        if (it[UserModel.UserEnum.status.name] == UserStatus.ACTIVE.name)
            when (it[UserModel.UserEnum.userType.name]) {
                UserType.CHAIRMAN.name -> {
                    val user = it.toObject(UserModel::class.java)
                    user?.token = "${FirebaseInstanceId.getInstance().token}"
                    SharedPreferenceUser.getInstance()

                        .userLogin(
                            user,
                            this@LoginActivity
                        )
                    Toast.makeText(
                        this@LoginActivity,
                        "Chairman login success.",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            ChairmanDashboardActivity::class.java
                        )
                    )
                    FireAccess.updateUserToken(
                        email,
                        "${FirebaseInstanceId.getInstance().token}"
                    )
                    finish()
                }
                UserType.SOCIETY_MEMBER.name -> {
                    val user = it.toObject(UserModel::class.java)
                    user?.token = "${FirebaseInstanceId.getInstance().token}"
                    SharedPreferenceUser.getInstance().userLogin(user, this@LoginActivity)
                    Toast.makeText(
                        this@LoginActivity,
                        "Member Login success.",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@LoginActivity, MemberDashboardActivity::class.java))
                    finish()
                    FireAccess.updateUserToken(
                        email,
                        "${FirebaseInstanceId.getInstance().token}"
                    )
                }
                else -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Invalid user.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        else {
            if (it[UserModel.UserEnum.userType.name] == UserType.CHAIRMAN.name) Toast.makeText(
                this@LoginActivity,
                "Your account is ${it[UserModel.UserEnum.status.name]}. Please contact Admin to activate your account.",
                Toast.LENGTH_LONG
            ).show()
            else Toast.makeText(
                this@LoginActivity,
                "Your account is ${it[UserModel.UserEnum.status.name]}. Please contact chairman to activate your account.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}