package com.example.societyguru.activity.chairman.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.societyguru.R
import com.example.societyguru.activity.admin.CreateNoticeActivity
import com.example.societyguru.activity.admin.NoticeInfoActivity
import com.example.societyguru.adapter.OnMaintenanceOptionClick
import com.example.societyguru.adapter.chairman.MaintenancePaidMemberListAdapter
import com.example.societyguru.adapter.sendNoticeOptionClick
import com.example.societyguru.databinding.ActivityNoticeListBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyMaintenanceModel

class MaintenancePaidMemberFragment(private val maintenanceId: String) : Fragment(),
    OnMaintenanceOptionClick,
    sendNoticeOptionClick {
    private lateinit var screen: ActivityNoticeListBinding
    private var adapter: MaintenancePaidMemberListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = ActivityNoticeListBinding.inflate(layoutInflater, container, false)

        screen.rvNotice.layoutManager = LinearLayoutManager(activity)

        adapter = MaintenancePaidMemberListAdapter(
            FireAccess.getMembersPaidMaintenanceRvAdapterOptions(maintenanceId), this
        )
        screen.rvNotice.adapter = adapter
        screen.btnAddNotice.visibility = View.GONE

        return screen.root

    }

    override fun showMaintenanceInfo(
        model: SocietyMaintenanceModel,
        counts: Int
    ) {

        val b = Bundle().apply {
            putString("id", model.maintenanceId)
        }
        startActivity(Intent(activity, NoticeInfoActivity::class.java).putExtras(b))
    }

    override fun onStart() {
        adapter?.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter?.stopListening()
        super.onStop()
    }

    override fun showOptions(model: SocietyMaintenanceModel.MaintenanceTo, anchor: View) {
        val popup = PopupMenu(activity, anchor)
        popup.inflate(R.menu.send_notice_popup)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.send_notice -> {
                    startActivity(Intent(activity, CreateNoticeActivity::class.java))
                    return@setOnMenuItemClickListener true
                }

                else -> return@setOnMenuItemClickListener false
            }
        }
      //  popup.show()
    }
}