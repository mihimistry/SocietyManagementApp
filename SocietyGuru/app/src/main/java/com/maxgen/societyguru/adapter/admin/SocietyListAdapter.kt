package com.maxgen.societyguru.adapter.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.databinding.SocietyListViewBinding
import com.maxgen.societyguru.enums.SocietyStatus
import com.maxgen.societyguru.model.SocietyModel

class SocietyListAdapter(
    options: FirestoreRecyclerOptions<SocietyModel>,
    private val optionClick: OnSocietyOptionClick
) :
    FirestoreRecyclerAdapter<SocietyModel, SocietyListAdapter.AdapterViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder =
        AdapterViewHolder(
            SocietyListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int, model: SocietyModel) =
        holder.setData(model, optionClick)


    class AdapterViewHolder(val view: SocietyListViewBinding) : RecyclerView.ViewHolder(view.root) {
        fun setData(model: SocietyModel, optionClick: OnSocietyOptionClick) {
            view.model = model
            view.tvStatus.setTextColor(if (model.status == SocietyStatus.BLOCKED.name) Color.RED else Color.GREEN)
            view.btnMore.setOnClickListener { optionClick.showOptions(model, view.btnMore) }
            view.root.setOnClickListener { optionClick.showSocietyInfo(model) }
        }
    }

}