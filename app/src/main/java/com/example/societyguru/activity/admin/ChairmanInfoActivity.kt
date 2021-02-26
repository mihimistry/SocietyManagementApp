package com.example.societyguru.activity.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.activity.chairman.PaymentHistoryActivity
import com.example.societyguru.databinding.ActivityChairmanInfoBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyModel
import com.example.societyguru.model.UserModel

class ChairmanInfoActivity : AppCompatActivity(), FireAccess.MemberInfoListener,
    FireAccess.SocietyInfoListener {
    private lateinit var screen: ActivityChairmanInfoBinding
    private lateinit var userEmail: String
    private lateinit var societyId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityChairmanInfoBinding.inflate(layoutInflater)
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

        screen.tvChairmanPaymentInfo.setOnClickListener {
            val b = Bundle()
            b.putString("userEmail", userEmail)
            b.putString("societyId", societyId)
            startActivity(Intent(this, PaymentHistoryActivity::class.java).putExtras(b))
        }

        getUserInfo()
    }

    private fun getUserInfo() {
        FireAccess.getUser(userEmail, this)
        FireAccess.getSocietyInfo(societyId, this)
    }

    override fun userReceived(flag: Boolean, error: String?, model: UserModel?) {
        if (flag) {
            if (model != null) {
                screen.chairman = model
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}