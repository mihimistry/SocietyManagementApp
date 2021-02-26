package com.maxgen.societyguru.activity.chairman

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.adapter.admin.OnNoticeSentToOptionClick
import com.maxgen.societyguru.databinding.ActivitySendNoticeBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import com.maxgen.societyguru.utils.MyUtils
import com.maxgen.societyguru.utils.SharedPreferenceUser
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class SendNoticeActivity : AppCompatActivity(), OnNoticeSentToOptionClick,
    FireAccess.NoticeCreatedListener {
    private lateinit var screen: ActivitySendNoticeBinding
    private var name: String = ""
    private var email: String = ""
    private var contact: String = ""
    private var maintenanceId: String = ""

    private val title get() = MyUtils.getEDTText(screen.edtTitle)
    private val desc get() = MyUtils.getEDTText(screen.edtDesc)
    private val newDueDate get() = MyUtils.getEDTText(screen.edtNewDueDate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivitySendNoticeBinding.inflate(layoutInflater)
        setContentView(screen.root)

        val b: Bundle? = intent.extras
        if (b != null) {
            name = b.getString("name", "")
            email = b.getString("email", "")
            contact = b.getString("contact", "")
            maintenanceId = b.getString("maintenanceId", "")
        }

        screen.tvName.text = name
        screen.tvEmail.text = email
        screen.tvMobile.text = contact
        screen.edtNewDueDate.setOnClickListener {
            dueDatePicker()
        }
        screen.btnSendNotice.setOnClickListener {
            if (MyUtils.isNetworkAvailable(this)) validateAndCreateNotice()
            else Toast.makeText(
                this,
                "No internet connections.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateAndCreateNotice() {
        var flag = true
        if (title.isEmpty()) {
            flag = true
            MyUtils.setEDTError(screen.edtTitle, "Please enter title.")
        }
        if (desc.isEmpty()) {
            flag = false
            MyUtils.setEDTError(screen.edtDesc, "Please enter description.")
        }

        if (flag) createNotice()
    }

    private fun createNotice() {
        val model = NoticeModel(
            title = title,
            description = desc,
            from = SharedPreferenceUser.getInstance().getUser(this).email
        )

        val noticeTo =
            NoticeModel.NoticeTo(
                email,
                MyUtils.getUserId(this),
                title,
                desc,
                "NO"
            )
        MyUtils.showProgress(this, "Sending Notice", null, false)
        FireAccess.createSingleNotice(model, noticeTo, this)

        val map = HashMap<String, Any>()
        map[SocietyMaintenanceModel.MaintenanceEnum.maintenanceDueDate.name] = newDueDate
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.MAINTENANCE.name)
            .document(maintenanceId).collection(FirebaseCollectionName.MAINTENANCETO.name)
            .whereEqualTo(
                SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,
                email
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                }
                if (value != null && !value.isEmpty) {
                    value.documents[0].reference.update(map)
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun dueDatePicker() {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                screen.edtNewDueDate.setText(
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

    override fun noticeCreated(flag: Boolean, error: String?) {
        MyUtils.dismissProgress()
        if (flag) {
            Toast.makeText(this, "Notice sent.", Toast.LENGTH_SHORT).show()
            finish()
        } else Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun dialPerson(phone: String) {
        MyUtils.dialPerson(this, phone)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}