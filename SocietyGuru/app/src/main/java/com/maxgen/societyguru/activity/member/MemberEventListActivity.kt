package com.maxgen.societyguru.activity.member

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maxgen.societyguru.databinding.ActivityMemberEventsBinding

class MemberEventListActivity : AppCompatActivity() {

    private lateinit var screen: ActivityMemberEventsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMemberEventsBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}