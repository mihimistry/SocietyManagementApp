package com.maxgen.societyguru.adapter

import android.view.View
import com.maxgen.societyguru.model.SocietyMaintenanceModel

interface sendNoticeOptionClick {

    fun showOptions(model: SocietyMaintenanceModel.MaintenanceTo, anchor: View)
}