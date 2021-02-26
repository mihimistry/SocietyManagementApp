package com.example.societyguru.adapter.chairman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.societyguru.databinding.EventListViewBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyEventModel

class EventHistoryListAdapter(
    private val userEmail: String,
    societyEventRvOptions: FirestoreRecyclerOptions<SocietyEventModel>,
    private val optionClickListener: OnEventOptionClickListener
) : FirestoreRecyclerAdapter<SocietyEventModel, EventHistoryListAdapter.EventHistoryHolder>(
    societyEventRvOptions
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventHistoryHolder = EventHistoryHolder(
        EventListViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: EventHistoryHolder,
        position: Int,
        model: SocietyEventModel
    ) {
        holder.setData(userEmail, model, optionClickListener)
    }

    class EventHistoryHolder(val view: EventListViewBinding) : RecyclerView.ViewHolder(view.root),
        FireAccess.OnEventRegistrationStatusListener {
        fun setData(
            userEmail: String,
            model: SocietyEventModel,
            optionClickListener: OnEventOptionClickListener
        ) {
            view.root.setOnClickListener { optionClickListener.showEventInfo(model) }
            view.event = model
            if (model.amount == "0") view.tvAmount.text = "FREE"
            else view.tvAmount.text = model.amount + " / " + model.chargesPer
            FireAccess.getEventRegistrationStatus(model.id, userEmail, this)
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