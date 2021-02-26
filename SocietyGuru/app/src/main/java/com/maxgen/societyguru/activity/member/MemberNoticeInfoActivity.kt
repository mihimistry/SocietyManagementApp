package com.maxgen.societyguru.activity.member

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.databinding.ActivityMemberNoticeInfoBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.utils.MyUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MemberNoticeInfoActivity : AppCompatActivity(), FireAccess.NoticeInfoListener {

    private lateinit var screen: ActivityMemberNoticeInfoBinding
    private lateinit var noticeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMemberNoticeInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getNoticeInfo()
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
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun listen(flag: Boolean, model: NoticeModel?) {
        if (flag && model != null) {
            val fmt = SimpleDateFormat("dd/MM/yyyy  hh:mm a", Locale.ENGLISH)
            screen.tvDateTime.text = fmt.format(model.createdAt.toDate())
            screen.model = model


            val map = HashMap<String, Any>()
            map[NoticeModel.NoticeEnum.seen.name]="YES"
            FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.NOTICETO.name)
                .whereEqualTo(NoticeModel.NoticeEnum.to.name,MyUtils.getUserId(this))
                .whereEqualTo(NoticeModel.NoticeEnum.noticeId.name, model.noticeId)
                .whereEqualTo("seen","NO")
                .addSnapshotListener { value, error ->
                    if (error!=null) Log.d("MEMBER_NOTICE","ERROR:"+error.message)
                    if (value!=null&&!value.isEmpty){
                        value.documents[0].reference.update(map)
                    }
                }
        } else {
            Toast.makeText(this, "Could not find data.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

}