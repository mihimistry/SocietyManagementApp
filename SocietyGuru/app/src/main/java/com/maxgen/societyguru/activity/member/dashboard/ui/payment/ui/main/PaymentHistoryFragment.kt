package com.maxgen.societyguru.activity.member.dashboard.ui.payment.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.maxgen.societyguru.adapter.member.PaymentHistoryPagerAdapter
import com.maxgen.societyguru.databinding.FragmentPaymentHistoryBinding


class PaymentHistoryFragment : Fragment(){
    private lateinit var screen:FragmentPaymentHistoryBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen= FragmentPaymentHistoryBinding.inflate(inflater,container,false)

        screen.tlHistory.setupWithViewPager(screen.vpHistory)

        val adapter = PaymentHistoryPagerAdapter(activity,childFragmentManager)
        screen.vpHistory.adapter=adapter

        return screen.root
    }

}