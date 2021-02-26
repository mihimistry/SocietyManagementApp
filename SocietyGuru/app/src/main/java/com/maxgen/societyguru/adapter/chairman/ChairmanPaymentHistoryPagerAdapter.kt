package com.maxgen.societyguru.adapter.chairman

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.maxgen.societyguru.activity.chairman.PaymentHistoryActivity
import com.maxgen.societyguru.activity.chairman.fragment.EventHistoryFragment
import com.maxgen.societyguru.activity.chairman.fragment.MaintenanceHistoryFragment

class ChairmanPaymentHistoryPagerAdapter(
    Context: PaymentHistoryActivity,
    supportFragmentManager: FragmentManager,
    private val userEmail: String,
    private val societyId: String
):FragmentPagerAdapter(supportFragmentManager) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> EventHistoryFragment(userEmail,societyId)
            1 -> MaintenanceHistoryFragment(userEmail,societyId)
            else -> null!!
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Event"
            1 -> "Maintenance"
            else -> null
        }
    }


}