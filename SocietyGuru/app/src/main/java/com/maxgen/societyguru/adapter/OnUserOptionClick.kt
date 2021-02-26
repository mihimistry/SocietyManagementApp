package com.maxgen.societyguru.adapter

import android.view.View
import com.maxgen.societyguru.model.UserModel

interface OnUserOptionClick {
    fun showOptions(model: UserModel, anchor: View)
    fun showUserInfo(model: UserModel)
}