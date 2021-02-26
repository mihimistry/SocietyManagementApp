package com.example.societyguru.activity.admin

import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.societyguru.adapter.admin.NoticeSendToListAdapter
import com.example.societyguru.adapter.admin.OnNoticeSentToOptionClick
import com.example.societyguru.databinding.ActivityCreateNoticeBinding
import com.example.societyguru.enums.UserType
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.NoticeModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.MyUtils.dismissProgress
import com.example.societyguru.utils.MyUtils.getEDTText
import com.example.societyguru.utils.MyUtils.isNetworkAvailable
import com.example.societyguru.utils.MyUtils.setEDTError
import com.example.societyguru.utils.MyUtils.showProgress
import com.example.societyguru.utils.SharedPreferenceUser


class CreateNoticeActivity : AppCompatActivity(), OnNoticeSentToOptionClick,
    FireAccess.NoticeCreatedListener {

    private lateinit var screen: ActivityCreateNoticeBinding
    private lateinit var adapter: NoticeSendToListAdapter

    private val title get() = getEDTText(screen.edtTitle)
    private val desc get() = getEDTText(screen.edtDesc)

    private val KEY_RECYCLER_STATE = "recycler_state"
    private val mRecyclerView: RecyclerView? = null
    private var mBundleRecyclerViewState: Bundle? = null
    private var listState: Parcelable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityCreateNoticeBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        screen.rvSendToSelect.layoutManager = LinearLayoutManager(this)
        adapter =
            if (SharedPreferenceUser.getInstance().getUser(this).userType == UserType.CHAIRMAN.name)
                NoticeSendToListAdapter(
                    FireAccess.getSocietyMemberRvCheckboxOptions(
                        SharedPreferenceUser.getInstance().getUser(this).societyId
                    ), this
                )
            else
                NoticeSendToListAdapter(FireAccess.getChairmanRvOptions(), this)

        screen.checkAll.setOnCheckedChangeListener { _, b ->
            adapter.models.forEach { model ->
                adapter.selectedModels.clear()
                model.checked = b
                adapter.notifyDataSetChanged()
            }
        }

        screen.btnSendNotice.setOnClickListener {
            if (isNetworkAvailable(this@CreateNoticeActivity)) validateAndCreateNotice()
            else Toast.makeText(
                this@CreateNoticeActivity,
                "No internet connections.",
                Toast.LENGTH_SHORT
            ).show()
        }

        screen.rvSendToSelect.adapter = adapter
    }

    private fun validateAndCreateNotice() {
        var flag = true
        if (title.isEmpty()) {
            flag = true
            setEDTError(screen.edtTitle, "Please enter title.")
        }
        if (desc.isEmpty()) {
            flag = false
            setEDTError(screen.edtDesc, "Please enter description.")
        }
        if (adapter.selectedModels.size < 1) {
            flag = false
            Toast.makeText(this, "Please select the chairman to send notice.", Toast.LENGTH_SHORT)
                .show()
        }
        if (flag) createNotice()
    }

    private fun createNotice() {
        val model = NoticeModel(
            title = title,
            description = desc,
            from = SharedPreferenceUser.getInstance().getUser(this).email
        )

        val noticeTo: ArrayList<NoticeModel.NoticeTo> = ArrayList()
        for (chairman in adapter.selectedModels) noticeTo.add(
            NoticeModel.NoticeTo(
                chairman.email,
                MyUtils.getUserId(this),
                title,
                desc,
                "NO",
                "NO"
            )
        )
        showProgress(this, "Sending Notice", null, false)
        FireAccess.createNotice(model, noticeTo, this)
    }

    override fun noticeCreated(flag: Boolean, error: String?) {
        dismissProgress()
        if (flag) {
            Toast.makeText(this, "Notice sent.", Toast.LENGTH_SHORT).show()
            finish()
        } else Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun dialPerson(phone: String) {
        MyUtils.dialPerson(this, phone)
    }

    override fun onStart() {
        adapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}