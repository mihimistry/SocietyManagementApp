package com.example.societyguru.activity.member.dashboard

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.example.societyguru.R
import com.example.societyguru.activity.LoginActivity
import com.example.societyguru.activity.member.MemberNoticeInfoActivity
import com.example.societyguru.activity.member.dashboard.ui.home.MemberHomeFragment
import com.example.societyguru.activity.member.dashboard.ui.payment.MemberPaymentFragment
import com.example.societyguru.activity.member.dashboard.ui.profile.MemberProfileFragment
import com.example.societyguru.databinding.ActivityMemberDashboardBinding
import com.example.societyguru.enums.FirebaseCollectionName
import com.example.societyguru.enums.UserStatus
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.firebaseAccess.OreoNotification
import com.example.societyguru.model.NoticeModel
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.SharedPreferenceUser

class MemberDashboardActivity : AppCompatActivity(), FireAccess.PendingNoticeListener {

    private lateinit var screen: ActivityMemberDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMemberDashboardBinding.inflate(layoutInflater)
        setContentView(screen.root)

        screen.navView.selectedItemId = R.id.home

        FireAccess.getPendingNotice(this, this)
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(MyUtils.getUserId(this))
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                }
                if (value != null && value.exists()) {
                    val model = value.toObject(UserModel::class.java)
                    if (model?.status == UserStatus.BLOCKED.name) {
                        screen.navHostFragment.visibility = View.GONE
                        screen.llBlocked.visibility = View.VISIBLE
                    }
                    if (model?.status == UserStatus.ACTIVE.name) {
                        screen.navHostFragment.visibility = View.VISIBLE
                        screen.llBlocked.visibility = View.GONE
                    }
                }
            }

        if (screen.navView.selectedItemId == R.id.home) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.nav_host_fragment,
                    MemberHomeFragment()
                ).commit()
        }

        screen.navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    title = "Home"
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.nav_host_fragment,
                            MemberHomeFragment()
                        ).commit()
                    supportActionBar?.elevation = 2f
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.payment -> {
                    title = "Payment"
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.nav_host_fragment,
                            MemberPaymentFragment()
                        ).commit()
                    supportActionBar?.elevation = 2f
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.profile -> {
                    title = "Profile"
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.nav_host_fragment,
                            MemberProfileFragment()
                        ).commit()
                    supportActionBar?.elevation = 0f
                    return@setOnNavigationItemSelectedListener true
                }
                else -> return@setOnNavigationItemSelectedListener false
            }
        }
        screen.navView.setOnNavigationItemReselectedListener { }
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

    override fun onReceivePendingNotice(data: ArrayList<NoticeModel.NoticeTo>) {
        for (model in data) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val title = model.title
                val body = model.description
                val resultIntent =
                    Intent(applicationContext, MemberNoticeInfoActivity::class.java).putExtra(
                        "id",
                        model.noticeId
                    )
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val oreoNotification = OreoNotification(this)
                val builder: Notification.Builder = oreoNotification.getOreoNotification(
                    title,
                    body,
                    pendingIntent,
                    defaultSound,
                    (R.mipmap.ic_launcher_round).toString()
                )

                val i = 0
                oreoNotification.manager?.notify(i, builder.build())
                FireAccess.convertPendingNoticeToDone(model)
            } else {
                val CHANNEL_ID = "Bestmarts"
                val title = model.title
                val body = model.description
                val resultIntent =
                    Intent(applicationContext, MemberNoticeInfoActivity::class.java).putExtra(
                        "id",
                        model.noticeId
                    )
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setNumber(10)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentInfo("Info")
                notificationManager.notify(1, notificationBuilder.build())
                FireAccess.convertPendingNoticeToDone(model)
            }
        }
    }

    private fun logoutUser() {
        AlertDialog.Builder(this@MemberDashboardActivity)
            .setTitle(R.string.app_name)
            .setMessage("Are you sure you want to Logout?")
            .setPositiveButton("Yes") { _, _ ->
                FireAccess.removeUserToken(
                    SharedPreferenceUser.getInstance().getUser(applicationContext).email
                )
                SharedPreferenceUser.getInstance().logout(this)
                startActivity(Intent(this@MemberDashboardActivity, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

}