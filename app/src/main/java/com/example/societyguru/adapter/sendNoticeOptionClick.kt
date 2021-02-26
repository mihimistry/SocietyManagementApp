package com.example.societyguru.adapter

import android.view.View
import com.example.societyguru.model.SocietyMaintenanceModel

interface sendNoticeOptionClick {

    fun showOptions(model: SocietyMaintenanceModel.MaintenanceTo, anchor: View)
}