package com.example.societyguru.activity.chairman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.adapter.chairman.MemberMaintenancePagerAdapter
import com.example.societyguru.databinding.ActivityChairmanNoticeBinding

class MemberMaintenancePaymentListActivity : AppCompatActivity() {
    private lateinit var screen: ActivityChairmanNoticeBinding
    private lateinit var adapter:MemberMaintenancePagerAdapter
    private var maintenanceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen= ActivityChairmanNoticeBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val b = intent.extras
        if (b != null) {
            maintenanceId=b.getString("maintenanceId","")
        }

        adapter= MemberMaintenancePagerAdapter(supportFragmentManager,maintenanceId)
        screen.viewPager.adapter=adapter
        screen.tabLayout.setupWithViewPager(screen.viewPager)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}