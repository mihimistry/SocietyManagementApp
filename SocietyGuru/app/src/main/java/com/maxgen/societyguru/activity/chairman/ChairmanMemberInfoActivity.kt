package com.maxgen.societyguru.activity.chairman

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.maxgen.societyguru.databinding.ActivityChairmanMemberInfoBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.UserModel

class ChairmanMemberInfoActivity : AppCompatActivity(), FireAccess.MemberInfoListener {
    private lateinit var screen: ActivityChairmanMemberInfoBinding
    private lateinit var userEmail: String
    private lateinit var societyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityChairmanMemberInfoBinding.inflate(layoutInflater)
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

        screen.tvPaymentHistory.setOnClickListener {
            val b = Bundle()
            b.putString("userEmail", userEmail)
            b.putString("societyId", societyId)

            startActivity(Intent(this, PaymentHistoryActivity::class.java).putExtras(b))
        }
        FireAccess.getUser(userEmail, this)
    }

    override fun userReceived(flag: Boolean, error: String?, model: UserModel?) {
        if (flag) {
            if (model != null) {
                screen.user = model
            }
        } else
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}