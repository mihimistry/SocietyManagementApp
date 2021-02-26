package com.example.societyguru.activity.chairman

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.societyguru.R
import com.example.societyguru.databinding.ActivityCreateEventBinding
import com.example.societyguru.enums.ChargesPer
import com.example.societyguru.enums.FirebaseCollectionName
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyEventModel
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.MyUtils.getEDTText
import com.example.societyguru.utils.SharedPreferenceUser
import java.text.SimpleDateFormat
import java.util.*

class CreateEventActivity : AppCompatActivity(), FireAccess.OnEventCreatingListener {

    private lateinit var screen: ActivityCreateEventBinding

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var startTime: Calendar? = null
    private var endTime: Calendar? = null


    private val title get() = getEDTText(screen.edtTitle)
    private val description get() = getEDTText(screen.edtDescription)
    private val sDate get() = getEDTText(screen.edtStartDate)
    private val eDate get() = getEDTText(screen.edtEndDate)
    private val sTime get() = getEDTText(screen.edtStartTime)
    private val eTime get() = getEDTText(screen.edtEndTime)
    private val amount get() = getEDTText(screen.edtAmount)
    private val chargesPer: String
        get() {
            return when (screen.rgCharges.checkedRadioButtonId) {
                R.id.rb_per_home -> ChargesPer.HOME.name
                R.id.rb_per_person -> ChargesPer.PERSON.name
                else -> ""
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        screen.edtStartDate.setOnClickListener {
            getDateFromUser(screen.edtStartDate, DATE.START)
        }
        screen.edtEndDate.setOnClickListener {
            if (getEDTText(screen.edtStartDate).isEmpty()) Toast.makeText(
                this,
                "Please select start date.",
                Toast.LENGTH_SHORT
            ).show()
            else getDateFromUser(screen.edtEndDate, DATE.END)
        }
        screen.edtStartTime.setOnClickListener {
            if (endDate == null) {
                Toast.makeText(this, "Please select end date.", Toast.LENGTH_SHORT).show()
            } else {
                val c: Calendar = Calendar.getInstance()
                val mHour = c.get(Calendar.HOUR_OF_DAY)
                val mMinute = c.get(Calendar.MINUTE)
                val timePickerDialog = TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        startTime = cal
                        endTime = null
                        screen.edtEndTime.setText("")
                        screen.edtStartTime.setText(SimpleDateFormat("hh:mm a").format(cal.time))
                        screen.edtStartTime.error = null
                    },
                    mHour,
                    mMinute,
                    false
                )
                timePickerDialog.show()
            }
        }
        screen.edtEndTime.setOnClickListener {
            if (getEDTText(screen.edtStartTime).isEmpty())
                Toast.makeText(this, "Please select start time.", Toast.LENGTH_SHORT).show()
            else {
                val c: Calendar = Calendar.getInstance()
                val mHour = c.get(Calendar.HOUR_OF_DAY)
                val mMinute = c.get(Calendar.MINUTE)
                val timePickerDialog = TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        endTime = cal
                        if (getEDTText(screen.edtStartDate) == getEDTText(screen.edtEndDate)) {
                            startTime?.let {
                                if (it > endTime) {
                                    Toast.makeText(
                                        this@CreateEventActivity,
                                        "End time must greater than start time.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@OnTimeSetListener
                                }
                            }
                        }
                        screen.edtEndTime.setText(SimpleDateFormat("hh:mm a").format(cal.time))
                        screen.edtEndTime.error = null
                    },
                    mHour,
                    mMinute,
                    false
                )
                timePickerDialog.show()
            }
        }

        screen.btnCreateEvent.setOnClickListener {
            if (MyUtils.isNetworkAvailable(this))
                validateAndCreateEvent()
            else Toast.makeText(this, "Please check internet connection.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun validateAndCreateEvent() {
        var b = true
        if (title.isEmpty()) {
            screen.edtTitle.error = "Enter Event Title"
            b = false
        }
        if (description.isEmpty()) {
            screen.edtDescription.error = "Enter Event Discription"
            b = false
        }
        if (sDate.isEmpty()) {
            screen.edtStartDate.requestFocus()
            screen.edtStartDate.error = "Enter Start Date"
            b = false
        }
        if (eDate.isEmpty()) {
            screen.edtEndDate.error = "Enter End Date"
            b = false
        }
        if (sTime.isEmpty()) {
            screen.edtStartTime.error = "Enter Start Time"
            b = false
        }
        if (eTime.isEmpty()) {
            screen.edtEndTime.error = "Enter End Time"
            b = false
        }
        if (amount.isEmpty()) {
            screen.edtAmount.error = "Enter valid amount."
            b = false
        }
        if (chargesPer.isEmpty()) {
            Toast.makeText(this, "Select Charges Type", Toast.LENGTH_SHORT).show()
            b = false
        }
        if (b) createEvent()
    }

    private fun createEvent() {
        val model = SocietyEventModel(
            societyId = SharedPreferenceUser.getInstance().getUser(this).societyId,
            eventTitle = title,
            eventDescription = description,
            eventStartDate = sDate,
            eventStartTime = sTime,
            eventEndDate = eDate,
            eventEndTime = eTime,
            amount = amount,
            chargesPer = chargesPer
        )

        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.societyId.name,MyUtils.getUserSocietyId(this))
            .addSnapshotListener { value, error ->
                if (error!=null) Log.d("CREATE_EVENT","ERROR:"+error.message)
                if (value!=null&&!value.isEmpty) {
                    val users=value.toObjects(UserModel::class.java)
                    val eventTo: ArrayList<SocietyEventModel.EventTo> = ArrayList()

                    for (user in users){
                        val event=SocietyEventModel.EventTo(
                            societyId = SharedPreferenceUser.getInstance().getUser(this).societyId,
                            eventTitle = title,
                            eventDescription = description,
                            eventStartDate = sDate,
                            eventStartTime = sTime,
                            eventEndDate = eDate,
                            eventEndTime = eTime,
                            amount = amount,
                            chargesPer = chargesPer,
                            to = user.email,
                            seen = "NO",
                            userRegistered = "NO",
                            paidTime = "",
                            paidDate = "",
                            transactionID = "",
                            totalCharge = "",
                            attendingPersons = ""
                        )
                        eventTo.add(event)
                    }
                    FireAccess.createSocietyEvent(model, eventTo,this)

                }
            }
    }

    private fun getDateFromUser(editText: EditText, saveDate: DATE) {
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
                if (saveDate == DATE.START) {
                    startDate = cal
                    endDate = null
                    screen.edtEndDate.setText("")
                    screen.edtStartDate.error = null
                } else {
                    endDate = cal
                    startTime = null
                    endTime = null
                    screen.edtStartTime.setText("")
                    screen.edtEndTime.setText("")
                    if (startDate != null && endDate != null) {
                        if (startDate!! > endDate) {
                            Toast.makeText(
                                this@CreateEventActivity,
                                "End date must same or bigger than start date.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@OnDateSetListener
                        }
                        screen.edtEndDate.error = null
                    }
                }
                editText.setText(SimpleDateFormat("dd/MM/yyyy").format(cal.time).toString())
            },
            year,
            month,
            day
        )
        if (saveDate == DATE.START) {
            dialog.datePicker.minDate = Calendar.getInstance().timeInMillis
        } else {
            startDate?.let {
                dialog.datePicker.minDate = it.timeInMillis - 1000
            }
        }
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    enum class DATE {
        START,
        END
    }

    override fun eventCreated(flag: Boolean, error: String?) {
        if (flag) {
            Toast.makeText(this, "Event created.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

}