package com.maxgen.societyguru.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.maxgen.societyguru.databinding.NoticeSentToListViewBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.model.UserModel

class NoticeSentToListAdapter(
        options: FirestoreRecyclerOptions<NoticeModel.NoticeTo>,
        private val optionClick: OnNoticeSentToOptionClick
) : FirestoreRecyclerAdapter<NoticeModel.NoticeTo, NoticeSentToListAdapter.AdapterViewHolder>(
        options
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder =
            AdapterViewHolder(
                    NoticeSentToListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

    override fun onBindViewHolder(
            holder: AdapterViewHolder,
            position: Int,
            model: NoticeModel.NoticeTo
    ) = holder.setData(model, optionClick)

    class AdapterViewHolder(val view: NoticeSentToListViewBinding) :
            RecyclerView.ViewHolder(view.root), FireAccess.MemberInfoListener,
            FireAccess.SocietyInfoListener {
        fun setData(model: NoticeModel.NoticeTo, optionClick: OnNoticeSentToOptionClick) {
            view.tvChairmanMobile.setOnClickListener {
                optionClick.dialPerson(view.tvChairmanMobile.text.toString().trim())

            }
            FireAccess.getUser(model.to, this)
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
            else Toast.makeText(view.root.context, error, Toast.LENGTH_SHORT).show()
        }

    }

}