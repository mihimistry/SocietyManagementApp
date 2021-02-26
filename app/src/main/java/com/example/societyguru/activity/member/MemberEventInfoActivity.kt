package com.example.societyguru.activity.member

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.societyguru.activity.PaymentActivity
import com.example.societyguru.activity.member.dashboard.RegisteredEventInfoActivity
import com.example.societyguru.databinding.ActivityMemberEventInfoBinding
import com.example.societyguru.enums.ChargesPer
import com.example.societyguru.enums.FirebaseCollectionName
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.NoticeModel
import com.example.societyguru.model.SocietyEventModel
import com.example.societyguru.model.member.PaidForEventModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.MyUtils.dismissProgress
import com.example.societyguru.utils.MyUtils.isNetworkAvailable
import com.example.societyguru.utils.MyUtils.showProgress
import com.example.societyguru.utils.SharedPreferenceUser
import java.text.SimpleDateFormat
import java.util.*

class MemberEventInfoActivity : AppCompatActivity(), FireAccess.OnSocietyEventInfoListener,
    FireAccess.OnEventRegisteringListener, FireAccess.OnEventRegistrationStatusListener,
    FireAccess.OnEventRegistrationInfoReceived {

    private lateinit var screen: ActivityMemberEventInfoBinding

    private var eventId: String = ""
    private var eventModel: SocietyEventModel? = null

    private var registered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMemberEventInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getEventData()
    }

    private fun getEventData() {
        val b = intent.extras
        if (b == null) {
            Toast.makeText(this, "Unknown launch", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        eventId = b.getString("id", "")
        if (eventId.isEmpty()) {
            Toast.makeText(this, "Event data not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        FireAccess.getSocietyEventInformation(eventId, this)
        FireAccess.getEventRegisteredInfo(eventId, MyUtils.getUserId(this), this)
    }

    override fun eventReceived(flag: Boolean, model: SocietyEventModel?, error: String?) {
        if (flag) {
            model?.let { eventModel ->
                this.eventModel = eventModel
                screen.event = eventModel

                val map = HashMap<String, Any>()
                map[NoticeModel.NoticeEnum.seen.name]="YES"
                FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.EVENTTO.name)
                    .whereEqualTo(SocietyEventModel.EventEnum.to.name,MyUtils.getUserId(this))
                    .whereEqualTo(SocietyEventModel.EventEnum.eventId.name, model.id)
                    .whereEqualTo(SocietyEventModel.EventEnum.seen.name,"NO")
                    .addSnapshotListener { value, error ->
                        if (error!=null) Log.d("MEMBER_EVENT","ERROR:"+error.message)
                        if (value!=null&&!value.isEmpty){
                            value.documents[0].reference.update(map)
                        }
                    }

                setFreeStatus(eventModel)
                FireAccess.getEventRegistrationStatus(eventModel.id, MyUtils.getUserId(this), this)

                val currDate: String =
                    SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time).toString()

                val sdf = SimpleDateFormat("dd/MM/yyyy")

                val currTime: String =
                    SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time).toString()

                val sdfTime = SimpleDateFormat("hh:mm a")

                if (sdf.parse(model.eventEndDate) == sdf.parse(currDate)) {
                    if (sdfTime.parse(model.eventEndTime) < sdfTime.parse(currTime)) {
                        screen.btnAttendEvent.visibility = View.GONE
                        screen.tvAlreadyRegistered.visibility = View.VISIBLE
                        screen.tvAlreadyRegistered.text = "Registration Closed"
                    }
                }

                if (sdf.parse(model.eventEndDate) < sdf.parse(currDate)) {
                    screen.btnAttendEvent.visibility = View.GONE
                    screen.tvAlreadyRegistered.visibility = View.VISIBLE
                    screen.tvAlreadyRegistered.text = "Registration Closed"
                }


                screen.llAttendingPersons.visibility =
                    if (eventModel.amount == "0" || eventModel.chargesPer == ChargesPer.HOME.name) View.GONE
                    else {
                        screen.imgbtnAdd.setOnClickListener {
                            var count: Int =
                                java.lang.String.valueOf(screen.tvPersonCount.text).toInt()
                            count++
                            screen.tvPersonCount.text = count.toString()
                            screen.tvTotalCharges.text =
                                (count * eventModel.amount.toInt()).toString()
                        }
                        screen.imgbtnRemove.setOnClickListener {
                            var count: Int =
                                java.lang.String.valueOf(screen.tvPersonCount.text).toInt()
                            if (count > 1) {
                                count--
                                screen.tvPersonCount.text = count.toString()
                                screen.tvTotalCharges.text =
                                    (count * eventModel.amount.toInt()).toString()
                            }
                        }
                        View.VISIBLE
                    }
                screen.btnAttendEvent.setOnClickListener {
                    if (!isNetworkAvailable(this)) {
                        Toast.makeText(
                            this,
                            "Please check internet connection.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    if (registered) {
                        val b = Bundle()
                        b.putString("id", eventId)
                        startActivity(
                            Intent(
                                this@MemberEventInfoActivity,
                                RegisteredEventInfoActivity::class.java
                            ).putExtras(b)
                        )
                        return@setOnClickListener
                    }
                    if (eventModel.amount == "0") {

                        val paidDate: String =
                            SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
                                .toString()

                        val paidTime: String =
                            SimpleDateFormat("hh:mm aa").format(Calendar.getInstance().time)
                                .toString()

                        val paidEventModel = PaidForEventModel(
                            eventId = eventId,
                            totalCharge = "0",
                            userId = SharedPreferenceUser.getInstance().getUser(this).email,
                            paidDate = paidDate,
                            paidTime = paidTime
                        )
                        showProgress(
                            this@MemberEventInfoActivity,
                            "Registering in Event",
                            null,
                            false
                        )
                        FireAccess.registerUserForSocietyEvent(
                            SharedPreferenceUser.getInstance().getUser(this),
                            eventModel,
                            paidEventModel,
                            this@MemberEventInfoActivity
                        )
                    } else if (eventModel.chargesPer == ChargesPer.PERSON.name && screen.tvPersonCount.text.toString()
                            .trim() != "0"
                    ) {

                        AlertDialog.Builder(this)
                            .setMessage("You want to register for ${screen.tvPersonCount.text} persons for this event.\nTotal charges will be ${screen.tvTotalCharges.text}")
                            .setTitle("Are you sure?")
                            .setPositiveButton("Yes") { _, _ ->
                                val b = Bundle()
                                b.putString("amount", screen.tvTotalCharges.text.toString())
                                startActivityForResult(
                                    Intent(
                                        this@MemberEventInfoActivity,
                                        PaymentActivity::class.java
                                    ).putExtras(b), 10
                                )
                            }
                            .setNegativeButton("No", null)
                            .show()
                    } else if (eventModel.chargesPer == ChargesPer.PERSON.name && screen.tvPersonCount.text.toString()
                            .trim() == "0"
                    ) {
                        Toast.makeText(
                            this,
                            "Please Enter Number of persons for event.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (eventModel.chargesPer == ChargesPer.HOME.name) {
                        val b = Bundle()
                        b.putString("amount", eventModel.amount)
                        startActivityForResult(
                            Intent(
                                this@MemberEventInfoActivity,
                                PaymentActivity::class.java
                            ).putExtras(b), 10
                        )
                    }
                }
            }
        } else Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    private fun setFreeStatus(eventModel: SocietyEventModel) {
        if (eventModel.amount == "0") {
            screen.tvAmount.text = "FREE"
            screen.tvChargesPer.visibility = View.INVISIBLE
            screen.llTotal.visibility = View.INVISIBLE
        } else
            screen.tvAmount.text = eventModel.amount
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun registeredInEvent(flag: Boolean) {
        dismissProgress()
        if (flag) {
            Toast.makeText(this, "Registered Successfully.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun registrationStatus(flag: Boolean) {
        registered = flag
        if (flag) {
            screen.llAttendingPersons.visibility = View.GONE
            screen.btnAttendEvent.visibility = View.GONE
            screen.tvAlreadyRegistered.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {

            val paidDate: String =
                SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time).toString()

            val paidTime: String =
                SimpleDateFormat("hh:mm aa").format(Calendar.getInstance().time).toString()

            this.eventModel?.let {
                val paidForEventModel = PaidForEventModel(
                    eventId = eventId,
                    userId = MyUtils.getUserId(this@MemberEventInfoActivity),
                    totalCharge = screen.tvTotalCharges.text.toString().trim(),
                    chargesPer = it.chargesPer,
                    attendingPersons = screen.tvPersonCount.text.toString(),
                    transactionID = "Test",
                    paidDate = paidDate,
                    paidTime = paidTime
                )

                FireAccess.registerUserForSocietyEvent(
                    MyUtils.getUserModel(this@MemberEventInfoActivity),
                    it,
                    paidForEventModel,
                    this@MemberEventInfoActivity
                )
            }
        } else if (requestCode == 10)
            Toast.makeText(this, "Transaction failed please try again.", Toast.LENGTH_SHORT).show()
    }

    override fun eventRegistrationInfoReceived(model: PaidForEventModel) {
        if (model.paidDate != "")
            screen.tvAlreadyRegistered.text = "Registered for Event on " + model.paidDate
        else
            screen.tvAlreadyRegistered.text = "Registered for Event"
    }

}