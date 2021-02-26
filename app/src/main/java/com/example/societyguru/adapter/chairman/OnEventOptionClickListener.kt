package com.example.societyguru.adapter.chairman

import com.example.societyguru.model.SocietyEventModel

interface OnEventOptionClickListener {
    fun showEventInfo(model: SocietyEventModel)
}