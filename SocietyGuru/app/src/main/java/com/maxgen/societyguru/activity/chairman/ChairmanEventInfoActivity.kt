package com.maxgen.societyguru.activity.chairman

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxgen.societyguru.activity.member.MemberEventInfoActivity
import com.maxgen.societyguru.adapter.chairman.EventRegisteredListAdapter
import com.maxgen.societyguru.adapter.chairman.OnRegisteredEventClickListener
import com.maxgen.societyguru.databinding.ActivityChairmanEventInfoBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyEventModel
import com.maxgen.societyguru.model.member.PaidForEventModel
import com.maxgen.societyguru.utils.MyUtils

class ChairmanEventInfoActivity : AppCompatActivity(), FireAccess.OnSocietyEventInfoListener,
    OnRegisteredEventClickListener, FireAccess.OnEventRegistrationStatusListener,
    FireAccess.OnEventRegistrationInfoReceived {

    private lateinit var screen: ActivityChairmanEventInfoBinding

    private var adapter: EventRegisteredListAdapter? = null

    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityChairmanEventInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)
        screen.rvAttendingEvent.layoutManager = LinearLayoutManager(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ViewCompat.setNestedScrollingEnabled(screen.rvAttendingEvent, false)
        getEventData()
    }

    private fun getEventData() {
        val b = intent.extras
        if (b == null) {
            Toast.makeText(this, "Unknown launch", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        eventId = b.getString("id", "")
        if (eventId.isEmpty()) {
            Toast.makeText(this, "Event data not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        FireAccess.getSocietyEventInformation(eventId, this)
        FireAccess.getEventRegisteredInfo(eventId,MyUtils.getUserId(this),this)
        adapter = EventRegisteredListAdapter(FireAccess.getRegisteredEventRvOptions(eventId), this)
        screen.rvAttendingEvent.adapter = adapter
    }

    override fun eventReceived(flag: Boolean, model: SocietyEventModel?, error: String?) {
        if (model != null) {
            FireAccess.getEventRegistrationStatus(model.id, MyUtils.getUserId(this), this)
        }

        if (flag) {
            screen.event = model

            screen.btnAttendEvent.setOnClickListener {
                val b = Bundle()
                b.putString("id", model?.id)
                startActivity(
                    Intent(this, MemberEventInfoActivity::class.java).putExtras(b)
                )
            }
        } else
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }


    override fun callUser(mobile: String) {
        MyUtils.dialPerson(this, mobile)
    }

    override fun onStart() {
        adapter?.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter?.stopListening()
        super.onStop()
    }

    override fun registrationStatus(flag: Boolean) {
        if (flag) {
            screen.btnAttendEvent.visibility = View.GONE
            screen.tvEventRegistered.visibility = View.VISIBLE
        }
    }

    override fun eventRegistrationInfoReceived(model: PaidForEventModel) {
        screen.paidEvent=model
        if (model.paidDate=="")
            screen.tvEventRegistered.text = "Already Registered"
        else
            screen.tvEventRegistered.text="Registered for Event on "+model.paidDate
    }

}