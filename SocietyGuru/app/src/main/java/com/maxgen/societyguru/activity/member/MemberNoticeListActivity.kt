package com.maxgen.societyguru.activity.member

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxgen.societyguru.adapter.admin.NoticeListAdapter
import com.maxgen.societyguru.adapter.admin.OnNoticeOptionClick
import com.maxgen.societyguru.databinding.ActivityMemberNoticeBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.utils.MyUtils

class MemberNoticeListActivity : AppCompatActivity(),
    OnNoticeOptionClick{

    private lateinit var screen: ActivityMemberNoticeBinding
    private var adapter: NoticeListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMemberNoticeBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        screen.rvNotice.layoutManager = LinearLayoutManager(this)
        adapter = NoticeListAdapter(
            FireAccess.getNoticeReceivedRvAdapterOptions(MyUtils.getUserId(this)),
            this,
            MyUtils.getUserId(this)
        )
        screen.rvNotice.adapter = adapter
        adapter?.startListening()

    }

    override fun onStart() {
        adapter?.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter?.stopListening()
        super.onStop()
    }

    override fun showNoticeInfo(model: NoticeModel) {
        val b = Bundle()
        b.putString("id", model.noticeId)
        startActivity(
            Intent(
                this@MemberNoticeListActivity,
                MemberNoticeInfoActivity::class.java
            ).putExtras(b)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}