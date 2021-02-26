package com.maxgen.societyguru.activity.chairman.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxgen.societyguru.activity.admin.NoticeInfoActivity
import com.maxgen.societyguru.adapter.admin.NoticeListAdapter
import com.maxgen.societyguru.adapter.admin.OnNoticeOptionClick
import com.maxgen.societyguru.databinding.ActivityNoticeListBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.utils.MyUtils
import com.maxgen.societyguru.utils.SharedPreferenceUser

class NoticeSentFragment:Fragment(), OnNoticeOptionClick {
    private lateinit var screen: ActivityNoticeListBinding
    private  var adapter: NoticeListAdapter?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen= ActivityNoticeListBinding.inflate(layoutInflater,container,false)

        screen.rvNotice.layoutManager = LinearLayoutManager(activity)
        adapter = NoticeListAdapter(
            FireAccess.getNoticeRvAdapterOptions(SharedPreferenceUser.getInstance().getUser(activity).email),
            this, activity?.let { MyUtils.getUserId(it) }
        )
        screen.rvNotice.adapter = adapter
        screen.btnAddNotice.visibility=View.GONE

        return screen.root

    }

    override fun showNoticeInfo(model: NoticeModel) {
        val b = Bundle().apply {
            putString("id", model.noticeId)
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
}