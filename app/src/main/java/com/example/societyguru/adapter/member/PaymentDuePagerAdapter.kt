package com.example.societyguru.adapter.member

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.societyguru.activity.member.dashboard.ui.payment.ui.main.DueEventFragment
import com.example.societyguru.activity.member.dashboard.ui.payment.ui.main.DueMaintenanceFragment

class PaymentDuePagerAdapter(
    activity: FragmentActivity?,
    childFragmentManager: FragmentManager
) :FragmentPagerAdapter(childFragmentManager){
    override fun getItem(position: Int): Fragment {
        return when(position){
            0->DueEventFragment()
            1->DueMaintenanceFragment()
            else -> null!!
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0->"Event"
            1->"Maintenance"
            else->null
        }
    }
    override fun getCount(): Int {
    return 2
    }
}