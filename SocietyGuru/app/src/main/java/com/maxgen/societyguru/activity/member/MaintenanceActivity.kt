package com.maxgen.societyguru.activity.member


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.activity.PaymentActivity
import com.maxgen.societyguru.databinding.ActivityMaintenanceBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import com.maxgen.societyguru.utils.MyUtils
import com.maxgen.societyguru.utils.SharedPreferenceUser
import java.text.SimpleDateFormat
import java.util.*

class MaintenanceActivity : AppCompatActivity(), FireAccess.OnMaintenancePaidListener,
    FireAccess.MaintenanceInfoListener, FireAccess.OnMaintenancePaymentStatusListener,
FireAccess.PaidMaintenanceInfoListener{

    private lateinit var screen: ActivityMaintenanceBinding
    private val calendar: Calendar = Calendar.getInstance()
    private var maintenanceId: String = ""
    private var paid = false
    private var counts: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMaintenanceBinding.inflate(layoutInflater)
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

        maintenanceId = b.getString("maintenanceId").toString()
        counts = b.getInt("counts", 1)

        if (maintenanceId.isEmpty()) {
            Toast.makeText(this, "Could not find data.", Toast.LENGTH_SHORT).show()
            return
        }

        val currDate: String =
            SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time).toString()

        val sdf = SimpleDateFormat("dd/MM/yyyy")

        FireAccess.getMaintenanceInfo(maintenanceId,MyUtils.getUserId(this), this)
        FireAccess.getMaintenancePaymentStatus(maintenanceId, MyUtils.getUserId(this), this)
        FireAccess.getPaidMaintenanceInfo(maintenanceId, MyUtils.getUserId(this), this)

        screen.btnPayNow.setOnClickListener {
            val b = Bundle()
            b.putString("amount", screen.tvAmount.text.toString().replace("₹", ""))
            if (sdf.parse(screen.tvDueDate.text.toString()) < sdf.parse(currDate)) {
            b.putString("charges", screen.tvLateCharges.text.toString().replace("₹", ""))
            }
            startActivityForResult(
                Intent(
                    this@MaintenanceActivity,
                    PaymentActivity::class.java
                ).putExtras(b), 1
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            val paidDate: String =
                SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time).toString()
            val paidAmount: String = screen.tvAmount.text.toString().replace("₹", "")

            val paidMaintenanceModel = SocietyMaintenanceModel.MaintenanceTo(
                paidDate = paidDate,
                maintenancePaid = "YES",
                maintenanceAmount = paidAmount,
                to = MyUtils.getUserId(this)
            )

            val maintenanceModel = SocietyMaintenanceModel(
                maintenanceId = maintenanceId
            )

            FireAccess.registerUserPaidForMaintenance(
                SharedPreferenceUser.getInstance().getUser(this),
                maintenanceModel,
                paidMaintenanceModel,
                this@MaintenanceActivity
            )
        }
    }

    override fun paidForMaintenance(flag: Boolean) {
        if (flag) {
            Toast.makeText(this, "Paid Successfully.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun paymentStatus(flag: Boolean) {
        paid = flag
        if (flag) {
            screen.dueDateLl.visibility = View.GONE
            screen.paidDateLl.visibility = View.VISIBLE
            screen.btnPayNow.visibility = View.GONE
            screen.lateChargesLl.visibility=View.GONE
            screen.tvWarning.visibility=View.GONE
            screen.txtAmount.text = "Amount Paid"
            screen.tvPaymentStatus.text = "Maintenance Paid"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun listen(flag: Boolean, paidModel: SocietyMaintenanceModel.MaintenanceTo?) {
        if (flag && paidModel != null) {
        screen.maintenanceToModel=paidModel}
            else {
                Toast.makeText(this, "Could not find data", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    override fun listen(flag: Boolean, model: SocietyMaintenanceModel?) {
        if (flag && model != null) {
            screen.maintenance = model

            screen.tvWarning.text = "* Late Charges will be applied After Due Date"

            val map = HashMap<String, Any>()
            map[SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.seen.name]="YES"
            FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.MAINTENANCETO.name)
                .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.to.name,MyUtils.getUserId(this))
                .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.maintenanceId.name,model.maintenanceId)
                .whereEqualTo(SocietyMaintenanceModel.MaintenanceTo.MaintenancePaidEnum.seen.name,"NO")
                .addSnapshotListener { value, error ->
                    if (error!=null) Log.d("MEMBER_MAINTENANCE","ERROR:"+error.message)
                    if (value!=null&&!value.isEmpty){
                        value.documents[0].reference.update(map)
                    }
                }

            val currDate: String =
                SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time).toString()

            val sdf = SimpleDateFormat("dd/MM/yyyy")

            if (model.maintenanceDescription.isNullOrEmpty())
                screen.tvDescription.visibility=View.GONE
            if (model.lateCharges != "") {
                screen.lateChargesLl.visibility = View.VISIBLE

                if (counts <= 0)
                    screen.tvLateCharges.text = "₹" + model.lateCharges
                else
                    screen.tvLateCharges.text = "₹" + (model.lateCharges.toInt() * counts).toString()

                if (sdf.parse(model.maintenanceDueDate) < sdf.parse(currDate)) {

                    screen.tvWarning.text =
                        "* Your Maintenance Due Date has passed Late Charges\n   will be Applied Now."

                    screen.tvAmount.text = "₹" + model.maintenanceAmount

                } else
                    screen.tvAmount.text = "₹" + model.maintenanceAmount
            } else
                screen.tvAmount.text = "₹" + model.maintenanceAmount
        } else {
            Toast.makeText(this, "Could not find data", Toast.LENGTH_SHORT).show()
            finish()
        }    }

}