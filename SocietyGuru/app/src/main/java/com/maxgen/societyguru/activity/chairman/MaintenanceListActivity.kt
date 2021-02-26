package com.maxgen.societyguru.activity.chairman

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maxgen.societyguru.R
import com.maxgen.societyguru.activity.member.MaintenanceActivity
import com.maxgen.societyguru.adapter.OnMaintenanceOptionClick
import com.maxgen.societyguru.adapter.member.MaintenanceListAdapter
import com.maxgen.societyguru.databinding.ActivityMaintenanceListBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import com.maxgen.societyguru.utils.MyUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MaintenanceListActivity : AppCompatActivity(),
//    FireAccess.OnMemberMaintenanceRvOptionsCreatedListener,
 //   FireAccess.OnFilteredMemberMaintenanceRvOptionsCreatedListener,
    OnMaintenanceOptionClick {
    private lateinit var screen: ActivityMaintenanceListBinding
    private var list: ArrayList<SocietyMaintenanceModel> = ArrayList()
    private var adapter: MaintenanceListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMaintenanceListBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        screen.rvMaintenance.layoutManager = LinearLayoutManager(this)

        screen.edtSelectMonth.setOnClickListener {
            monthPicker()
        }

        screen.btnClear.setOnClickListener {
            screen.edtSelectMonth.setText("")
            screen.btnClear.visibility = View.GONE
            getMaintenanceList()

        }

        screen.btnAddMaintenance.setOnClickListener {

            startActivity(Intent(this, CreateMaintenanceActivity::class.java))
        }

        screen.btnAddMaintenance.visibility =
            if (MyUtils.getUserType(this) == UserType.SOCIETY_MEMBER.name) View.GONE else View.VISIBLE


        getMaintenanceList()
    }

    private fun getMaintenanceList() {

        val query = FirebaseFirestore.getInstance()
            .collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,
                MyUtils.getUserId(this)
            )
            .orderBy(SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,Query.Direction.DESCENDING)

        val rvOptions = FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
            .setQuery(query, SocietyMaintenanceModel::class.java).build()

        adapter = MaintenanceListAdapter(MyUtils.getUserId(this), rvOptions, this)
        screen.rvMaintenance.adapter = adapter

        adapter?.stopListening()
        adapter?.startListening()
        adapter?.notifyDataSetChanged()

    }

    private fun getFilteredMaintenanceList(maintenanceMonth: String) {
        val query = FirebaseFirestore.getInstance()
            .collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,
                MyUtils.getUserId(this)
            )
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.maintenanceMonth.name,
                maintenanceMonth
            )
            .orderBy(SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,Query.Direction.DESCENDING)

        val rvOptions = FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
            .setQuery(query, SocietyMaintenanceModel::class.java).build()

        adapter = MaintenanceListAdapter(MyUtils.getUserId(this), rvOptions, this)
        screen.rvMaintenance.adapter = adapter
        adapter?.stopListening()
        adapter?.startListening()
        adapter?.notifyDataSetChanged()
    }

    private fun monthPicker() {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this,
            AlertDialog.THEME_HOLO_DARK,
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

        (dialog.datePicker as ViewGroup).findViewById<ViewGroup>(
            Resources.getSystem().getIdentifier("day", "id", "android")
        ).visibility = View.GONE

        dialog.show()
    }

    override fun showMaintenanceInfo(
        model: SocietyMaintenanceModel,
        counts: Int
    ) {
        val b = Bundle()
        b.putString("maintenanceId", model.maintenanceId)
        b.putInt("counts", counts)
        startActivity(
            Intent(
                this@MaintenanceListActivity,
                MaintenanceActivity::class.java
            ).putExtras(b)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
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