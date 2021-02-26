package com.example.societyguru.adapter

import com.example.societyguru.model.SocietyMaintenanceModel

interface OnMaintenanceOptionClick {

    fun showMaintenanceInfo(
        model: SocietyMaintenanceModel,
        counts: Int
    )
}