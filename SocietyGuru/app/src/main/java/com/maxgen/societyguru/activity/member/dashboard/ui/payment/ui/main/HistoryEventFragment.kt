package com.maxgen.societyguru.activity.member.dashboard.ui.payment.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.activity.chairman.ChairmanEventInfoActivity
import com.maxgen.societyguru.activity.member.MemberEventInfoActivity
import com.maxgen.societyguru.adapter.chairman.EventListAdapter
import com.maxgen.societyguru.adapter.chairman.OnEventOptionClickListener
import com.maxgen.societyguru.databinding.FragmentHistoryEventBinding
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyEventModel
import com.maxgen.societyguru.utils.MyUtils

class HistoryEventFragment : Fragment(), FireAccess.OnPaidEventOptionsListener,
    OnEventOptionClickListener {

    private lateinit var screen: FragmentHistoryEventBinding
    private var adapter: EventListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentHistoryEventBinding.inflate(inflater, container, false)
        context?.let { FireAccess.getPaidEventRvOptions(MyUtils.getUserId(it), this) }
        screen.rvEventHistory.layoutManager = LinearLayoutManager(context)
        return screen.root
    }

    override fun optionsReceived(options: FirestoreRecyclerOptions<SocietyEventModel>) {
        context?.let {
            adapter = EventListAdapter(MyUtils.getUserId(it), options, this)
            screen.rvEventHistory.adapter = adapter
            adapter?.startListening()
        }
    }

    override fun showEventInfo(model: SocietyEventModel) {
        val b = Bundle()
        b.putString("id", model.id)
        context?.let {
            if (MyUtils.getUserType(it) == UserType.CHAIRMAN.name)
                startActivity(Intent(it, ChairmanEventInfoActivity::class.java).putExtras(b))
            else startActivity(Intent(it, MemberEventInfoActivity::class.java).putExtras(b))
        }
    }

    override fun onStart() {
        adapter?.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter?.stopListening()
        super.onStop()
    }

}