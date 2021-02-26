package com.example.societyguru.activity

import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.example.societyguru.databinding.ActivityRegistrationBinding
import com.example.societyguru.enums.UserStatus
import com.example.societyguru.enums.UserType
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyModel
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils.dismissProgress
import com.example.societyguru.utils.MyUtils.getEDTText
import com.example.societyguru.utils.MyUtils.setEDTError
import com.example.societyguru.utils.MyUtils.showProgress

class RegistrationActivity : AppCompatActivity() {

    private lateinit var screen: ActivityRegistrationBinding

    private val fName get() = getEDTText(screen.edtFName)
    private val lName get() = getEDTText(screen.edtLName)
    private val email get() = getEDTText(screen.edtEmail)
    private val mobile get() = getEDTText(screen.edtMobile)
    private val pass get() = getEDTText(screen.edtPass)
    private val cPass get() = getEDTText(screen.edtCPass)
    private val society get() = screen.spnrSociety.selectedItemPosition
    private val flatHouseNo get() = getEDTText(screen.edtFlatHouseNo)

    private val societyList: ArrayList<SocietyModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(screen.root)
        loadSocieties()
        screen.tvLogin.setOnClickListener { finish() }
    }

    private fun loadSocieties() {
        FireAccess.getOpenSocieties(object : FireAccess.SocietyListListener {
            override fun listen(flag: Boolean, societyList: List<SocietyModel>, error: String) {
                if (error.equals("No societies found.", true)) {
                    Snackbar.make(
                        screen.root,
                        "Currently entries are blocked by Chairman, Contact your society chairman.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    screen.btnRegister.setOnClickListener { }
                } else {
                    screen.btnRegister.setOnClickListener { validateAndCreateUser() }
                }
                this@RegistrationActivity.societyList.clear()
                this@RegistrationActivity.societyList.add(
                    SocietyModel(
                        sname = "Select Society",
                        area = ""
                    )
                )

                if (flag) this@RegistrationActivity.societyList.addAll(societyList)
                else Toast.makeText(this@RegistrationActivity, error, Toast.LENGTH_SHORT).show()
                val spinnerAdapter =
                    ArrayAdapter(
                        this@RegistrationActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        this@RegistrationActivity.societyList
                    )
                screen.spnrSociety.adapter = spinnerAdapter
            }
        })
    }

    private fun validateAndCreateUser() {
        var flag = true
        if (fName.isEmpty()) {
            setEDTError(screen.edtFName, "Please enter valid first name.")
            flag = false
        }
        if (lName.isEmpty()) {
            setEDTError(screen.edtLName, "Please enter valid last name.")
            flag = false
        }
        if (flatHouseNo.isEmpty()) {
            setEDTError(screen.edtFlatHouseNo, "Please enter flat / house number.")
            flag = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setEDTError(screen.edtEmail, "Please enter valid email.")
            flag = false
        }
        if (!Patterns.PHONE.matcher(mobile).matches() || mobile.length != 10) {
            setEDTError(screen.edtMobile, "Please enter valid mobile number.")
            flag = false
        }
        if (pass.length < 6) {
            setEDTError(screen.edtPass, "Password must atleast 6 character long.")
            flag = false
        }
        if (pass != cPass) {
            setEDTError(screen.edtPass, "Password does not match.")
            flag = false
        }
        if (society < 1) Toast.makeText(this, "Please select society.", Toast.LENGTH_SHORT).show()
        if (flag) {
            checkAndCreateUser()
        }
    }

    private fun checkAndCreateUser() {
        showProgress(this, "Registering User", null, false)
        FireAccess.userExists(email, object : FireAccess.CheckListener {
            override fun listen(flag: Boolean, error: String?) {
                if (flag) {
                    dismissProgress()
                    Toast.makeText(
                        this@RegistrationActivity,
                        "Email address already in use.",
                        Toast.LENGTH_SHORT
                    ).show()
                    setEDTError(screen.edtEmail, "Email ID is already registered.")
                } else FireAccess.createUser(email, pass, object : FireAccess.CheckListener {
                    override fun listen(flag: Boolean, error: String?) {
                        if (flag) {
                            val model = UserModel(
                                fName = fName,
                                lName = lName,
                                status = UserStatus.PENDING.name,
                                userType = UserType.SOCIETY_MEMBER.name,
                                password = pass,
                                mobile = mobile,
                                email = email,
                                searchName = fName.toLowerCase() + lName.toLowerCase(),
                                societyId = societyList[screen.spnrSociety.selectedItemPosition].societyId,
                                flatHouseNumber = flatHouseNo
                            )
                            FireAccess.storeUser(model, object : FireAccess.CheckListener {
                                override fun listen(flag: Boolean, error: String?) {
                                    if (flag) {
                                        dismissProgress()
                                        Toast.makeText(
                                            this@RegistrationActivity,
                                            "Registration success, Please wait for Chairman's Approval.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        FireAccess.increaseUser()
                                        FireAccess.increaseSocietyMembers(societyList[screen.spnrSociety.selectedItemPosition].societyId)
                                        finish()
                                    } else {
                                        dismissProgress()
                                        Toast.makeText(
                                            this@RegistrationActivity,
                                            error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            })
                        } else {
                            Toast.makeText(this@RegistrationActivity, error, Toast.LENGTH_SHORT)
                                .show()
                            dismissProgress()
                        }
                    }
                })
            }
        })
    }

}