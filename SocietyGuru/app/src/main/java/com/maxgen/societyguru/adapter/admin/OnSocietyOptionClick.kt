package com.maxgen.societyguru.adapter.admin

import android.view.View
import com.maxgen.societyguru.model.SocietyModel

interface OnSocietyOptionClick {

    fun showOptions(model: SocietyModel, anchor: View)

    fun showSocietyInfo(model: SocietyModel)

}