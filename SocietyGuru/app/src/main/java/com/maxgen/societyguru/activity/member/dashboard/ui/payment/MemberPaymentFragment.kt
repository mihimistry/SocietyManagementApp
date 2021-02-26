package com.maxgen.societyguru.activity.member.dashboard.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.maxgen.societyguru.activity.member.dashboard.ui.payment.ui.main.SectionsPagerAdapter
import com.maxgen.societyguru.databinding.FragmentMemberPaymentBinding

class MemberPaymentFragment : Fragment() {

    private lateinit var screen: FragmentMemberPaymentBinding
    private lateinit var pagerAdapter: FragmentPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentMemberPaymentBinding.inflate(inflater, container, false)
        context?.let {
            pagerAdapter =
                SectionsPagerAdapter(
                    it,
                    childFragmentManager
                )
            screen.viewPager.adapter = pagerAdapter
            screen.tabLayout.setupWithViewPager(screen.viewPager)
        }
        return screen.root
    }

}