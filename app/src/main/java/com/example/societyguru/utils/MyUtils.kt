package com.example.societyguru.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.util.DisplayMetrics
import android.widget.EditText
import com.kaopiz.kprogresshud.KProgressHUD
import com.example.societyguru.model.SocietyModel
import com.example.societyguru.model.UserModel
import java.security.MessageDigest

object MyUtils {

    private var progressHUD: KProgressHUD? = null
    fun showProgress(context: Context, title: String, desc: String?, cancelable: Boolean) {
        progressHUD =
            KProgressHUD(context).setLabel(title).setCancellable(cancelable).setAnimationSpeed(2)
                .setDimAmount(0.5f)
        if (desc == null) progressHUD?.setDetailsLabel("please wait") else progressHUD?.setDetailsLabel(
            desc
        )
        if (isNetworkAvailable(context)) progressHUD?.show()
    }

    fun dismissProgress() {
        progressHUD?.let {
            if (it.isShowing) it.dismiss()
        }
    }

    fun getEDTText(edt: EditText?): String {
        return edt?.text?.toString()?.trim { it <= ' ' } ?: ""
    }

    fun setEDTError(edt: EditText, error: String) {
        edt.error = error
    }

    fun getScreenHeightPercent(activity: Activity, size: Float): Int {
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        return (metrics.heightPixels * size / 100).toInt()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun dialPerson(context: Context, mobile: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$mobile")
        context.startActivity(intent)
    }

    fun getUserAddress(context: Context, societyModel: SocietyModel) =
        "${SharedPreferenceUser.getInstance()
            .getUser(context).flatHouseNumber}, ${societyModel.sname}, ${societyModel.area}, ${societyModel.city} , ${societyModel.state}, ${societyModel.country} - ${societyModel.pinCode}"

    fun getUserModel(context: Context): UserModel =
        SharedPreferenceUser.getInstance().getUser(context)

    fun getUserId(context: Context) = getUserModel(context).email

    fun getUserType(context: Context) = getUserModel(context).userType

    fun getUserMobile(context: Context) = getUserModel(context).mobile

    fun getUserFirstName(context: Context) = getUserModel(context).fName

    fun getUserSocietyId(context: Context) = getUserModel(context).societyId

    fun getUserPass(context: Context) = getUserModel(context).password

    fun hashCal(type: String, hashString: String): String {
        return MessageDigest
            .getInstance(type)
            .digest(hashString.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }

}