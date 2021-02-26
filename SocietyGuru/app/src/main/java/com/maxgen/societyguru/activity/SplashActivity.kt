package com.maxgen.societyguru.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.maxgen.societyguru.activity.admin.AdminDashboardActivity
import com.maxgen.societyguru.activity.chairman.ChairmanDashboardActivity
import com.maxgen.societyguru.activity.member.dashboard.MemberDashboardActivity
import com.maxgen.societyguru.databinding.ActivitySplashBinding
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.utils.SharedPreferenceUser

class SplashActivity : AppCompatActivity() {

    private lateinit var screen: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(screen.root)

        Handler().postDelayed({
            validateLogin()
        }, 2000)

    }

    private fun validateLogin() {
        val user = SharedPreferenceUser.getInstance().getUser(this)
        if (user.email.isBlank() || user.email.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            when (user.userType) {
                UserType.ADMIN.name -> {
                    startActivity(Intent(this@SplashActivity, AdminDashboardActivity::class.java))
                    finish()
                }
                UserType.CHAIRMAN.name -> {
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            ChairmanDashboardActivity::class.java
                        )
                    )
                    finish()
                }
                UserType.SOCIETY_MEMBER.name -> {
                    startActivity(Intent(this@SplashActivity, MemberDashboardActivity::class.java))
                    finish()
                }
                else -> {
                    Toast.makeText(this, "Unknown user.", Toast.LENGTH_SHORT).show()
                    SharedPreferenceUser.getInstance().logout(this)
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

}