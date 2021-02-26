package com.example.societyguru.activity.member

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.databinding.ActivityMemberEventsBinding

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