package com.maxgen.societyguru.adapter.chairman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.databinding.EventListViewBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyEventModel

class EventListAdapter(
    private val userId: String,
    options: FirestoreRecyclerOptions<SocietyEventModel>,
    private val optionClickListener: OnEventOptionClickListener
) : FirestoreRecyclerAdapter<SocietyEventModel, EventListAdapter.EventHolder>(options) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = EventHolder(
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
        FireAccess.OnEventRegistrationStatusListener,FireAccess.OnEventSeenListner {
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
            FireAccess.getNotSeenEvent(model.id,userId,this)
        }

        override fun registrationStatus(flag: Boolean) {
            if (flag) view.tvRegistrationStatus.text = "Registered"
            else {view.tvRegistrationStatus.text = "Not Registered"
            }
        }

        override fun notSeenEvent(flag: Boolean, error: String?) {
            if (flag) view.tvNew.visibility = View.VISIBLE
            else view.tvNew.visibility = View.GONE        }
    }

}