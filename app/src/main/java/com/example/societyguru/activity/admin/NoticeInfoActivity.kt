package com.example.societyguru.activity.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.societyguru.adapter.admin.NoticeSentToListAdapter
import com.example.societyguru.adapter.admin.OnNoticeSentToOptionClick
import com.example.societyguru.databinding.ActivityNoticeInfoBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.NoticeModel
import com.example.societyguru.utils.MyUtils
import java.text.SimpleDateFormat
import java.util.*


class NoticeInfoActivity : AppCompatActivity(), FireAccess.NoticeInfoListener,
    OnNoticeSentToOptionClick {

    private lateinit var screen: ActivityNoticeInfoBinding
    private lateinit var adapter: NoticeSentToListAdapter
    private lateinit var noticeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityNoticeInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getNoticeInfo()
        ViewCompat.setNestedScrollingEnabled(screen.rvNoticeSentTo, false)
    }

    private fun getNoticeInfo() {
        val b = intent.extras
        if (b == null) {
            Toast.makeText(this, "Invalid launch", Toast.LENGTH_SHORT).show()
            return
        }
        noticeId = b.getString("id", "")
        if (noticeId.isEmpty()) {
            Toast.makeText(this, "Could not find data.", Toast.LENGTH_SHORT).show()
            return
        }
        FireAccess.getNoticeInfo(noticeId, this)
        getNoticeSentTo()
    }

    private fun getNoticeSentTo() {
        adapter = NoticeSentToListAdapter(FireAccess.getNoticeSentToRvOptions(noticeId), this)
        screen.rvNoticeSentTo.layoutManager = LinearLayoutManager(this)
        screen.rvNoticeSentTo.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun listen(flag: Boolean, model: NoticeModel?) {
        if (flag && model != null) {
            val fmt = SimpleDateFormat("hh:mm a (dd/MM/yyyy)", Locale.ENGLISH)
            screen.tvDateTime.text = fmt.format(model.createdAt.toDate())
            screen.model = model

        } else {
            Toast.makeText(this, "Could not find data.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun dialPerson(phone: String) {
        MyUtils.dialPerson(this, phone)
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