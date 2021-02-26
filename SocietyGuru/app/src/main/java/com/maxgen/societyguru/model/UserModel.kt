package com.maxgen.societyguru.model

open class UserModel(
    var fName: String = "",
    var lName: String = "",
    var email: String = "",
    var mobile: String = "",
    var password: String = "",
    var userType: String = "",
    var societyId: String = "",
    var searchName: String = "",
    var status: String = "",
    var token: String = "",
    var flatHouseNumber: String = ""
) {
    enum class UserEnum {
        USER,
        fName,
        lName,
        email,
        mobile,
        password,
        userType,
        searchName,
        societyId,
        status,
        token,
        flatHouseNumber
    }
}