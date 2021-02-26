package com.example.societyguru.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class SocietyEventModel(
    var id: String = "",
    var societyId: String = "",
    var eventTitle: String = "",
    var eventDescription: String = "",
    var eventStartDate: String = "",
    var eventEndDate: String = "",
    var eventStartTime: String = "",
    var eventEndTime: String = "",
    var amount: String = "",
    var chargesPer: String = "",
    var createdAt: Timestamp = Timestamp.now()
) {

    fun getCreatedDateFormat(): String {
        return SimpleDateFormat("hh:mm a (dd/MM/yyyy)", Locale.ENGLISH).format(createdAt.toDate())
    }
    enum class EventEnum {
        id,
        societyId,
        eventTitle,
        eventDescription,
        eventStartDate,
        eventEndDate,
        eventStartTime,
        eventEndTime,
        amount,
        chargesPer,
        createdAt,
        eventId,
        userId,
        userRegistered,
        transactionID,
        attendingPersons,
        totalCharge,
        paidDate,
        paidTime,
        seen,
        to
    }

    class EventTo(
        var id: String = "",
        var societyId: String = "",
        var eventTitle: String = "",
        var eventDescription: String = "",
        var eventStartDate: String = "",
        var eventEndDate: String = "",
        var eventStartTime: String = "",
        var eventEndTime: String = "",
        var amount: String = "",
        var createdAt: Timestamp = Timestamp.now(),
        var eventId: String = "",
        var userRegistered:String="",
        var transactionID: String = "",
        var attendingPersons: String = "",
        var totalCharge: String = "",
        var chargesPer: String = "",
        var paidDate: String = "",
        var paidTime: String = "",
        var seen:String="",
        var to:String=""
    )
}