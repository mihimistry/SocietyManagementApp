package com.maxgen.societyguru.activity

import android.R
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.databinding.ActivityRegisterForNewSocietyBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.enums.UserStatus
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.model.UserModel
import com.maxgen.societyguru.utils.MyUtils
import com.maxgen.societyguru.utils.MyUtils.dismissProgress

class RegisterForNewSocietyActivity : AppCompatActivity() {
    private lateinit var screen: ActivityRegisterForNewSocietyBinding

    private val fName get() = MyUtils.getEDTText(screen.edtFName)
    private val lName get() = MyUtils.getEDTText(screen.edtLName)
    private val mobile get() = MyUtils.getEDTText(screen.edtMobile)
    private val society get() = screen.spnrSociety.selectedItemPosition
    private val flatHouseNo get() = MyUtils.getEDTText(screen.edtFlatHouseNo)

    private val societyList: ArrayList<SocietyModel> = ArrayList()
    private lateinit var email: String
    private lateinit var pass: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityRegisterForNewSocietyBinding.inflate(layoutInflater)
        setContentView(screen.root)

        val b = intent.extras
        if (b != null) {
            email = b.getString("userEmail", "")
            pass = b.getString("userPass", "")
        }
        loadSocieties()
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
                this@RegisterForNewSocietyActivity.societyList.clear()
                this@RegisterForNewSocietyActivity.societyList.add(
                    SocietyModel(
                        sname = "Select Society",
                        area = ""
                    )
                )
                if (flag) this@RegisterForNewSocietyActivity.societyList.addAll(societyList)
                else Toast.makeText(this@RegisterForNewSocietyActivity, error, Toast.LENGTH_SHORT)
                    .show()
                val spinnerAdapter =
                    ArrayAdapter(
                        this@RegisterForNewSocietyActivity,
                        R.layout.simple_spinner_dropdown_item,
                        this@RegisterForNewSocietyActivity.societyList
                    )
                screen.spnrSociety.adapter = spinnerAdapter
            }
        })
    }

    private fun validateAndCreateUser() {
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

        if (!Patterns.PHONE.matcher(mobile).matches()) {
            MyUtils.setEDTError(screen.edtMobile, "Please enter valid mobile number.")
            flag = false
        }

        if (society < 1) Toast.makeText(this, "Please select society.", Toast.LENGTH_SHORT).show()
        if (flag) {
            checkAndCreateUser()
        }
    }

    private fun checkAndCreateUser() {
        MyUtils.showProgress(this, "Registering User", null, false)
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(email)
            .get()
            .addOnSuccessListener {
                dismissProgress()
                if (it != null && it.exists()) {
                    Toast.makeText(
                        this@RegisterForNewSocietyActivity,
                        "Email address already in use.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    FireAccess.createUser(email, pass, object : FireAccess.CheckListener {
                        override fun listen(flag: Boolean, error: String?) {
                            if (flag) {
                                storeNewUser()
                            } else {
                                storeNewUser()
                            }
                        }
                    })
                }
            }.addOnFailureListener {
                dismissProgress()
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun storeNewUser() {
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
                    MyUtils.dismissProgress()
                    Toast.makeText(
                        this@RegisterForNewSocietyActivity,
                        "Registration success, Please wait for Chairman's Approval.",
                        Toast.LENGTH_SHORT
                    ).show()
                    FireAccess.increaseUser()
                    FireAccess.increaseSocietyMembers(societyList[screen.spnrSociety.selectedItemPosition].societyId)
                    finish()
                } else {
                    MyUtils.dismissProgress()
                    Toast.makeText(
                        this@RegisterForNewSocietyActivity,
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

}