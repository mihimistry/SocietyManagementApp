package com.maxgen.societyguru.activity.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.R
import com.maxgen.societyguru.activity.LoginActivity
import com.maxgen.societyguru.databinding.ActivityAdminDashboardBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.enums.General
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.model.UserModel
import com.maxgen.societyguru.utils.MyUtils.getScreenHeightPercent
import com.maxgen.societyguru.utils.SharedPreferenceUser


class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var screen: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(screen.root)
        screen.tvSocieties.layoutParams.height = getScreenHeightPercent(this, 15f)
        screen.tvUsers.layoutParams.height = getScreenHeightPercent(this, 15f)
        screen.tvSocietiesLayout.setOnClickListener {
            startActivity(Intent(this@AdminDashboardActivity, SocietyListActivity::class.java))
        }

        screen.tvUsersLayout.setOnClickListener {
            startActivity(
                Intent(
                    this@AdminDashboardActivity,
                    UserListActivity::class.java
                )
            )
        }

        screen.tvNoticeLayout.setOnClickListener {
            startActivity(
                Intent(
                    this@AdminDashboardActivity,
                    NoticeListActivity::class.java
                )
            )
        }
        getTotalSocieties()
        getTotalUsers()
    }

    private fun getTotalSocieties() {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .addSnapshotListener { value, error ->
                if (error!=null){
                    Toast.makeText(this,error.message,Toast.LENGTH_SHORT).show()
                }
                if (value!=null&&!value.isEmpty) {
                    screen.tvSocieties.text = value.documents.size.toString()
                }
            }
    }

    private fun getTotalUsers() {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.userType.name,UserType.CHAIRMAN.name)
            .addSnapshotListener { value, error ->
                if (error!=null){
                    Toast.makeText(this,error.message,Toast.LENGTH_SHORT).show()
                }
                if (value!=null&&!value.isEmpty){
                    screen.tvUsers.text = value.documents.size.toString()

                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dash_log_out_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.log_out -> logoutUser()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logoutUser() {
        AlertDialog.Builder(this@AdminDashboardActivity)
            .setTitle(R.string.app_name)
            .setMessage("Are you sure you want to Logout?")
            .setPositiveButton("Yes") { _, _ ->
                SharedPreferenceUser.getInstance().logout(this)
                startActivity(Intent(this@AdminDashboardActivity, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

}