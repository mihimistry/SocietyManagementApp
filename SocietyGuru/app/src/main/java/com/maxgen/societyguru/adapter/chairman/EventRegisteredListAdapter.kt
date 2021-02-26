package com.maxgen.societyguru.adapter.chairman

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.databinding.PaidEventUserListViewBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.model.UserModel
import com.maxgen.societyguru.model.member.PaidForEventModel

class EventRegisteredListAdapter(
    options: FirestoreRecyclerOptions<PaidForEventModel>,
    private val listener: OnRegisteredEventClickListener
) :
    FirestoreRecyclerAdapter<PaidForEventModel, EventRegisteredListAdapter.RegisteredViewHolder>(
        options
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = RegisteredViewHolder(
        PaidEventUserListViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: RegisteredViewHolder,
        position: Int,
        model: PaidForEventModel
    ) = holder.setData(model, listener)


    class RegisteredViewHolder(val view: PaidEventUserListViewBinding) :
        RecyclerView.ViewHolder(view.root), FireAccess.MemberInfoListener,
        FireAccess.SocietyInfoListener {

        fun setData(model: PaidForEventModel, listener: OnRegisteredEventClickListener) {
            FireAccess.getSocietyInfo(model.userId, this)
            view.tvChairmanMobile.setOnClickListener {
                listener.callUser(view.tvChairmanMobile.text.toString().trim())
            }
            FireAccess.getUser(model.userId, this)
        }

        override fun userReceived(flag: Boolean, error: String?, model: UserModel?) {
            if (flag && model != null) {
                FireAccess.getSocietyInfo(model.societyId, this)
                view.tvChairmanName.text = "${model.fName}  ${model.lName}"
                view.tvChairmanMobile.text = model.mobile
                view.tvFlatHouseNo.text = model.flatHouseNumber
            } else Toast.makeText(view.root.context, error, Toast.LENGTH_SHORT).show()
        }

        override fun listenSocietyInfo(model: SocietyModel?, flag: Boolean, error: String?) {
            if (flag && model != null) view.tvSocietyName.text =
                "${model.sname}, ${model.area}, ${model.city}"
        }

    }

}