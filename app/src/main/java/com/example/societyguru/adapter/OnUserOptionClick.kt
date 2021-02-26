package com.example.societyguru.adapter

import android.view.View
import com.example.societyguru.model.UserModel

interface OnUserOptionClick {
    fun showOptions(model: UserModel, anchor: View)
    fun showUserInfo(model: UserModel)
}