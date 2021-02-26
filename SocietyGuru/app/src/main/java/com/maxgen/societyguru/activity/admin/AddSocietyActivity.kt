package com.maxgen.societyguru.activity.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.databinding.ActivityAddSocietyBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.enums.SocietyStatus
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.utils.MyUtils

class AddSocietyActivity : AppCompatActivity() {
    private lateinit var screen:ActivityAddSocietyBinding

    private lateinit var userEmail: String
    private lateinit var societyId: String

    private val societyName get() = MyUtils.getEDTText(screen.edtName)
    private val societyArea get() = MyUtils.getEDTText(screen.edtArea)
    private val societyCity get() = MyUtils.getEDTText(screen.edtCity)
    private val societyState get() = MyUtils.getEDTText(screen.edtState)
    private val societyCountry get() = MyUtils.getEDTText(screen.edtCountry)
    private val societyPinCode get() = MyUtils.getEDTText(screen.edtPincode)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen= ActivityAddSocietyBinding.inflate(layoutInflater)
        setContentView(screen.root)

        val bundle = intent.extras
        if (bundle == null) {
            Toast.makeText(this, "Invalid launch.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userEmail = bundle.getString("userEmail", "")
        societyId = bundle.getString("societyId", "")
        if (userEmail.isEmpty()) {
            Toast.makeText(this, "User email did not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        screen.btnAddSociety.setOnClickListener {
            if (MyUtils.isNetworkAvailable(this)) validateAndCreate()
            else Toast.makeText(
                this,
                "No internet connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateAndCreate() {
        var flag = true
        if (societyName.isEmpty()) {
            MyUtils.setEDTError(screen.edtName, "Please enter society name.")
            flag = false
        }
        if (societyArea.isEmpty()) {
            MyUtils.setEDTError(screen.edtArea, "Please enter area.")
            flag = false
        }
        if (societyCity.isEmpty()) {
            MyUtils.setEDTError(screen.edtCity, "Please enter city.")
            flag = false
        }
        if (societyState.isEmpty()) {
            MyUtils.setEDTError(screen.edtState, "Please enter state.")
            flag = false
        }
        if (societyCountry.isEmpty()) {
            MyUtils.setEDTError(screen.edtCountry, "Please enter country")
            flag = false
        }
        if (societyPinCode.isEmpty() || societyPinCode.length < 6) {
            MyUtils.setEDTError(screen.edtPincode, "Please enter valid pincode")
            flag = false
        }


        if (flag) checkAndAddSociety()
    }

    private fun checkAndAddSociety() {
        MyUtils.showProgress(this, "Validating Society", null, false)
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .whereEqualTo(SocietyModel.SocietyEnum.sname.name, societyName)
            .whereEqualTo(SocietyModel.SocietyEnum.area.name, societyArea)
            .whereEqualTo(SocietyModel.SocietyEnum.city.name, societyCity)
            .whereEqualTo(SocietyModel.SocietyEnum.country.name, societyCountry)
            .whereEqualTo(SocietyModel.SocietyEnum.pinCode.name, societyPinCode)
            .whereEqualTo(SocietyModel.SocietyEnum.state.name, societyState)
            .limit(1).get()
            .addOnCompleteListener { task ->
                MyUtils.dismissProgress()
                if (task.isSuccessful) {
                    task.result?.let {
                        if (it.isEmpty) addSociety()
                        else {
                            Toast.makeText(
                                this,
                                "This society already registered.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    if (task.result == null) {
                        Toast.makeText(
                            this,
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    task.exception?.let {
                        Toast.makeText(
                            this,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun addSociety() {
        MyUtils.showProgress(this, "Adding Society", null, false)
        FirebaseFirestore.getInstance()
            .collection(FirebaseCollectionName.SOCIETIES.name).add(
                SocietyModel(
                    chairmanEmail = userEmail,
                    sname = societyName,
                    area = societyArea,
                    city = societyCity,
                    country = societyCountry,
                    pinCode = societyPinCode,
                    state = societyState,
                    status = SocietyStatus.ACTIVE.name
                )

            ).addOnSuccessListener {
                MyUtils.dismissProgress()
                setSocietyId(it.id)

              //  checkAndStoreChairman(it.id)
            }.addOnFailureListener {
                MyUtils.dismissProgress()
                Toast.makeText(
                    this,
                    "${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setSocietyId(societyId: String) {
        MyUtils.showProgress(this, "Finishing up", null, false)
        val map = HashMap<String, Any>()
        map[SocietyModel.SocietyEnum.societyId.name] = societyId
        map[SocietyModel.SocietyEnum.members.name] = 0
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(societyId).update(map).addOnSuccessListener {
                MyUtils.dismissProgress()
                Toast.makeText(
                    this,
                    "New Society Added Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                FireAccess.increaseSociety()
                finish()
            }.addOnFailureListener { setSocietyId(societyId) }
    }

}