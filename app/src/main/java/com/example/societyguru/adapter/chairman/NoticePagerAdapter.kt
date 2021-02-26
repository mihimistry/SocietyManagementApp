package com.example.societyguru.adapter.chairman

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.societyguru.activity.chairman.fragment.NoticeReceivedFragment
import com.example.societyguru.activity.chairman.fragment.NoticeSentFragment

class NoticePagerAdapter(supportFragmentManager: FragmentManager) : FragmentPagerAdapter(supportFragmentManager) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0->NoticeSentFragment()
            1->NoticeReceivedFragment()
            else -> null!!
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0->"Sent"
            1->"Received"
            else->null
        }
    }
}