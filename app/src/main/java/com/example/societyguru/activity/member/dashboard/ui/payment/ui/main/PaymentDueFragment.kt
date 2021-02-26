package com.example.societyguru.activity.member.dashboard.ui.payment.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.societyguru.adapter.member.PaymentDuePagerAdapter
import com.example.societyguru.databinding.FragmentPaymentDueBinding

class PaymentDueFragment : Fragment() {

    private lateinit var screen:FragmentPaymentDueBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentPaymentDueBinding.inflate(inflater, container, false)

        screen.tlDue.setupWithViewPager(screen.vpDue)

        val adapter = PaymentDuePagerAdapter(activity,childFragmentManager)
        screen.vpDue.adapter=adapter

        return screen.root
    }
}
