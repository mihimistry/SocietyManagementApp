package com.example.societyguru.adapter.member

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.societyguru.R
import com.example.societyguru.activity.chairman.EventListActivity
import com.example.societyguru.activity.chairman.MaintenanceListActivity
import com.example.societyguru.activity.member.MemberNoticeListActivity
import com.example.societyguru.model.member.DashModel
import kotlinx.android.synthetic.main.member_dash_view.view.*

class MemberDashAdapter(context: Context?, foodsList: ArrayList<DashModel>) :
    BaseAdapter() {

    var dashList = foodsList
    var context: Context? = context

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val dash = this.dashList[p0]
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dashView = inflater.inflate(R.layout.member_dash_view, null)
        dashView.image.setImageResource(dash.image!!)
        dashView.tv_title.text = dash.name!!
        if (dash.count==0){
            dashView.item_badge.visibility=View.GONE
        }
        dashView.item_badge.text= dash.count.toString()
        dashView.setOnClickListener {
            when (dashView.tv_title.text) {
                "Maintenance" -> context!!.startActivity(
                    Intent(
                        context,
                        MaintenanceListActivity::class.java
                    )
                )
                "Events" -> context!!.startActivity(Intent(context, EventListActivity::class.java))
                "Notices" -> context!!.startActivity(
                    Intent(
                        context,
                        MemberNoticeListActivity::class.java
                    )
                )
            }
        }
        return dashView
    }

    override fun getItem(p0: Int): Any {
        return dashList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return dashList.size
    }
}