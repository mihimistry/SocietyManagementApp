package com.maxgen.societyguru.activity.member.dashboard.ui.payment.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxgen.societyguru.activity.chairman.ChairmanEventInfoActivity
import com.maxgen.societyguru.activity.member.MemberEventInfoActivity
import com.maxgen.societyguru.adapter.chairman.OnEventOptionClickListener
import com.maxgen.societyguru.adapter.member.DueEventListAdapter
import com.maxgen.societyguru.databinding.FragmentDueEventBinding
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyEventModel
import com.maxgen.societyguru.utils.MyUtils
import kotlinx.android.synthetic.main.activity_member_dashboard.*

class DueEventFragment : Fragment(), OnEventOptionClickListener {
    private lateinit var screen: FragmentDueEventBinding
    private var adapter: DueEventListAdapter?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentDueEventBinding.inflate(inflater, container, false)

        screen.rvDueEvent.layoutManager = LinearLayoutManager(activity)
        context?.let {
            adapter = DueEventListAdapter(
                MyUtils.getUserId(it),
                FireAccess.societyEventRvOptions(MyUtils.getUserSocietyId(it)),
                this
            )
        }
        screen.rvDueEvent.adapter = adapter

        return screen.root
    }

    override fun showEventInfo(model: SocietyEventModel) {
        val b = Bundle()
        b.putString("id", model.id)
        if (context?.let { MyUtils.getUserType(it) } == UserType.CHAIRMAN.name)
            startActivity(Intent(activity, ChairmanEventInfoActivity::class.java).putExtras(b))
        else startActivity(Intent(activity, MemberEventInfoActivity::class.java).putExtras(b))
    }

    override fun onStart() {
        adapter?.startListening()
        screen = FragmentDueEventBinding.inflate(layoutInflater, container, false)

        screen.rvDueEvent.layoutManager = LinearLayoutManager(activity)
        context?.let {
            adapter = DueEventListAdapter(
                MyUtils.getUserId(it),
                FireAccess.societyEventRvOptions(MyUtils.getUserSocietyId(it)),
                this
            )
        }

        screen.rvDueEvent.adapter = adapter

        super.onStart()
    }

    override fun onStop() {
        adapter?.stopListening()
        super.onStop()
    }
}