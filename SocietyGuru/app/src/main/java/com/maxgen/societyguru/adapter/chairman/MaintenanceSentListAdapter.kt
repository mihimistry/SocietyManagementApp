package com.maxgen.societyguru.adapter.chairman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.adapter.OnMaintenanceOptionClick
import com.maxgen.societyguru.databinding.MaintenanceListViewBinding
import com.maxgen.societyguru.model.SocietyMaintenanceModel

class MaintenanceSentListAdapter(
options: FirestoreRecyclerOptions<SocietyMaintenanceModel>,
private val optionClick: OnMaintenanceOptionClick
) : FirestoreRecyclerAdapter<SocietyMaintenanceModel, MaintenanceSentListAdapter.AdapterViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder =
        AdapterViewHolder(
            MaintenanceListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    fun deleteItem(position: Int) {
        snapshots.getSnapshot(position).reference.delete()
        notifyItemRemoved(position)
    }
    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int, model: SocietyMaintenanceModel) =
        holder.setData(model, optionClick)

    class AdapterViewHolder(val view: MaintenanceListViewBinding) : RecyclerView.ViewHolder(view.root) {
        fun setData(model: SocietyMaintenanceModel, optionClick: OnMaintenanceOptionClick) {
            view.maintenance = model

            view.root.setOnClickListener { optionClick.showMaintenanceInfo(model, 0) }

            if (model.maintenanceDescription!="")
                view.tvDescription.visibility=View.VISIBLE

            view.tvAmount.text=model.maintenanceAmount


        }
    }

}