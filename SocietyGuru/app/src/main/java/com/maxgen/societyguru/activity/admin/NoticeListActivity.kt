package com.maxgen.societyguru.activity.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxgen.societyguru.adapter.admin.NoticeListAdapter
import com.maxgen.societyguru.adapter.admin.OnNoticeOptionClick
import com.maxgen.societyguru.databinding.ActivityNoticeListBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.utils.MyUtils

class NoticeListActivity : AppCompatActivity(), OnNoticeOptionClick {

    private lateinit var screen: ActivityNoticeListBinding
    private lateinit var adapter: NoticeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityNoticeListBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        screen.rvNotice.layoutManager = LinearLayoutManager(this)
        adapter = NoticeListAdapter(FireAccess.getNoticeRvAdapterOptions(MyUtils.getUserId(this)), this,MyUtils.getUserId(this))
        screen.rvNotice.adapter = adapter
        screen.btnAddNotice.setOnClickListener {
            startActivity(
                Intent(this@NoticeListActivity, CreateNoticeActivity::class.java)
            )
        }
    }

    override fun showNoticeInfo(model: NoticeModel) {
        val b = Bundle().apply {
            putString("id", model.noticeId)
        }
        startActivity(Intent(this, NoticeInfoActivity::class.java).putExtras(b))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onStart() {
        adapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }

}