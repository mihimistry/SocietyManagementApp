package com.maxgen.societyguru.adapter

import com.maxgen.societyguru.model.SocietyMaintenanceModel

interface OnMaintenanceOptionClick {

    fun showMaintenanceInfo(
        model: SocietyMaintenanceModel,
        counts: Int
    )
}