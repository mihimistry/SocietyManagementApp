package com.example.societyguru.activity.chairman.fragment

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
import com.example.societyguru.activity.chairman.MaintenanceHistoryInfoActivity
import com.example.societyguru.adapter.OnMaintenanceOptionClick
import com.example.societyguru.adapter.member.MaintenanceListAdapter
import com.example.societyguru.databinding.FragmentHistoryMaintenanceBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyMaintenanceModel
import java.text.SimpleDateFormat
import java.util.*

class MaintenanceHistoryFragment(private val userEmail: String, private val societyId: String) :
    Fragment(),

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

        screen.edtSelectMonth.setOnClickListener {
            monthPicker()
        }

        screen.btnClear.setOnClickListener {
            screen.edtSelectMonth.setText("")
            screen.btnClear.visibility = View.GONE
            getMaintenanceList()

        }
        getMaintenanceList()


        return screen.root
    }

    private fun getMaintenanceList() {

        adapter = MaintenanceListAdapter(userEmail, FireAccess.getMaintenanceReceivedRvAdapterOptions(userEmail), this)
        screen.rvMaintenanceHistory.adapter = adapter
        adapter?.stopListening()
        adapter?.startListening()
        adapter?.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun showMaintenanceInfo(
        model: SocietyMaintenanceModel,
        counts: Int
    ) {
        val b = Bundle()
        b.putString("maintenanceId", model.maintenanceId)
        b.putString("userEmail", userEmail)
        b.putInt("counts",counts)
        startActivity(
            Intent(
                activity,
                MaintenanceHistoryInfoActivity::class.java
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
                    getFilteredMaintenanceList(screen.edtSelectMonth.text.toString())

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

    private fun getFilteredMaintenanceList(maintenanceMonth: String) {

        adapter = MaintenanceListAdapter(userEmail, FireAccess.getFilteredMaintenanceReceivedRvAdapterOptions(userEmail,maintenanceMonth), this)
        screen.rvMaintenanceHistory.adapter = adapter
        adapter?.stopListening()
        adapter?.startListening()
        adapter?.notifyDataSetChanged()
    }
}