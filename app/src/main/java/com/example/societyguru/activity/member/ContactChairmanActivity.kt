package com.example.societyguru.activity.member

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.databinding.ActivityContactChairmanBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils


class ContactChairmanActivity : AppCompatActivity(), FireAccess.SocietyChairmanListener {

    private lateinit var screen: ActivityContactChairmanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityContactChairmanBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        FireAccess.getSocietyChairman(MyUtils.getUserSocietyId(this), this)

        screen.tvEmail.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "message/rfc822"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf(screen.tvEmail.text.toString().trim()))
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "There are no email clients installed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        screen.tvContact.setOnClickListener {
            val phoneIntent = Intent(Intent.ACTION_DIAL)
            phoneIntent.data = Uri.parse("tel:"
                        + screen.tvContact.text.toString().trim()
            )

            startActivity(phoneIntent)
        }
    }

    override fun listenSocietyChairmanInfo(model: UserModel?, flag: Boolean, error: String?) {
        screen.chairmanInfo = model
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}