package com.maxgen.societyguru.activity.chairman

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.maxgen.societyguru.databinding.ActivityEventHistoryInfoBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyEventModel
import com.maxgen.societyguru.model.member.PaidForEventModel

class EventHistoryInfoActivity : AppCompatActivity(), FireAccess.OnSocietyEventInfoListener,
    FireAccess.OnEventRegistrationInfoReceived,FireAccess.OnEventRegistrationStatusListener {

    private lateinit var screen: ActivityEventHistoryInfoBinding
    private var eventId: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityEventHistoryInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)

        val b = intent.extras
        if (b == null) {
            Toast.makeText(this, "Unknown launch", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        eventId = b.getString("id", "")
        userEmail = b.getString("userEmail", "")

        if (eventId.isEmpty()) {
            Toast.makeText(this, "Event data not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        FireAccess.getSocietyEventInformation(eventId, this)
        FireAccess.getEventRegisteredInfo(eventId,userEmail, this)
        FireAccess.getEventRegistrationStatus(eventId,userEmail,this)
    }

    override fun eventReceived(flag: Boolean, model: SocietyEventModel?, error: String?) {
        screen.event = model
    }

    override fun eventRegistrationInfoReceived(model: PaidForEventModel) {
        screen.eventPaid = model
        if (model.paidDate=="")
            screen.registerationDateLl.visibility=View.GONE
        else
            screen.registerationDateLl.visibility=View.VISIBLE
    }

    override fun registrationStatus(flag: Boolean) {
        if (flag){
            screen.chargesPaidLl.visibility = View.VISIBLE
            screen.chargesLl.visibility = View.GONE
            screen.tvRegistrationStatus.text="Registred for Event"
        }
        else
            screen.tvRegistrationStatus.text="Not Registered"
    }
}