package com.maxgen.societyguru.adapter.chairman

import com.maxgen.societyguru.model.SocietyEventModel

interface OnEventOptionClickListener {
    fun showEventInfo(model: SocietyEventModel)
}