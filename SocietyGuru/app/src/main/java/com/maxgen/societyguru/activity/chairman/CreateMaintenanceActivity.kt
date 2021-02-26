package com.maxgen.societyguru.activity.chairman

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxgen.societyguru.adapter.admin.NoticeSendToListAdapter
import com.maxgen.societyguru.adapter.admin.OnNoticeSentToOptionClick
import com.maxgen.societyguru.databinding.ActivityCreateMaintenanceBinding
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import com.maxgen.societyguru.utils.MyUtils
import com.maxgen.societyguru.utils.SharedPreferenceUser
import java.text.SimpleDateFormat
import java.util.*


class CreateMaintenanceActivity : AppCompatActivity(), FireAccess.OnMaintenanceCreatingListener,
    OnNoticeSentToOptionClick {
    private lateinit var screen: ActivityCreateMaintenanceBinding
    private lateinit var adapter: NoticeSendToListAdapter

    private val month get() = MyUtils.getEDTText(screen.edtMonth)
    private val dueDate get() = MyUtils.getEDTText(screen.edtDueDate)
    private val amount get() = MyUtils.getEDTText(screen.edtAmount)
    private val lateCharges get() = MyUtils.getEDTText(screen.edtLateCharges)
    private val description get() = MyUtils.getEDTText(screen.edtDesc)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityCreateMaintenanceBinding.inflate(layoutInflater)
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

        screen.rvSendToSelect.adapter = adapter
        screen.checkAll.setOnCheckedChangeListener { _, b ->
            screen.checkChairman.isChecked = true
            if (!screen.checkAll.isChecked)
                screen.checkChairman.isChecked = false

            adapter.models.forEach { model ->
                adapter.selectedModels.clear()
                model.checked = b
                adapter.notifyDataSetChanged()
            }
        }

        screen.edtMonth.setOnClickListener {
            monthPicker()
        }

        screen.edtDueDate.setOnClickListener {
            dueDatePicker()
        }

        screen.btnSendMaintenance.setOnClickListener {
            validateAndCreateMaintenance()
        }
    }


    private fun validateAndCreateMaintenance() {
        var flag = true
        if (month.isEmpty()) {
            flag = true
            MyUtils.setEDTError(screen.edtMonth, "Please enter Maintenance Month.")
        }
        if (dueDate.isEmpty()) {
            flag = true
            MyUtils.setEDTError(screen.edtMonth, "Please enter Due Date.")
        }
        if (amount.isEmpty()) {
            flag = true
            MyUtils.setEDTError(screen.edtMonth, "Please enter Maintenance Month.")
        }
        if (lateCharges.isEmpty()) {
            flag = true
            MyUtils.setEDTError(screen.edtMonth, "Please enter Late Charges.")
        }
        if (!screen.checkChairman.isChecked && adapter.selectedModels.size < 1) {
            flag = false
            Toast.makeText(this, "Please select Member to send Maintenance.", Toast.LENGTH_SHORT)
                .show()
        }
        if (!MyUtils.isNetworkAvailable(applicationContext)) {
            flag = false
            Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
        }
        if (flag)
            createMaintenance()
    }

    private fun createMaintenance() {
        val model = SocietyMaintenanceModel(
            societyId = SharedPreferenceUser.getInstance().getUser(this).societyId,
            from = MyUtils.getUserId(this),
            maintenanceMonth = month,
            maintenanceDescription = description,
            maintenanceDueDate = dueDate,
            maintenanceAmount = amount,
            lateCharges = lateCharges
        )

        val maintenanceTo: ArrayList<SocietyMaintenanceModel.MaintenanceTo> = ArrayList()

        for (user in adapter.selectedModels) maintenanceTo.add(
            SocietyMaintenanceModel.MaintenanceTo(
                user.email,
                maintenancePaid = "NO",
                maintenanceMonth = month,
                maintenanceDescription = description,
                maintenanceDueDate = dueDate,
                maintenanceAmount = amount,
                lateCharges = lateCharges,
                from = MyUtils.getUserId(this),
                memberName = user.fName + " " + user.lName,
                memberContact = user.mobile,
                societyId = SharedPreferenceUser.getInstance().getUser(this).societyId,
                seen = "NO"
                )
        )

        if (screen.checkChairman.isChecked) maintenanceTo.add(
            SocietyMaintenanceModel.MaintenanceTo(
                MyUtils.getUserId(this),
                maintenancePaid = "NO",
                maintenanceMonth = month,
                maintenanceDescription = description,
                maintenanceDueDate = dueDate,
                maintenanceAmount = amount,
                lateCharges = lateCharges,
                from = MyUtils.getUserId(this),
                memberName = MyUtils.getUserFirstName(this) + " " + SharedPreferenceUser.getInstance()
                    .getUser(this).lName,
                memberContact = MyUtils.getUserMobile(this),
                societyId = SharedPreferenceUser.getInstance().getUser(this).societyId
                )
        )

        MyUtils.showProgress(this, "Sending Maintenance", null, false)
        FireAccess.createSocietyMaintenance(model, maintenanceTo, this)
    }

    private fun dueDatePicker() {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this,
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                screen.edtDueDate.setText(
                    SimpleDateFormat("dd/MM/yyyy").format(cal.time).toString()
                )
            },
            year,
            month,
            day
        )
        dialog.datePicker.minDate = c.timeInMillis

        dialog.show()

    }

    private fun monthPicker() {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this,
            AlertDialog.THEME_HOLO_LIGHT,
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                screen.edtMonth.setText(SimpleDateFormat("MMMM yyyy").format(cal.time).toString())
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

    override fun maintenanceCreated(flag: Boolean, error: String?) {
        if (flag) {
            Toast.makeText(this, "Maintenance created.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun dialPerson(phone: String) {
        MyUtils.dialPerson(this, phone)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}