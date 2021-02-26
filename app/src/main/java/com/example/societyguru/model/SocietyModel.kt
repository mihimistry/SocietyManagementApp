package com.example.societyguru.model

import com.example.societyguru.enums.SocietyStatus

data class SocietyModel(
    var societyId: String = "",
    var chairmanEmail: String = "",
    var chairmanContact: String = "",
    var sname: String = "",
    var area: String = "",
    var city: String = "",
    var state: String = "",
    var country: String = "",
    var pinCode: String = "",
    var status: String = "",
    var searchName: String = "",
    var members: Int = 0,
    val entryStatus: String = SocietyStatus.BLOCKED.name
) {

    enum class SocietyEnum {
        SOCIETY,
        societyId,
        chairmanEmail,
        chairmanContact,
        sname,
        area,
        city,
        state,
        country,
        pinCode,
        status,
        members,
        entryStatus
    }

    override fun toString(): String {
        if (area.isEmpty()) return sname
        return "$sname, $area, $city, $state, $country, $pinCode"
    }

}