package com.example.societyguru.activity.chairman

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.activity.admin.CreateNoticeActivity
import com.example.societyguru.adapter.chairman.NoticePagerAdapter
import com.example.societyguru.databinding.ActivityChairmanNoticeBinding

class ChairmanNoticeActivity : AppCompatActivity() {
    private lateinit var screen:ActivityChairmanNoticeBinding
    private lateinit var adapter:NoticePagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen= ActivityChairmanNoticeBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter= NoticePagerAdapter(supportFragmentManager)
        screen.viewPager.adapter=adapter
        screen.tabLayout.setupWithViewPager(screen.viewPager)

        screen.btnAddNotice.visibility=View.VISIBLE
        screen.btnAddNotice.setOnClickListener {
            startActivity(
                Intent(this, CreateNoticeActivity::class.java)
            )
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}