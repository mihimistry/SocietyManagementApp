package com.example.societyguru.activity.chairman.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.societyguru.activity.admin.NoticeInfoActivity
import com.example.societyguru.adapter.admin.NoticeListAdapter
import com.example.societyguru.adapter.admin.OnNoticeOptionClick
import com.example.societyguru.databinding.ActivityNoticeListBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.NoticeModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.SharedPreferenceUser

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