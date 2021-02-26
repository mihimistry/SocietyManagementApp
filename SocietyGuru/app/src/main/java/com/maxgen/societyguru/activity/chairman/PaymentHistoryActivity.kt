package com.maxgen.societyguru.activity.chairman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maxgen.societyguru.adapter.chairman.ChairmanPaymentHistoryPagerAdapter
import com.maxgen.societyguru.databinding.ActivityPaymentHistoryBinding

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var adapter: ChairmanPaymentHistoryPagerAdapter
    private lateinit var screen: ActivityPaymentHistoryBinding
    private lateinit var userEmail: String
    private lateinit var societyId: String


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        screen = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras

        if (bundle != null) {
            userEmail = bundle.getString("userEmail", "")
            societyId = bundle.getString("societyId", "")

        }

        screen.tabLayout.setupWithViewPager(screen.viewPager)
        adapter = ChairmanPaymentHistoryPagerAdapter(this, supportFragmentManager, userEmail,societyId)
        screen.viewPager.adapter = adapter

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
