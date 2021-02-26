package com.example.societyguru.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.societyguru.databinding.UserListViewBinding
import com.example.societyguru.enums.UserStatus
import com.example.societyguru.model.UserModel

class UserListAdapter(
    options: FirestoreRecyclerOptions<UserModel>,
    private val optionClick: OnUserOptionClick
) : FirestoreRecyclerAdapter<UserModel, UserListAdapter.UserViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
        UserViewHolder(
            UserListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: UserModel) =
        holder.setData(model, optionClick)

    class UserViewHolder(val view: UserListViewBinding) : RecyclerView.ViewHolder(view.root) {
        fun setData(model: UserModel, optionClick: OnUserOptionClick) {
            view.model = model
            view.tvStatus.setTextColor(
                when (model.status) {
                    UserStatus.ACTIVE.name -> Color.GREEN
                    UserStatus.PENDING.name -> Color.YELLOW
                    UserStatus.BLOCKED.name -> Color.RED
                    else -> Color.BLACK
                }
            )

            view.root.setOnClickListener { optionClick.showUserInfo(model) }
            view.btnMore.setOnClickListener { optionClick.showOptions(model, view.btnMore) }
        }
    }

}