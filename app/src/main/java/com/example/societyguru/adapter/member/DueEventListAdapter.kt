package com.example.societyguru.adapter.member

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.societyguru.adapter.chairman.OnEventOptionClickListener
import com.example.societyguru.databinding.EventListViewBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyEventModel

class DueEventListAdapter(
    private val userId: String,
    options: FirestoreRecyclerOptions<SocietyEventModel>,
    private val optionClickListener: OnEventOptionClickListener
) : FirestoreRecyclerAdapter<SocietyEventModel, DueEventListAdapter.EventHolder>(options) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventHolder = EventHolder(
        EventListViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: EventHolder,
        position: Int,
        model: SocietyEventModel
    ) {
        holder.setData(userId, model, optionClickListener)
    }

    class EventHolder(val view: EventListViewBinding) : RecyclerView.ViewHolder(view.root),
        FireAccess.OnEventRegistrationStatusListener {
        fun setData(
            userId: String,
            model: SocietyEventModel,
            optionClickListener: OnEventOptionClickListener
        ) {
            view.root.setOnClickListener { optionClickListener.showEventInfo(model) }
            view.event = model
            if (model.amount == "0") view.tvAmount.text = "FREE"
            else view.tvAmount.text = model.amount + " / " + model.chargesPer
            FireAccess.getEventRegistrationStatus(model.id, userId, this)
        }

        override fun registrationStatus(flag: Boolean) {
            if (flag) {
                view.tvRegistrationStatus.text = "Registered"
                view.llEvent.visibility=View.GONE
                view.llEvent.layoutParams.height=0
            } else {
                view.tvRegistrationStatus.text = "Not Registered"
            }
        }
    }


}