package com.example.societyguru.activity.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.example.societyguru.R
import com.example.societyguru.adapter.OnUserOptionClick
import com.example.societyguru.adapter.UserListAdapter
import com.example.societyguru.databinding.ActivitySocietyInfoBinding
import com.example.societyguru.enums.FirebaseCollectionName
import com.example.societyguru.enums.UserStatus
import com.example.societyguru.enums.UserType
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyModel
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils

class SocietyInfoActivity : AppCompatActivity(), OnUserOptionClick, FireAccess.SocietyInfoListener,
    FireAccess.SocietyChairmanListener {

    private lateinit var screen: ActivitySocietyInfoBinding
    private lateinit var societyId: String
    private lateinit var adapter: UserListAdapter
    private var alreadyExecuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivitySocietyInfoBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getSocietyData()
        ViewCompat.setNestedScrollingEnabled(screen.rvMembers, false)

    }

    private fun getSocietyData() {
        val bundle = intent.extras
        if (bundle == null) {
            Toast.makeText(this, "Invalid launch.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        societyId = bundle.getString("id", "")
        if (societyId.isEmpty()) {
            Toast.makeText(this, "Society Id did not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        getSocietyInfo()
    }

    private fun getSocietyInfo() {
        FireAccess.getSocietyInfo(societyId, this)
        FireAccess.getSocietyChairman(societyId, this)

        adapter = UserListAdapter(FireAccess.getSocietyUserRvAdapterOptions(societyId), this)
        screen.rvMembers.layoutManager = LinearLayoutManager(this)
        screen.rvMembers.adapter = adapter
    }

    override fun showOptions(model: UserModel, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.inflate(R.menu.admin_user_options)
        if (model.status == UserStatus.BLOCKED.name) {
            popup.menu.findItem(R.id.user_unblock).isVisible = true
            popup.menu.findItem(R.id.user_block).isVisible = false
        } else {

            popup.menu.findItem(R.id.user_unblock).isVisible = false
            popup.menu.findItem(R.id.user_block).isVisible = true
            popup.menu.findItem(R.id.assign_chairman).isVisible = true
        }

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.user_block -> {
                    blockUser(model)
                    return@setOnMenuItemClickListener true
                }
                R.id.user_unblock -> {
                    unblockUser(model)
                    return@setOnMenuItemClickListener true
                }
                R.id.assign_chairman -> {
                    assignChairmanAsMember(model)

                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popup.show()
    }

    private fun assignMemberAsChairman(model: UserModel) {

        var flag = true
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.userType.name] = UserType.CHAIRMAN.name
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(model.email)
            .update(map)
            .addOnSuccessListener {
                val hasMap = HashMap<String, Any>()
                hasMap[SocietyModel.SocietyEnum.chairmanEmail.name] = model.email

                FirebaseFirestore.getInstance()
                    .collection(FirebaseCollectionName.SOCIETIES.name)
                    .document(societyId)
                    .update(hasMap)
                    .addOnSuccessListener {
                        screen.chairmanData.visibility = View.VISIBLE
                        screen.tvNullChairman.visibility = View.GONE
                        Toast.makeText(
                            this@SocietyInfoActivity,
                            "Successfully Assigned " + model.fName + " " + model.lName + " as Chairman",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this@SocietyInfoActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        MyUtils.dismissProgress()

                    }


            }

            .addOnFailureListener {
                Toast.makeText(this@SocietyInfoActivity, "fAIL${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun assignChairmanAsMember(model: UserModel) {

        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.userType.name] = UserType.SOCIETY_MEMBER.name
        if (screen.tvChairmanEmail.text.isNotEmpty()) {
            FirebaseFirestore.getInstance().collectionGroup(FirebaseCollectionName.USERS.name)
                .whereEqualTo(UserModel.UserEnum.email.name, screen.tvChairmanEmail.text)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Toast.makeText(this, "Error : " + error.message, Toast.LENGTH_LONG).show()
                    }
                    if (value != null && !value.isEmpty) {
                        value.documents[0].reference.update(map)
                            .addOnSuccessListener {
                                assignMemberAsChairman(model)

                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@SocietyInfoActivity,
                                    "${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                }
        } else assignMemberAsChairman(model)

        /*
        var b = true
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.societyId.name, societyId)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.CHAIRMAN.name)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(
                        this@SocietyInfoActivity,
                        error.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (value != null && !value.isEmpty) {
                    val chairman: UserModel = value.toObjects(UserModel::class.java)[0]
                    val map = HashMap<String, Any>()
                    map[UserModel.UserEnum.userType.name] = UserType.SOCIETY_MEMBER.name

                    if (model.userType == UserType.CHAIRMAN.name) {
                        FirebaseFirestore.getInstance()
                            .collection(FirebaseCollectionName.USERS.name)
                            .document(chairman.email)
                            .update(map)

                            .addOnSuccessListener {

                                Toast.makeText(
                                    this@SocietyInfoActivity,
                                    "Successfully Assigned " + chairman.fName + " " + chairman.lName + " as Member",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@SocietyInfoActivity,
                                    "${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                    } else
                        Toast.makeText(
                            this@SocietyInfoActivity,
                            "Could not assign chairman as member",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

         */
    }

    override fun showUserInfo(model: UserModel) {
        val bundle = Bundle()
        bundle.putString("email", model.email)
        bundle.putString("id", model.societyId)
        startActivity(Intent(this, MemberInfoActivity::class.java).putExtras(bundle))
    }

    private fun blockUser(model: UserModel) {
        MyUtils.showProgress(this, "Blocking User", null, false)
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.status.name] = UserStatus.BLOCKED.name
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(model.email)
            .update(map)
            .addOnSuccessListener {
                MyUtils.dismissProgress()
                Toast.makeText(this@SocietyInfoActivity, "User Blocked.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                MyUtils.dismissProgress()
                Toast.makeText(this@SocietyInfoActivity, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unblockUser(model: UserModel) {
        MyUtils.showProgress(this, "Activating", null, false)
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.status.name] = UserStatus.ACTIVE.name
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(model.email)
            .update(map)
            .addOnSuccessListener {
                MyUtils.dismissProgress()
                Toast.makeText(this@SocietyInfoActivity, "User Activated.", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                MyUtils.dismissProgress()
                Toast.makeText(this@SocietyInfoActivity, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onStart() {
        adapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }

    override fun listenSocietyInfo(model: SocietyModel?, flag: Boolean, error: String?) {

        if (flag)
            model?.let {
                screen.societyName = it.sname
                screen.societyAddress =
                    "${it.area}, ${it.city} , ${it.state}, ${it.country} - ${it.pinCode}"
                screen.tvEmptyRows.visibility =
                    if (it.members < 2) View.VISIBLE else View.GONE
            }
        else
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun listenSocietyChairmanInfo(model: UserModel?, flag: Boolean, error: String?) {
        if (flag) {
            screen.chairman = model

            //       screen.tvChangeChairman.setOnClickListener {
            //            if (model != null) {
            //               assignChairmanAsMember(model)
            //          }
            //           var b=Bundle()
            //         b.putString("societyId",model?.societyId)
            //         startActivity(Intent(this,ChangeChairmanActivity::class.java).putExtras(b))
            //          finish() }

        } else {
            screen.chairmanData.visibility = View.GONE
            screen.tvNullChairman.visibility = View.VISIBLE
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

}