package com.maxgen.societyguru.adapter.chairman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.maxgen.societyguru.adapter.OnMaintenanceOptionClick
import com.maxgen.societyguru.databinding.MaintenanceListViewBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyMaintenanceModel
import java.text.SimpleDateFormat
import java.util.*

class ChairmanMaintenanceListAdapter(
    private val optionClick: OnMaintenanceOptionClick,
    private var userId: String,
    diffUtil: DiffUtil.ItemCallback<SocietyMaintenanceModel>
) :
    ListAdapter<SocietyMaintenanceModel, ChairmanMaintenanceListAdapter.MaintenanceViewHolder>(
     diffUtil
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        return MaintenanceViewHolder(
            MaintenanceListViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {
        holder.setData(getItem(position), userId, optionClick)    }

    class MaintenanceViewHolder(val view: MaintenanceListViewBinding) :
        RecyclerView.ViewHolder(view.root),
        FireAccess.OnMaintenancePaymentStatusListener {


        fun setData(
            model: SocietyMaintenanceModel,
            userId: String,
            optionClick: OnMaintenanceOptionClick
        ) {
            view.maintenance = model
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

            view.root.setOnClickListener { optionClick.showMaintenanceInfo(model, counts) }

            if (model.maintenanceDescription == "")
                view.tvDescription.visibility = View.GONE

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
    }


}
