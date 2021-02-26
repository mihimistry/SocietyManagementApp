package com.example.societyguru.model.member

class PaidMaintenanceModel(
    val paidDate: String = "",
    val paidAmount: String = "",
    val month: String = "",
    val userId: String = ""
) {
    enum class PaidMaintenanceEnum {
        paidDate,
        paidAmount,
        month,
        userId,
    }
}