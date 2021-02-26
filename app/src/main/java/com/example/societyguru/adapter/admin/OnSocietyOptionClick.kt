package com.example.societyguru.adapter.admin

import android.view.View
import com.example.societyguru.model.SocietyModel

interface OnSocietyOptionClick {

    fun showOptions(model: SocietyModel, anchor: View)

    fun showSocietyInfo(model: SocietyModel)

}