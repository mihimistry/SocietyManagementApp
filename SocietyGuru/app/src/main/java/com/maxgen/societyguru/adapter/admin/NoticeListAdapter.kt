package com.maxgen.societyguru.adapter.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.databinding.NoticeListViewBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.utils.MyUtils

class NoticeListAdapter(
    options: FirestoreRecyclerOptions<NoticeModel>,
    private val optionClick: OnNoticeOptionClick,
    private val userId: String?=null
) : FirestoreRecyclerAdapter<NoticeModel, NoticeListAdapter.AdapterViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder =
        AdapterViewHolder(
            NoticeListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int, model: NoticeModel) =
        holder.setData(model, optionClick,userId!!)

    class AdapterViewHolder(val view: NoticeListViewBinding) : RecyclerView.ViewHolder(view.root),
        FireAccess.OnNoticeSeenListner {
        fun setData(
            model: NoticeModel,
            optionClick: OnNoticeOptionClick,
            userId: String
        ) {
            view.model = model
            view.root.setOnClickListener { optionClick.showNoticeInfo(model) }
            FireAccess.getNotSeenNotices(model.noticeId,userId,this)
        }

        override fun notSeenNotices(flag: Boolean, error: String?) {
            if (flag) view.tvNew.visibility = View.VISIBLE
            else view.tvNew.visibility = View.GONE
        }
    }

}