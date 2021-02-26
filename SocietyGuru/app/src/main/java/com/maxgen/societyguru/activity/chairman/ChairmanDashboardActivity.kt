package com.maxgen.societyguru.activity.chairman

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.R
import com.maxgen.societyguru.activity.LoginActivity
import com.maxgen.societyguru.activity.member.MemberNoticeInfoActivity
import com.maxgen.societyguru.databinding.ActivityChairmanDashboardBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.enums.SocietyStatus
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.firebaseAccess.OreoNotification
import com.maxgen.societyguru.model.NoticeModel
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.model.UserModel
import com.maxgen.societyguru.utils.MyUtils
import com.maxgen.societyguru.utils.SharedPreferenceUser

class ChairmanDashboardActivity : AppCompatActivity(), FireAccess.SocietyInfoListener,
    FireAccess.PendingNoticeListener, FireAccess.OnSocietyEntryStatusChanging {

    private lateinit var screen: ActivityChairmanDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityChairmanDashboardBinding.inflate(layoutInflater)
        setContentView(screen.root)

        screen.tvMembersLayout.setOnClickListener {
            startActivity(
                Intent(
                    this@ChairmanDashboardActivity,
                    SocietyMemberListActivity::class.java
                )
            )
        }

        screen.tvProfileLayout.setOnClickListener {
            startActivity(
                Intent(
                    this@ChairmanDashboardActivity,
                    ChairmanProfileActivity::class.java
                )
            )
        }
        screen.tvMaintenanceLayout.setOnClickListener {
            startActivity(Intent(this, MaintenanceListActivity::class.java))
        }
        screen.tvNoticeLayout.setOnClickListener {
            startActivity(
                Intent(
                    this@ChairmanDashboardActivity,
                    ChairmanNoticeActivity::class.java
                )
            )
        }

        screen.tvEventLayout.setOnClickListener {
            startActivity(
                Intent(
                    this@ChairmanDashboardActivity,
                    EventListActivity::class.java
                )
            )
        }

        screen.tvMemberMaintenanceLayout.setOnClickListener {
            startActivity(Intent(this, MaintenanceSentListActivity::class.java))

        }
        screen.tbSocietyEntryStatus.setOnCheckedChangeListener { _, b ->
            FireAccess.changeSocietyRegistrationStatus(
                MyUtils.getUserSocietyId(this),
                if (b) SocietyStatus.ACTIVE.name else SocietyStatus.BLOCKED.name,
                this
            )
        }

        FireAccess.getSocietyInfo(MyUtils.getUserSocietyId(this), this)
        FireAccess.getPendingNotice(this, this)

        getTotalUsers()
    }

    private fun getTotalUsers() {
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.SOCIETY_MEMBER.name)
            .whereEqualTo(UserModel.UserEnum.societyId.name,MyUtils.getUserSocietyId(this))
            .get().addOnSuccessListener {
                screen.tvMembers.text = it.count().toString()
            }
    }

    override fun listenSocietyInfo(model: SocietyModel?, flag: Boolean, error: String?) =
        if (flag && model != null) {
         //   screen.tvMembers.text = (model.members - 1).toString()
            screen.tbSocietyEntryStatus.isChecked = model.status == SocietyStatus.ACTIVE.name
        } else Toast.makeText(this, error, Toast.LENGTH_SHORT).show()

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
        AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage("Are you sure you want to Logout?")
            .setPositiveButton("Yes") { _, _ ->
                FireAccess.removeUserToken(
                    SharedPreferenceUser.getInstance().getUser(this@ChairmanDashboardActivity).email
                )
                SharedPreferenceUser.getInstance().logout(this)
                startActivity(Intent(this@ChairmanDashboardActivity, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
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

    override fun societyEntryStatusChanged(flag: Boolean, error: String?) {
        if (!flag) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

}