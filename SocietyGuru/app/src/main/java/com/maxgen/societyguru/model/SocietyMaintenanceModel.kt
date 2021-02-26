package com.maxgen.societyguru.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class SocietyMaintenanceModel(
    var maintenanceId: String = "",
    var societyId: String = "",
    var from: String = "",
    var maintenanceMonth: String = "",
    var maintenanceDescription: String = "",
    var maintenanceDueDate: String = "",
    var maintenanceAmount: String = "",
    var lateCharges: String = "",
    var createdAt: Timestamp = Timestamp.now()
) {

    fun getCreatedDateFormat(): String {
        return SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(createdAt.toDate())
    }

    enum class MaintenanceEnum {
        maintenanceId,
        societyId,
        from,
        maintenanceMonth,
        maintenanceDescription,
        maintenanceDueDate,
        maintenanceAmount,
        lateCharges,
        createdAt
    }

    class MaintenanceTo(
        var to: String = "",
        var from: String = "",
        var paidDate: String = "",
        var maintenancePaid: String = "",
        var maintenanceMonth: String = "",
        var maintenanceDescription: String = "",
        var maintenanceDueDate: String = "",
        var maintenanceAmount: String = "",
        var noticeSent: String = "",
        var icon: String = "",
        var lateCharges: String = "",
        var memberName: String = "",
        var memberContact: String = "",
        var maintenanceId: String = "",
        var id: String = "",
        var societyId: String = "",
        var seen: String = "",
        var createdAt: Timestamp = Timestamp.now()

    ) {

        enum class MaintenancePaidEnum {
            to,
            paidDate,
            maintenanceId,
            maintenancePaid,
            maintenanceMonth,
            maintenanceDescription,
            maintenanceDueDate,
            maintenanceAmount,
            memberName,
            memberContact,
            lateCharges,
            seen
        }
    }

}