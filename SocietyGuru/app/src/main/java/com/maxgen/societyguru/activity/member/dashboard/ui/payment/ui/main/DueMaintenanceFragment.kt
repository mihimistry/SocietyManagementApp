package com.maxgen.societyguru.activity.member.dashboard.ui.payment.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.activity.member.MaintenanceActivity
import com.maxgen.societyguru.adapter.OnMaintenanceOptionClick
import com.maxgen.societyguru.adapter.member.MaintenanceListAdapter
import com.maxgen.societyguru.databinding.FragmentDueMaintenanceBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import com.maxgen.societyguru.utils.MyUtils

class DueMaintenanceFragment : Fragment(), FireAccess.OnDueMaintenanceRvOptionsCreatedListener,
    OnMaintenanceOptionClick {
    private lateinit var screen: FragmentDueMaintenanceBinding
    private var adapter: MaintenanceListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentDueMaintenanceBinding.inflate(inflater, container, false)
        screen.rvMaintenanceDue.layoutManager = LinearLayoutManager(context)
        activity?.let { FireAccess.getDueMaintenanceReceivedRvOptions(it, this) }
        return screen.root
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun getDueRvOptions(rvOptions: FirestoreRecyclerOptions<SocietyMaintenanceModel>) {
        activity?.let { adapter = MaintenanceListAdapter(MyUtils.getUserId(it),rvOptions, this) }
        screen.rvMaintenanceDue.adapter = adapter
        adapter?.startListening()
    }

    override fun showMaintenanceInfo(
        model: SocietyMaintenanceModel,
        counts: Int
    ) {
        val b = Bundle()
        b.putString("maintenanceId", model.maintenanceId)
        startActivity(
            Intent(
                activity,
                MaintenanceActivity::class.java
            ).putExtras(b)
        )
    }

}