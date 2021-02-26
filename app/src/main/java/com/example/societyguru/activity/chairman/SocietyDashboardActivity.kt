package com.example.societyguru.activity.chairman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.R
import com.example.societyguru.adapter.member.MemberDashAdapter
import com.example.societyguru.databinding.ActivitySocietyDashboardBinding
import com.example.societyguru.model.member.DashModel

class SocietyDashboardActivity : AppCompatActivity() {
    private lateinit var screen:ActivitySocietyDashboardBinding
    var adapter: MemberDashAdapter? = null
    var dashList = ArrayList<DashModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(screen.root)

        dashList.add(
            DashModel(
                "Members",
                R.drawable.ic_maintenance,
                0
            )
        )

        dashList.add(
            DashModel(
                "Maintenance",
                R.drawable.ic_maintenance,
                0
            )
        )

        dashList.add(
            DashModel(
                "Events",
                R.drawable.ic_calendar,
                0
            )
        )
        dashList.add(
            DashModel(
                "Notices",
                R.drawable.ic_notices,
                0
            )
        )
        adapter = MemberDashAdapter(this, dashList)
    }
}