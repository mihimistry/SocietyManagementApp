package com.example.societyguru.activity.chairman.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.societyguru.activity.chairman.EventHistoryInfoActivity
import com.example.societyguru.adapter.chairman.EventListAdapter
import com.example.societyguru.adapter.chairman.OnEventOptionClickListener
import com.example.societyguru.databinding.FragmentHistoryEventBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyEventModel

class EventHistoryFragment(private val userEmail: String,private val  societyId: String) : Fragment(), OnEventOptionClickListener {
    private lateinit var screen: FragmentHistoryEventBinding
    private var adapter: EventListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentHistoryEventBinding.inflate(inflater, container, false)

        screen.rvEventHistory.layoutManager = LinearLayoutManager(activity)
        activity?.let {

            adapter = EventListAdapter(
                userEmail,
                FireAccess.societyEventRvOptions(societyId),
                this
            )
        }
        screen.rvEventHistory.adapter = adapter
        return screen.root
    }

    override fun showEventInfo(model: SocietyEventModel) {
        val b = Bundle()
        b.putString("id", model.id)
        b.putString("userEmail",userEmail)
        context?.let {
            startActivity(Intent(it, EventHistoryInfoActivity::class.java).putExtras(b))
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