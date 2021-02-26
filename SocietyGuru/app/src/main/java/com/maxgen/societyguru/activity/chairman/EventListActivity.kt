package com.maxgen.societyguru.activity.chairman

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxgen.societyguru.activity.member.MemberEventInfoActivity
import com.maxgen.societyguru.adapter.chairman.EventListAdapter
import com.maxgen.societyguru.adapter.chairman.OnEventOptionClickListener
import com.maxgen.societyguru.databinding.ActivityEventListBinding
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyEventModel
import com.maxgen.societyguru.utils.MyUtils

class EventListActivity : AppCompatActivity(), OnEventOptionClickListener {

    private lateinit var screen: ActivityEventListBinding
    private lateinit var adapter: EventListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        screen.btnAddEvent.setOnClickListener {
            startActivity(
                Intent(
                    this@EventListActivity,
                    CreateEventActivity::class.java
                )
            )
        }



        screen.rvEvent.layoutManager = LinearLayoutManager(this)
        adapter = EventListAdapter(
            MyUtils.getUserId(this),
            FireAccess.societyEventRvOptions(MyUtils.getUserSocietyId(this)),
            this
        )

        screen.rvEvent.adapter = adapter

        screen.btnAddEvent.visibility =
            if (MyUtils.getUserType(this) == UserType.SOCIETY_MEMBER.name) View.GONE else View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun showEventInfo(model: SocietyEventModel) {
        val b = Bundle()
        b.putString("id", model.id)
        if (MyUtils.getUserType(this) == UserType.CHAIRMAN.name)
            startActivity(Intent(this, ChairmanEventInfoActivity::class.java).putExtras(b))
        else startActivity(Intent(this, MemberEventInfoActivity::class.java).putExtras(b))
    }

    override fun onStart() {
        adapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }

}