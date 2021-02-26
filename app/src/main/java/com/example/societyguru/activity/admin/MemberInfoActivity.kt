package com.example.societyguru.activity.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.databinding.ActivityMemberInfoBinding
import com.example.societyguru.enums.UserType
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyModel
import com.example.societyguru.model.UserModel

class MemberInfoActivity : AppCompatActivity(), FireAccess.MemberInfoListener,
    FireAccess.SocietyInfoListener, FireAccess.SocietyChairmanListener {
    private lateinit var screen: ActivityMemberInfoBinding
    private lateinit var userEmail: String
    private lateinit var societyId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMemberInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getUserData()
    }

    private fun getUserData() {
        val bundle = intent.extras
        if (bundle == null) {
            Toast.makeText(this, "Invalid launch.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userEmail = bundle.getString("email", "")
        societyId = bundle.getString("id", "")
        if (userEmail.isEmpty()) {
            Toast.makeText(this, "User email did not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        getUserInfo()
    }

    private fun getUserInfo() {
        FireAccess.getUser(userEmail, this)
        FireAccess.getSocietyInfo(societyId, this)
        FireAccess.getSocietyChairman(societyId, this)
    }

    override fun userReceived(flag: Boolean, error: String?, model: UserModel?) {
        if (flag) {
            if (model != null) {
                if (model.userType == UserType.CHAIRMAN.name)
                    screen.chairmanLl.visibility = View.INVISIBLE
                screen.user = model
            }
        } else
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun listenSocietyInfo(model: SocietyModel?, flag: Boolean, error: String?) {
        if (flag)
            model?.let {
                screen.societyName = it.sname
                screen.societyAddress =
                    "${it.area}, ${it.city} , ${it.state}, ${it.country} - ${it.pinCode}"
            }
        else
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun listenSocietyChairmanInfo(model: UserModel?, flag: Boolean, error: String?) {
        if (flag)
            screen.chairman = model
        else
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}