package com.maxgen.societyguru.activity.chairman

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.maxgen.societyguru.databinding.ActivityChairmanProfileBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.utils.MyUtils.getUserAddress
import com.maxgen.societyguru.utils.SharedPreferenceUser

class ChairmanProfileActivity : AppCompatActivity(), FireAccess.SocietyInfoListener {

    private lateinit var screen: ActivityChairmanProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityChairmanProfileBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
        screen.user = SharedPreferenceUser.getInstance().getUser(this)
        screen.imgEdit.setOnClickListener {
            startActivity(Intent(this@ChairmanProfileActivity, EditProfileActivity::class.java))
            finish()
        }
        FireAccess.getSocietyInfo(SharedPreferenceUser.getInstance().getUser(this).societyId, this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun listenSocietyInfo(model: SocietyModel?, flag: Boolean, error: String?) {
        if (flag)
            model?.let {
                screen.societyName = it.sname
                screen.societyAddress = getUserAddress(this, it)
            }
        else Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}