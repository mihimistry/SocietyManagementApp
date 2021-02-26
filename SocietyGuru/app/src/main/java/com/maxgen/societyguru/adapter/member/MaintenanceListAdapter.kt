package com.maxgen.societyguru.adapter.member

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.adapter.OnMaintenanceOptionClick
import com.maxgen.societyguru.databinding.MaintenanceListViewBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import java.text.SimpleDateFormat
import java.util.*

class MaintenanceListAdapter(
    private val userId: String,
    options: FirestoreRecyclerOptions<SocietyMaintenanceModel>,
    private val optionClick: OnMaintenanceOptionClick
) : FirestoreRecyclerAdapter<SocietyMaintenanceModel, MaintenanceListAdapter.AdapterViewHolder>(
    options
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder =
        AdapterViewHolder(
            MaintenanceListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(
        holder: AdapterViewHolder,
        position: Int,
        model: SocietyMaintenanceModel
    ) =
        holder.setData(userId, model, optionClick)

    fun deleteItem(position: Int) {
        snapshots.getSnapshot(position).reference.delete()
    }

    class AdapterViewHolder(val view: MaintenanceListViewBinding) :
        RecyclerView.ViewHolder(view.root),
        FireAccess.OnMaintenancePaymentStatusListener,
        FireAccess.OnMaintenanceSeenListner {
        fun setData(
            userId: String,
            model: SocietyMaintenanceModel,
            optionClick: OnMaintenanceOptionClick
        ) {

            view.maintenance = model

            FireAccess.getNotSeenMaintenance(model.maintenanceId,userId,this)
            val currDate: String =
                SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time).toString()

            val sdf = SimpleDateFormat("dd/MM/yyyy")


            val counts = monthsBetween(
                sdf.parse(model.maintenanceDueDate),
                sdf.parse(currDate)
            )

            if (sdf.parse(model.maintenanceDueDate) < sdf.parse(currDate)) {
                if (model.lateCharges != "") {


                    view.tvAmount.text =
                        ((model.maintenanceAmount.toInt()) + (model.lateCharges.toInt()) * counts).toString()
                } else
                    view.tvAmount.text = model.maintenanceAmount
            } else
                view.tvAmount.text = model.maintenanceAmount


            if (model.maintenanceDescription == "")
                view.tvDescription.visibility = View.GONE

            view.root.setOnClickListener { optionClick.showMaintenanceInfo(model, counts) }

            FireAccess.getMaintenancePaymentStatus(model.maintenanceId, userId, this)
        }

        private fun monthsBetween(dueDate: Date?, currDate: Date?): Int {

            val start = Calendar.getInstance()
            start.time = dueDate

            val end = Calendar.getInstance()
            end.time = currDate

            var monthsBetween = 0
            var dateDiff = end[Calendar.DAY_OF_MONTH] - start[Calendar.DAY_OF_MONTH]

            if (dateDiff < 0) {
                val borrrow = start.getActualMaximum(Calendar.DAY_OF_MONTH)
                dateDiff =
                    end[Calendar.DAY_OF_MONTH] + borrrow - start[Calendar.DAY_OF_MONTH]
                monthsBetween--
                if (dateDiff > 0) {
                    monthsBetween++
                }
            } else {
                monthsBetween++
            }
            monthsBetween += end[Calendar.MONTH] - start[Calendar.MONTH]
            monthsBetween += (end[Calendar.YEAR] - start[Calendar.YEAR]) * 12
            return monthsBetween

            val cal = Calendar.getInstance()
            if (dueDate != null) {
                if (dueDate.before(currDate)) {
                    cal.time = dueDate
                } else {
                    cal.time = currDate
                    currDate = dueDate
                }
            }
            var c = 0
            while (cal.time.before(currDate)) {
                cal.add(Calendar.MONTH, 1)
                c++
            }

            return c - 1
        }


        override fun paymentStatus(flag: Boolean) {
            if (flag) {
                view.tvPaymentStatus.text = "PAID"
            } else
                view.tvPaymentStatus.text = "NOT PAID"
        }

        override fun notSeenMaintenance(flag: Boolean, error: String?) {
            if (flag) view.tvNew.visibility = View.VISIBLE
            else view.tvNew.visibility = View.GONE
        }
    }

}