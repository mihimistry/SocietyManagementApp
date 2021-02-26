package com.example.societyguru.adapter.chairman

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.societyguru.activity.chairman.fragment.MaintenanceDueMemberFragment
import com.example.societyguru.activity.chairman.fragment.MaintenancePaidMemberFragment

class MemberMaintenancePagerAdapter(
    supportFragmentManager: FragmentManager,
    private val maintenanceId: String
) : FragmentPagerAdapter(supportFragmentManager) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0-> MaintenancePaidMemberFragment(maintenanceId)
            1-> MaintenanceDueMemberFragment(maintenanceId)
            else -> null!!
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0->"Paid"
            1->"Not Paid"
            else->null
        }
    }}
