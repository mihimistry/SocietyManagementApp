package com.maxgen.societyguru.adapter.chairman

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.adapter.sendNoticeOptionClick
import com.maxgen.societyguru.databinding.ChiarmanMemberMaintenanceListViewBinding
import com.maxgen.societyguru.model.SocietyMaintenanceModel

class MaintenancePaidMemberListAdapter(
    options: FirestoreRecyclerOptions<SocietyMaintenanceModel.MaintenanceTo>,
    private val optionClick:sendNoticeOptionClick
) : FirestoreRecyclerAdapter<SocietyMaintenanceModel.MaintenanceTo, MaintenancePaidMemberListAdapter.AdapterViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder =
        AdapterViewHolder(
            ChiarmanMemberMaintenanceListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )


    fun deleteItem(position: Int) {
        snapshots.getSnapshot(position).reference.delete()
    }
    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int, model: SocietyMaintenanceModel.MaintenanceTo) =
        holder.setData(position,model,optionClick)

    class AdapterViewHolder(val view: ChiarmanMemberMaintenanceListViewBinding) : RecyclerView.ViewHolder(view.root) {
        fun setData(position: Int,model: SocietyMaintenanceModel.MaintenanceTo,optionClick: sendNoticeOptionClick) {

            view.model = model
            view.btnMore.setOnClickListener { optionClick.showOptions(model, view.btnMore) }

        }
    }
}
