package com.example.societyguru.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class NoticeModel(
    var noticeId: String = "",
    var from: String = "",
    var title: String = "",
    var description: String = "",
    var createdAt: Timestamp = Timestamp.now()
) {

    fun getCreatedDateFormat(): String {
        return SimpleDateFormat("hh:mm a (dd/MM/yyyy)", Locale.ENGLISH).format(createdAt.toDate())
    }

    enum class NoticeEnum { from, to, title, description, noticeSent, seen, NOTICETO, noticeId ,createdAt}

    class NoticeTo(
        var to: String = "",
        var from: String = "",
        var title: String = "",
        var description: String = "",
        var noticeSent: String = "",
        var seen:String="",
        var icon: String = "",
        var noticeId: String = "",
        var createdAt: Timestamp = Timestamp.now()
    )

}