package com.maxgen.societyguru.activity.admin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.maxgen.societyguru.databinding.ActivityUpdateSocietyBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.enums.SocietyStatus
import com.maxgen.societyguru.enums.UserStatus
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.model.UserModel
import com.maxgen.societyguru.utils.MyUtils.dismissProgress
import com.maxgen.societyguru.utils.MyUtils.getEDTText
import com.maxgen.societyguru.utils.MyUtils.setEDTError
import com.maxgen.societyguru.utils.MyUtils.showProgress

class UpdateSocietyActivity : AppCompatActivity() {

    private lateinit var screen: ActivityUpdateSocietyBinding
    private lateinit var societyId: String
    private lateinit var chairmanId: String

    private val societyName get() = getEDTText(screen.edtName)
    private val societyArea get() = getEDTText(screen.edtArea)
    private val societyCity get() = getEDTText(screen.edtCity)
    private val societyState get() = getEDTText(screen.edtState)
    private val societyCountry get() = getEDTText(screen.edtCountry)
    private val societyPinCode get() = getEDTText(screen.edtPincode)

    private val chairmanFName get() = getEDTText(screen.edtChairmanFName)
    private val chairmanLName get() = getEDTText(screen.edtChairmanLName)
    private val chairmanEmail get() = getEDTText(screen.edtChairmanEmail)
    private val chairmanPass get() = getEDTText(screen.edtChairmanPass)
    private val chairmanCPass get() = getEDTText(screen.edtChairmanCPass)
    private val chairmanMobile get() = getEDTText(screen.edtChairmanMobile)
    private val chairmanFlatHouseNo get() = getEDTText(screen.edtChairmanFlatHouseNo)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityUpdateSocietyBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setPage()
        screen.btnUpdate.setOnClickListener { validateAndUpdateSociety() }
    }

    private fun validateAndUpdateSociety() {
        var flag = true
        if (societyName.isEmpty()) {
            setEDTError(screen.edtName, "Please enter society name.")
            flag = false
        }
        if (societyArea.isEmpty()) {
            setEDTError(screen.edtArea, "Please enter area.")
            flag = false
        }
        if (societyCity.isEmpty()) {
            setEDTError(screen.edtCity, "Please enter city.")
            flag = false
        }
        if (societyState.isEmpty()) {
            setEDTError(screen.edtState, "Please enter state.")
            flag = false
        }
        if (societyCountry.isEmpty()) {
            setEDTError(screen.edtCountry, "Please enter country")
            flag = false
        }
        if (societyPinCode.isEmpty() || societyPinCode.length < 6) {
            setEDTError(screen.edtPincode, "Please enter valid pincode")
            flag = false
        }
        if (chairmanFName.isEmpty()) {
            setEDTError(screen.edtChairmanFName, "Please enter chairman first name.")
            flag = false
        }
        if (chairmanLName.isEmpty()) {
            setEDTError(screen.edtChairmanLName, "Please enter chairman last name.")
            flag = false
        }
        if (chairmanEmail.isEmpty()) {
            setEDTError(screen.edtChairmanEmail, "Please enter chairman email.")
            flag = false
        }
        if (chairmanPass.isEmpty()) {
            setEDTError(screen.edtChairmanPass, "Please enter chairman password.")
            flag = false
        }
        if (chairmanCPass.isEmpty() || chairmanCPass != chairmanPass) {
            setEDTError(screen.edtChairmanCPass, "Please enter correct password")
            flag = false
        }
        if (chairmanMobile.isEmpty() || chairmanMobile.length != 10) {
            setEDTError(screen.edtChairmanMobile, "Please enter valid mobile no.")
            flag = false
        }
        if (chairmanFlatHouseNo.isEmpty()) {
            setEDTError(screen.edtChairmanFlatHouseNo, "Please enter chairman house no.")
            flag = false
        }
        if (flag) {
            updateSociety()
            updateChairman()
        }
    }

    private fun updateChairman() {

        showProgress(this, "Updating", null, false)

        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.fName.name] = chairmanFName
        map[UserModel.UserEnum.lName.name] = chairmanLName
        map[UserModel.UserEnum.email.name] = chairmanEmail
        map[UserModel.UserEnum.password.name] = chairmanPass
        map[UserModel.UserEnum.mobile.name] = chairmanMobile
        map[UserModel.UserEnum.flatHouseNumber.name] = chairmanFlatHouseNo
        map[UserModel.UserEnum.userType.name] = UserType.CHAIRMAN.name

        if (chairmanId!="") {
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
                .document(chairmanId)
                .update(map)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@UpdateSocietyActivity,
                        "Chairman information updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this@UpdateSocietyActivity, "${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
        else{
            map[UserModel.UserEnum.societyId.name] = societyId
            map[UserModel.UserEnum.searchName.name] = chairmanFName.toLowerCase()+chairmanLName.toLowerCase()
            map[UserModel.UserEnum.status.name] = UserStatus.ACTIVE.name

            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
                .document(chairmanEmail)
                .set(map)
                .addOnSuccessListener {
                    val hasMap=HashMap<String,Any>()
                    hasMap[SocietyModel.SocietyEnum.chairmanEmail.name]=chairmanEmail
                    FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
                        .document(societyId)
                        .update(hasMap)
                        .addOnFailureListener {
                            Toast.makeText(this@UpdateSocietyActivity, "${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    Toast.makeText(
                        this@UpdateSocietyActivity,
                        "Chairman information updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this@UpdateSocietyActivity, "${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun updateSociety() {
        showProgress(this, "Updating", null, false)
        val map = HashMap<String, Any>()
        map[SocietyModel.SocietyEnum.sname.name] = societyName
        map[SocietyModel.SocietyEnum.area.name] = societyArea
        map[SocietyModel.SocietyEnum.city.name] = societyCity
        map[SocietyModel.SocietyEnum.state.name] = societyState
        map[SocietyModel.SocietyEnum.country.name] = societyCountry
        map[SocietyModel.SocietyEnum.pinCode.name] = societyPinCode
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(societyId)
            .update(map)
            .addOnSuccessListener {
                Toast.makeText(
                    this@UpdateSocietyActivity,
                    "Society information updated.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this@UpdateSocietyActivity, "${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun setPage() {
        val bundle = intent.extras
        if (bundle == null) {
            Toast.makeText(this, "Invalid launch.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        societyId = bundle.getString(SocietyModel.SocietyEnum.societyId.name, "")
        chairmanId = bundle.getString(SocietyModel.SocietyEnum.chairmanEmail.name, "")

        if (societyId.isEmpty()) {
            Toast.makeText(
                this@UpdateSocietyActivity,
                "Society ID not found.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }
        if (chairmanId.isEmpty()) {
            Toast.makeText(
                this@UpdateSocietyActivity,
                "Chairman not found.",
                Toast.LENGTH_SHORT
            ).show()
        }
        getSocietyInformation()
    }

    private fun getChairmanInformation() {
        showProgress(this, "Getting Chairman information", null, false)
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(chairmanId).get()
            .addOnSuccessListener {
                dismissProgress()
                if (it.exists()) fillChairmanInfo(it)
                else {
                    Toast.makeText(
                        this@UpdateSocietyActivity,
                        "Chairman data not found.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                dismissProgress()
                Toast.makeText(this@UpdateSocietyActivity, "${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun fillChairmanInfo(document: DocumentSnapshot?) {
        Log.d("UPDATE_CHAIRMAN_DATA", "${document?.data}")
        screen.edtChairmanFName.setText(document?.getString(UserModel.UserEnum.fName.name))
        screen.edtChairmanLName.setText(document?.getString(UserModel.UserEnum.lName.name))
        screen.edtChairmanEmail.setText(document?.getString(UserModel.UserEnum.email.name))
        screen.edtChairmanPass.setText(document?.getString(UserModel.UserEnum.password.name))
        screen.edtChairmanMobile.setText(document?.getString(UserModel.UserEnum.mobile.name))
        screen.edtChairmanFlatHouseNo.setText(document?.getString(UserModel.UserEnum.flatHouseNumber.name))

        if (screen.edtChairmanEmail.text.isNotEmpty())
            !screen.edtChairmanEmail.isFocusable
    }

    private fun getSocietyInformation() {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(societyId).get(Source.CACHE)
            .addOnSuccessListener {
                if (it.exists()) fillInfo(it)
                else {
                    Toast.makeText(
                        this@UpdateSocietyActivity,
                        "Society data not found.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this@UpdateSocietyActivity, "${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        if (chairmanId!=""){
            getChairmanInformation()}

    }

    private fun fillInfo(snapshot: DocumentSnapshot) {

        Log.d("UPDATE_SOCIETY_DATA", "${snapshot.data}")
        screen.edtName.setText(snapshot.getString(SocietyModel.SocietyEnum.sname.name))
        screen.edtArea.setText(snapshot.getString(SocietyModel.SocietyEnum.area.name))
        screen.edtCity.setText(snapshot.getString(SocietyModel.SocietyEnum.city.name))
        screen.edtState.setText(snapshot.getString(SocietyModel.SocietyEnum.state.name))
        screen.edtCountry.setText(snapshot.getString(SocietyModel.SocietyEnum.country.name))
        screen.edtPincode.setText(snapshot.getString(SocietyModel.SocietyEnum.pinCode.name))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}