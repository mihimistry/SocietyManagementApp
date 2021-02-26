package com.example.societyguru.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.societyguru.databinding.NoticeSendToListViewBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyModel
import com.example.societyguru.model.UserCheckboxModel
import com.example.societyguru.model.UserModel

class NoticeSendToListAdapter(
    options: FirestoreRecyclerOptions<UserCheckboxModel>,
    private val optionClick: OnNoticeSentToOptionClick
) : FirestoreRecyclerAdapter<UserCheckboxModel,
        NoticeSendToListAdapter.AdapterViewHolder>(options) {

    val selectedModels: ArrayList<UserCheckboxModel> = ArrayList()
    val models: ArrayList<UserCheckboxModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AdapterViewHolder(
        NoticeSendToListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(
        holder: AdapterViewHolder,
        position: Int,
        model: UserCheckboxModel
    ) {
        holder.setData(model, optionClick, selectedModels)
        models.add(model)
    }

    class AdapterViewHolder(val view: NoticeSendToListViewBinding) :
        RecyclerView.ViewHolder(view.root), FireAccess.MemberInfoListener,
        FireAccess.SocietyInfoListener {

        fun setData(
            model: UserCheckboxModel,
            optionClick: OnNoticeSentToOptionClick,
            models: ArrayList<UserCheckboxModel>
        ) {

            view.check.setOnCheckedChangeListener { _, b ->
                model.checked = b
                if (!b && models.size > 0) models.remove(model)
                if (b) models.add(model)
            }
            view.check.isChecked = model.checked
            view.check.isChecked = model.checked
            view.tvChairmanMobile.setOnClickListener {
                optionClick.dialPerson(view.tvChairmanMobile.text.toString().trim())
            }
            FireAccess.getUser(model.email, this)
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
            if (flag && model != null) view.tvSocietyName.text = model.sname
            else Toast.makeText(view.root.context, error, Toast.LENGTH_SHORT).show()
        }

    }

}