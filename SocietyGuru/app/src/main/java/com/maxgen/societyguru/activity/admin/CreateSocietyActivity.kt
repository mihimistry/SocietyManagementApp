package com.maxgen.societyguru.activity.admin

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.databinding.ActivityCreateSocietyBinding
import com.maxgen.societyguru.enums.*
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.model.SocietyModel.SocietyEnum.*
import com.maxgen.societyguru.model.UserModel
import com.maxgen.societyguru.utils.MyUtils.dismissProgress
import com.maxgen.societyguru.utils.MyUtils.getEDTText
import com.maxgen.societyguru.utils.MyUtils.isNetworkAvailable
import com.maxgen.societyguru.utils.MyUtils.setEDTError
import com.maxgen.societyguru.utils.MyUtils.showProgress


class CreateSocietyActivity : AppCompatActivity() {

    private lateinit var screen: ActivityCreateSocietyBinding

    private val societyName get() = getEDTText(screen.edtName)
    private val societyArea get() = getEDTText(screen.edtArea)
    private val societyCity get() = getEDTText(screen.edtCity)
    private val societyState get() = getEDTText(screen.edtState)
    private val societyCountry get() = getEDTText(screen.edtCountry)
    private val societyPinCode get() = getEDTText(screen.edtPincode)
    private val chairmanFName get() = getEDTText(screen.edtChairmanFName)
    private val chairmanLName get() = getEDTText(screen.edtChairmanLName)
    private val chairmanEmail get() = getEDTText(screen.edtChairmanEmail)
    private val chairmanMobile get() = getEDTText(screen.edtChairmanMobile)
    private val chairmanPass get() = getEDTText(screen.edtChairmanPass)
    private val chairmanCPass get() = getEDTText(screen.edtChairmanCPass)
    private val chairmanFlatHouseNo get() = getEDTText(screen.edtChairmanFlatHouseNo)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        screen = ActivityCreateSocietyBinding.inflate(layoutInflater)
        setContentView(screen.root)
        screen.btnCreate.setOnClickListener {
            if (isNetworkAvailable(this@CreateSocietyActivity)) validateAndCreate()
            else Toast.makeText(
                this@CreateSocietyActivity,
                "No internet connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateAndCreate() {
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
            setEDTError(screen.edtChairmanFName, "Please enter first name.")
            flag = false
        }
        if (chairmanLName.isEmpty()) {
            setEDTError(screen.edtChairmanLName, "Please enter last name")
            flag = false
        }
        if (chairmanEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(chairmanEmail).matches()) {
            setEDTError(screen.edtChairmanEmail, "Please enter valid email.")
            flag = false
        }
        if (chairmanMobile.isEmpty() || !Patterns.PHONE.matcher(chairmanMobile).matches()) {
            setEDTError(screen.edtChairmanMobile, "Please enter valid mobile number.")
            flag = false
        }
        if (chairmanFlatHouseNo.isEmpty()) {
            setEDTError(screen.edtChairmanFlatHouseNo, "Please enter flat / house number.")
            flag = false
        }
        if (chairmanPass.isEmpty() || chairmanPass.length < 6) {
            setEDTError(screen.edtChairmanPass, "Password must be 6 characters long.")
            flag = false
        }
        if (chairmanCPass.isEmpty() || chairmanPass != chairmanCPass) {
            setEDTError(screen.edtChairmanCPass, "Password does not match.")
            flag = false
        }

        if (flag) checkAndCreateSociety()
    }

    private fun checkAndCreateSociety() {
        showProgress(this, "Validating Society", null, false)
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .whereEqualTo(sname.name, societyName)
            .whereEqualTo(area.name, societyArea)
            .whereEqualTo(city.name, societyCity)
            .whereEqualTo(country.name, societyCountry)
            .whereEqualTo(pinCode.name, societyPinCode)
            .whereEqualTo(state.name, societyState)
            .limit(1).get()
            .addOnCompleteListener { task ->
                dismissProgress()
                if (task.isSuccessful) {
                    task.result?.let {
                        if (it.isEmpty) createSociety()
                        else {
                            Toast.makeText(
                                this@CreateSocietyActivity,
                                "This society already registered.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    if (task.result == null) {
                        Toast.makeText(
                            this@CreateSocietyActivity,
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    task.exception?.let {
                        Toast.makeText(
                            this@CreateSocietyActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun createSociety() {
        showProgress(this, "Creating Society", null, false)
        FirebaseFirestore.getInstance()
            .collection(FirebaseCollectionName.SOCIETIES.name).add(
                SocietyModel(
                    chairmanEmail=chairmanEmail,
                    sname = societyName,
                    area = societyArea,
                    city = societyCity,
                    country = societyCountry,
                    pinCode = societyPinCode,
                    state = societyState,
                    searchName=societyName.trim().toLowerCase(),
                    status = SocietyStatus.ACTIVE.name
                )
            ).addOnSuccessListener {
                dismissProgress()
                checkAndStoreChairman(it.id)
            }.addOnFailureListener {
                dismissProgress()
                Toast.makeText(
                    this@CreateSocietyActivity,
                    "${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkAndStoreChairman(societyId: String) {
        showProgress(this, "Checking Chairman", null, false)
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.email.name, chairmanEmail).limit(1).get()
            .addOnCompleteListener { task ->
                dismissProgress()
                if (task.isSuccessful) task.result?.let {
                    if (it.isEmpty) storeChairman(societyId)
                    else {
                        Toast.makeText(
                            this@CreateSocietyActivity,
                            "Email id already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                        removeSociety(societyId)
                    }
                }
                else {
                    task.exception?.let {
                        Toast.makeText(
                            this@CreateSocietyActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    removeSociety(societyId)
                }
            }
    }

    private fun storeChairman(societyId: String) {
        showProgress(this, "Registering Chairman", null, false)
        FirebaseFirestore.getInstance()
            .collection(FirebaseCollectionName.USERS.name)
            .document(getEDTText(screen.edtChairmanEmail)).set(
                UserModel(
                    fName = chairmanFName,
                    email = chairmanEmail,
                    lName = chairmanLName,
                    mobile = chairmanMobile,
                    password = chairmanPass,
                    societyId = societyId,
                    searchName=chairmanFName.toLowerCase()+chairmanEmail.toLowerCase(),
                    userType = UserType.CHAIRMAN.name,
                    status = UserStatus.ACTIVE.name
                )
            ).addOnSuccessListener {
                dismissProgress()
                createChairmanUser(societyId)
            }.addOnFailureListener {
                dismissProgress()
                Toast.makeText(
                    this@CreateSocietyActivity,
                    "${it.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                removeSociety(societyId)
            }
    }

    private fun createChairmanUser(societyId: String) {
        showProgress(this, "Creating Chairman Account", null, false)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(chairmanEmail, chairmanPass)
            .addOnSuccessListener {
                dismissProgress()
                setSocietyId(societyId)
            }.addOnFailureListener {
                dismissProgress()
                Toast.makeText(this@CreateSocietyActivity, "${it.message}", Toast.LENGTH_SHORT)
                    .show()
                removeChairman()
                removeSociety(societyId)
            }
    }

    private fun setSocietyId(societyId: String) {
        showProgress(this, "Finishing up", null, false)
        val map = HashMap<String, Any>()
        map[SocietyModel.SocietyEnum.societyId.name] = societyId
        map[members.name] = 1
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(societyId).update(map).addOnSuccessListener {
                dismissProgress()
                Toast.makeText(
                    this@CreateSocietyActivity,
                    "Society created with Chairman account.",
                    Toast.LENGTH_SHORT
                ).show()

                FireAccess.increaseSociety()
                FireAccess.increaseChairman()
                finish()
            }.addOnFailureListener { setSocietyId(societyId) }
    }

    private fun removeSociety(societyId: String) {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(societyId).delete().addOnSuccessListener { }
            .addOnFailureListener { removeSociety(societyId) }

        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.TOTAL.name)
            .document(General.societies.name)
            .update(General.totalSocieties.name, FieldValue.increment(-1))

    }

    private fun removeChairman() {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(chairmanEmail).delete().addOnSuccessListener { }
            .addOnFailureListener { removeChairman() }

        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.TOTAL.name)
            .document(General.chairmen.name)
            .update(General.totalChairmen.name, FieldValue.increment(-1))

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

}