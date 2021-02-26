package com.example.societyguru.activity.member.dashboard

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.databinding.ActivityRegisteredEventInfoBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyEventModel
import com.example.societyguru.model.member.PaidForEventModel
import com.example.societyguru.utils.MyUtils

class RegisteredEventInfoActivity : AppCompatActivity(),
    FireAccess.OnEventRegistrationInfoReceived, FireAccess.OnSocietyEventInfoListener {

    private lateinit var screen: ActivityRegisteredEventInfoBinding

    private var eventId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityRegisteredEventInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getEventInfo()
    }

    private fun getEventInfo() {
        val b = intent.extras
        b?.let {
            eventId = it.getString("id", "")
            if (eventId.isNotEmpty()) {
                FireAccess.getEventRegisteredInfo(eventId, MyUtils.getUserId(this), this)
                FireAccess.getSocietyEventInformation(eventId, this)
            }
        }
        if (b == null) {
            Toast.makeText(this, "Unknown launch.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun eventRegistrationInfoReceived(model: PaidForEventModel) {
        screen.model = model

    }

    override fun eventReceived(flag: Boolean, model: SocietyEventModel?, error: String?) {
        if (flag) screen.event = model
        else Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}