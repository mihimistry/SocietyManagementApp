package com.maxgen.societyguru.activity.chairman

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maxgen.societyguru.adapter.OnMaintenanceOptionClick
import com.maxgen.societyguru.adapter.chairman.MaintenanceSentListAdapter
import com.maxgen.societyguru.databinding.ActivityMaintenanceListBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import com.maxgen.societyguru.utils.MyUtils
import java.text.SimpleDateFormat
import java.util.*


class MaintenanceSentListActivity : AppCompatActivity(), OnMaintenanceOptionClick {

    private lateinit var screen: ActivityMaintenanceListBinding
    private var adapter: MaintenanceSentListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMaintenanceListBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        screen.rvMaintenance.layoutManager = LinearLayoutManager(this)

        adapter = MaintenanceSentListAdapter(
            FireAccess.getMaintenanceRvAdapterOptions(MyUtils.getUserId(this)), this
        )
        screen.rvMaintenance.adapter = adapter

        screen.btnAddMaintenance.visibility = View.GONE

        screen.edtSelectMonth.setOnClickListener {
            monthPicker()
        }

        screen.btnClear.setOnClickListener {
            screen.edtSelectMonth.setText("")
            screen.btnClear.visibility = View.GONE
            getMaintenanceList()
        }

        getMaintenanceList()

        /*
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val builder1 =
                        AlertDialog.Builder(this@MaintenanceSentListActivity)
                    builder1.setMessage("Are You Sure?")
                    builder1.setCancelable(true)

                    builder1.setPositiveButton(
                        "Yes, Delete"
                    ) { dialog, id ->
                        adapter!!.deleteItem(viewHolder.adapterPosition)
                        dialog.cancel()
                        adapter?.notifyDataSetChanged()
                    }

                    builder1.setNegativeButton(
                        "No, Cancel"
                    ) { dialog, id ->
                        dialog.cancel()

                        adapter?.notifyDataSetChanged()
                    }

                    val alert11 = builder1.create()
                    alert11.show()
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(
                            ContextCompat.getColor(
                                this@MaintenanceSentListActivity,
                                R.color.darker_gray
                            )
                        )
                        .addActionIcon(R.drawable.ic_menu_delete)
                        .create()
                        .decorate()
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }).attachToRecyclerView(screen.rvMaintenance)


     */
    }


    private fun getMaintenanceList() {

        val query = FirebaseFirestore.getInstance()
            .collection(FirebaseCollectionName.MAINTENANCE.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.from.name,
                MyUtils.getUserId(this)
            )
            .orderBy(
                SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,
                Query.Direction.DESCENDING
            )

        val rvOptions = FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
            .setQuery(query, SocietyMaintenanceModel::class.java).build()

        adapter = MaintenanceSentListAdapter(rvOptions, this)
        screen.rvMaintenance.adapter = adapter
        adapter?.stopListening()
        adapter?.startListening()
        adapter?.notifyDataSetChanged()

    }

    private fun getFilteredMaintenanceList(maintenanceMonth: String) {
        val query = FirebaseFirestore.getInstance()
            .collection(FirebaseCollectionName.MAINTENANCE.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.from.name,
                MyUtils.getUserId(this)
            )
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceEnum.maintenanceMonth.name,
                maintenanceMonth
            )
            .orderBy(
                SocietyMaintenanceModel.MaintenanceEnum.createdAt.name,
                Query.Direction.DESCENDING
            )


        val rvOptions = FirestoreRecyclerOptions.Builder<SocietyMaintenanceModel>()
            .setQuery(query, SocietyMaintenanceModel::class.java).build()

        adapter = MaintenanceSentListAdapter(rvOptions, this)
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

        (dialog.datePicker as ViewGroup).findViewById<ViewGroup>(
            Resources.getSystem().getIdentifier("day", "id", "android")
        ).visibility = View.GONE

        dialog.show()
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
        startActivity(
            Intent(
                this,
                MemberMaintenancePaymentListActivity::class.java
            ).putExtras(b)
        )

    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}