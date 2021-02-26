package com.example.societyguru.activity.chairman

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.databinding.ActivityEditProfileBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.MyUtils.dismissProgress
import com.example.societyguru.utils.SharedPreferenceUser


class EditProfileActivity : AppCompatActivity(), FireAccess.OnUserUpdatedListener {

    lateinit var screen: ActivityEditProfileBinding

    private val fName get() = MyUtils.getEDTText(screen.edtFName)
    private val lName get() = MyUtils.getEDTText(screen.edtLName)
    private val email get() = MyUtils.getUserId(this)
    private val mobile get() = MyUtils.getUserMobile(this)
    private val pass get() = MyUtils.getUserPass(this)
    private val flatHouseNo get() = MyUtils.getEDTText(screen.edtFlatHouseNo)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(screen.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val user = SharedPreferenceUser.getInstance().getUser(this)

        screen.edtFName.setText(user.fName)
        screen.edtLName.setText(user.lName)
        screen.edtEmail.setText(user.email)
        screen.edtMobile.setText(user.mobile)
        screen.edtPass.setText(user.password)
        screen.edtFlatHouseNo.setText(user.flatHouseNumber)
        screen.btnUpdate.setOnClickListener {
            validateAndUpdateUser()
        }

    }

    private fun validateAndUpdateUser() {
        var flag = true
        if (fName.isEmpty()) {
            MyUtils.setEDTError(screen.edtFName, "Please enter valid first name.")
            flag = false
        }
        if (lName.isEmpty()) {
            MyUtils.setEDTError(screen.edtLName, "Please enter valid last name.")
            flag = false
        }
        if (flatHouseNo.isEmpty()) {
            MyUtils.setEDTError(screen.edtFlatHouseNo, "Please enter flat / house number.")
            flag = false
        }
        if (flag) {
            updateUser()
        }
    }

    private fun updateUser() {
        MyUtils.showProgress(this, "Updating User", null, false)
        val model = UserModel(
            fName = fName,
            lName = lName,
            email = email,
            password = pass,
            mobile = mobile,
            flatHouseNumber = flatHouseNo
        )
        FireAccess.updateUserInfo(model, this)
    }

    override fun userUpdated(flag: Boolean, model: UserModel?, error: String?) {
        if (flag && model != null) {
            dismissProgress()
            SharedPreferenceUser.getInstance().userUpdate(model, this)
            Toast.makeText(
                this@EditProfileActivity,
                "Update success",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            dismissProgress()
            Toast.makeText(
                this@EditProfileActivity,
                error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}