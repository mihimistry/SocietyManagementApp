package com.maxgen.societyguru.activity.member.dashboard.ui.payment.ui.main

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
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
import com.maxgen.societyguru.databinding.FragmentHistoryMaintenanceBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import com.maxgen.societyguru.utils.MyUtils
import java.text.SimpleDateFormat
import java.util.*

class HistoryMaintenanceFragment : Fragment(), FireAccess.OnPaidMaintenanceRvOptionsCreatedListener,
    FireAccess.OnPaidFilteredMaintenanceRvOptionsCreatedListener,
    OnMaintenanceOptionClick {
    private lateinit var screen: FragmentHistoryMaintenanceBinding
    private var adapter: MaintenanceListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentHistoryMaintenanceBinding.inflate(inflater, container, false)
        screen.rvMaintenanceHistory.layoutManager = LinearLayoutManager(context)
        activity?.let { FireAccess.getPaidMaintenanceReceivedRvOptions(it, this) }

        screen.edtSelectMonth.setOnClickListener {
            monthPicker()
        }

        screen.btnClear.setOnClickListener {
            screen.edtSelectMonth.setText("")
            screen.btnClear.visibility = View.GONE

        }

        screen.btnFilter.setOnClickListener {

            if (screen.edtSelectMonth.text.toString() == "") {

                activity?.let {
                    FireAccess.getPaidMaintenanceReceivedRvOptions(it, this)
                }
            } else {
                val maintenanceMonth: String =
                    screen.edtSelectMonth.text.toString()
                activity?.let {
                    FireAccess.getPaidFilteredMaintenanceReceivedRvOptions(
                        it,
                        maintenanceMonth,
                        this
                    )

                }

            }
        }
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

    override fun getPaidRvOptions(rvOptions: FirestoreRecyclerOptions<SocietyMaintenanceModel>) {
        activity?.let { adapter = MaintenanceListAdapter(MyUtils.getUserId(it), rvOptions, this) }
        screen.rvMaintenanceHistory.adapter = adapter
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

    private fun monthPicker() {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dialog = activity?.let {
            DatePickerDialog(
                it,
                AlertDialog.THEME_HOLO_LIGHT,
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    screen.edtSelectMonth.setText(
                        SimpleDateFormat("MMMM yyyy").format(cal.time).toString()
                    )
                    screen.btnClear.visibility = View.VISIBLE

                },
                year,
                month,
                day
            )
        }
        (dialog?.datePicker as ViewGroup).findViewById<ViewGroup>(
            Resources.getSystem().getIdentifier("day", "id", "android")
        ).visibility = View.GONE
        dialog.show()
    }
}