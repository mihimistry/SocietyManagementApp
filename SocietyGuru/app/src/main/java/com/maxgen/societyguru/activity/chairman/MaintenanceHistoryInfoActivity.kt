package com.maxgen.societyguru.activity.chairman

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.maxgen.societyguru.databinding.ActivityMaintenanceHistoryInfoBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import java.text.SimpleDateFormat
import java.util.*

class MaintenanceHistoryInfoActivity : AppCompatActivity(),
    FireAccess.MaintenanceInfoListener, FireAccess.OnMaintenancePaymentStatusListener,
    FireAccess.PaidMaintenanceInfoListener {
    private lateinit var screen: ActivityMaintenanceHistoryInfoBinding
    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var maintenanceId: String
    private lateinit var userEmail: String
    private var counts: Int = 1

    private var paid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMaintenanceHistoryInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getMaintenanceInfo()
    }

    private fun getMaintenanceInfo() {
        val b = intent.extras
        if (b == null) {
            Toast.makeText(this, "Invalid launch", Toast.LENGTH_SHORT).show()
            return
        }
        counts = b.getInt("counts", 1)
        maintenanceId = b.getString("maintenanceId", "")
        userEmail = b.getString("userEmail", "")

        if (maintenanceId.isEmpty()) {
            Toast.makeText(this, "Could not find data.", Toast.LENGTH_SHORT).show()
            return
        }

        FireAccess.getMaintenanceInfo(maintenanceId, userEmail, this)

        FireAccess.getMaintenancePaymentStatus(maintenanceId, userEmail, this)
        FireAccess.getPaidMaintenanceInfo(maintenanceId, userEmail, this)
    }

    override fun paymentStatus(flag: Boolean) {
        paid = flag
        if (flag) {
            screen.dueDateLl.visibility = View.GONE
            screen.paidDateLl.visibility = View.VISIBLE
            screen.txtAmount.text = "Amount Paid"
            screen.tvPaymentStatus.text = "Maintenance Paid"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun listen(flag: Boolean, model: SocietyMaintenanceModel?) {
        if (flag && model != null) {
            screen.maintenance = model

            val currDate: String =
                SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time).toString()

            val sdf = SimpleDateFormat("dd/MM/yyyy")

            if (model.maintenanceDescription.isNullOrEmpty())
                screen.tvDescription.visibility=View.GONE
            if (model.lateCharges != "") {

                if (sdf.parse(model.maintenanceDueDate) < sdf.parse(currDate)) {


                    if (counts <= 0)
                        screen.tvAmount.text = "₹" + model.maintenanceAmount
                    else
                        screen.tvAmount.text = "₹" +(model.maintenanceAmount.toInt()+ (model.lateCharges.toInt() * counts)).toString()

                } else
                    screen.tvAmount.text = "₹" + model.maintenanceAmount
            } else
                screen.tvAmount.text = "₹" + model.maintenanceAmount
        } else {
            Toast.makeText(this, "Could not find data.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun listen(flag: Boolean, paidModel: SocietyMaintenanceModel.MaintenanceTo?) {
        if (flag && paidModel != null) {
            screen.maintenanceToModel = paidModel
            if (paidModel.maintenanceDescription == "")
                screen.tvDescription.visibility = View.GONE
        } else {
            Toast.makeText(this, "Could not find data.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

}