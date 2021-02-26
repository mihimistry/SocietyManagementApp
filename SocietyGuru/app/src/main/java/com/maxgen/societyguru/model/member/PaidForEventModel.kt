package com.maxgen.societyguru.model.member

class PaidForEventModel(
    var eventId: String = "",
    var userId: String = "",
    var transactionID: String = "",
    var attendingPersons: String = "",
    var totalCharge: String = "",
    var chargesPer: String = "",
    var paidDate: String = "",
    var paidTime: String = ""

) {

    enum class PaidEventEnum {
        eventId,
        userId,
        transactionID,
        attendingPersons,
        totalCharge,
        chargesPer,
        paidDate,
        paidTime
    }

}