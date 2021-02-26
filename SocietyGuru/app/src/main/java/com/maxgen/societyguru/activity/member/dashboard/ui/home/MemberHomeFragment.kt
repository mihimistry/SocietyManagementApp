package com.maxgen.societyguru.activity.member.dashboard.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.R
import com.maxgen.societyguru.activity.chairman.EventListActivity
import com.maxgen.societyguru.activity.chairman.MaintenanceListActivity
import com.maxgen.societyguru.activity.member.ContactChairmanActivity
import com.maxgen.societyguru.activity.member.MemberNoticeListActivity
import com.maxgen.societyguru.adapter.member.MemberDashAdapter
import com.maxgen.societyguru.databinding.FragmentMemberHomeBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.model.SocietyEventModel
import com.maxgen.societyguru.model.member.DashModel
import com.maxgen.societyguru.utils.MyUtils

class MemberHomeFragment : Fragment() {

    private lateinit var screen: FragmentMemberHomeBinding
    private lateinit var memberHomeViewModel: MemberHomeViewModel

    var adapter: MemberDashAdapter? = null
    var dashList = ArrayList<DashModel>()
    private var nCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentMemberHomeBinding.inflate(inflater, container, false)

        screen.tvContactChairman.setOnClickListener {
            startActivity(Intent(activity, ContactChairmanActivity::class.java))
        }
        memberHomeViewModel = ViewModelProviders.of(this).get(MemberHomeViewModel::class.java)

        screen.cvMaintenance.setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    MaintenanceListActivity::class.java
                )
            )
        }

        screen.cvEvent.setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    EventListActivity::class.java
                )
            )

        }

        screen.cvNotice.setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    MemberNoticeListActivity::class.java
                )
            )
        }



        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.NOTICETO.name)
            .whereEqualTo(NoticeModel.NoticeEnum.to.name, activity?.let { MyUtils.getUserId(it) })
            .whereEqualTo(NoticeModel.NoticeEnum.seen.name, "NO")
            .addSnapshotListener { value, error ->
                if (error != null) Log.d("N_SEEN", "ERROR:" + error.message)
                if (value != null && !value.isEmpty) {
                    screen.noticeBadge.visibility=View.VISIBLE
                    screen.noticeBadge.text = value.count().toString()
                } else screen.noticeBadge.visibility = View.GONE
            }

        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(NoticeModel.NoticeEnum.to.name, activity?.let { MyUtils.getUserId(it) })
            .whereEqualTo(NoticeModel.NoticeEnum.seen.name, "NO")
            .addSnapshotListener { value, error ->
                if (error != null) Log.d("M_SEEN", "ERROR:" + error.message)
                if (value != null && !value.isEmpty) {
                    screen.maintenanceBadge.visibility=View.VISIBLE
                    screen.maintenanceBadge.text = value.count().toString()
                } else screen.maintenanceBadge.visibility = View.GONE
            }

        FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.EVENTTO.name)
            .whereEqualTo(SocietyEventModel.EventEnum.to.name, activity?.let { MyUtils.getUserId(it) })
            .whereEqualTo(SocietyEventModel.EventEnum.seen.name, "NO")
            .addSnapshotListener { value, error ->
                if (error != null) Log.d("E_SEEN", "ERROR:" + error.message)
                if (value != null && !value.isEmpty) {
                    screen.eventBadge.visibility=View.VISIBLE
                    screen.eventBadge.text = value.count().toString()
                } else screen.eventBadge.visibility = View.GONE
            }

        dashList.add(
            DashModel(
                "Maintenance",
                R.drawable.ic_maintenance, 0
            )
        )

        dashList.add(
            DashModel(
                "Events",
                R.drawable.ic_calendar, 0
            )
        )

        dashList.add(
            DashModel(
                "Notices",
                R.drawable.ic_notices, 0
            )
        )

        adapter = MemberDashAdapter(activity, dashList)
        screen.gridView.adapter = adapter
        return screen.root
    }

}

