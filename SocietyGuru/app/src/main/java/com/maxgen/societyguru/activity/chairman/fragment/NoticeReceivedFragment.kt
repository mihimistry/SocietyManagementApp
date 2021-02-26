package com.maxgen.societyguru.activity.chairman.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxgen.societyguru.activity.member.MemberNoticeInfoActivity
import com.maxgen.societyguru.adapter.admin.NoticeListAdapter
import com.maxgen.societyguru.adapter.admin.OnNoticeOptionClick
import com.maxgen.societyguru.databinding.ActivityNoticeListBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.utils.MyUtils

class NoticeReceivedFragment : Fragment(),
    //FireAccess.OnMemberRvOptionsCreatedListener,
    OnNoticeOptionClick {
    private lateinit var screen: ActivityNoticeListBinding
    private var adapter: NoticeListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = ActivityNoticeListBinding.inflate(layoutInflater, container, false)

        screen.rvNotice.layoutManager = LinearLayoutManager(activity)
        screen.btnAddNotice.visibility = View.GONE

        activity?.let {adapter = NoticeListAdapter(
            FireAccess.getNoticeReceivedRvAdapterOptions(
                MyUtils.getUserId(
                    it
                )
            ), this,MyUtils.getUserId(it)
        )}

        screen.rvNotice.adapter = adapter
        return screen.root

    }

    override fun showNoticeInfo(model: NoticeModel) {
        val b = Bundle()
        b.putString("id", model.noticeId)
        startActivity(
            Intent(
                activity,
                MemberNoticeInfoActivity::class.java
            ).putExtras(b)
        )
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