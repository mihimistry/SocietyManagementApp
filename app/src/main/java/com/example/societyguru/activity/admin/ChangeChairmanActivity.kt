package com.example.societyguru.activity.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.example.societyguru.R
import com.example.societyguru.adapter.OnUserOptionClick
import com.example.societyguru.adapter.UserListAdapter
import com.example.societyguru.databinding.ActivityUsersBinding
import com.example.societyguru.enums.FirebaseCollectionName
import com.example.societyguru.enums.UserStatus
import com.example.societyguru.enums.UserType
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyModel
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils

class ChangeChairmanActivity:AppCompatActivity(), OnUserOptionClick {
    private lateinit var screen: ActivityUsersBinding

    private lateinit var societyId: String
    private lateinit var adapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen= ActivityUsersBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        societyId=intent.extras!!.getString("societyId","")
        adapter = UserListAdapter(FireAccess.getSocietyUserRvAdapterOptions(societyId), this)
        screen.rvUsers.layoutManager = LinearLayoutManager(this)
        screen.rvUsers.adapter = adapter
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
                    // assignChairmanAsMember(model)
                      assignMemberAsChairman(model)
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
                        flag = false
                        Toast.makeText(this@ChangeChairmanActivity, "success", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            this@ChangeChairmanActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                Toast.makeText(
                    this@ChangeChairmanActivity,
                    "Successfully Assigned " + model.fName + " " + model.lName + " as Chairman",
                    Toast.LENGTH_SHORT
                ).show()
            }

            .addOnFailureListener {
                Toast.makeText(this@ChangeChairmanActivity, "fAIL${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
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
                Toast.makeText(this@ChangeChairmanActivity, "User Blocked.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                MyUtils.dismissProgress()
                Toast.makeText(this@ChangeChairmanActivity, "${it.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@ChangeChairmanActivity, "User Activated.", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                MyUtils.dismissProgress()
                Toast.makeText(this@ChangeChairmanActivity, "${it.message}", Toast.LENGTH_SHORT).show()
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

}