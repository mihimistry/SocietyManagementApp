package com.example.societyguru.activity.chairman

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.societyguru.R
import com.example.societyguru.adapter.OnUserOptionClick
import com.example.societyguru.adapter.UserListAdapter
import com.example.societyguru.databinding.ActivitySocietyMemberListBinding
import com.example.societyguru.enums.UserStatus
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.UserModel
import com.example.societyguru.utils.MyUtils.dismissProgress
import com.example.societyguru.utils.MyUtils.showProgress
import com.example.societyguru.utils.SharedPreferenceUser

class SocietyMemberListActivity : AppCompatActivity(), OnUserOptionClick {

    private lateinit var screen: ActivitySocietyMemberListBinding
    private lateinit var adapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivitySocietyMemberListBinding.inflate(layoutInflater)
        setContentView(screen.root)
        val user: UserModel = SharedPreferenceUser.getInstance().getUser(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        adapter = UserListAdapter(
            FireAccess.getSocietyUserRvAdapterOptions(societyId = user.societyId),
            this
        )
        screen.rvUsers.layoutManager = LinearLayoutManager(this)
        screen.rvUsers.adapter = adapter
    }

    override fun showUserInfo(model: UserModel) {
        val bundle = Bundle()
        bundle.putString("email", model.email)
        bundle.putString("id", model.societyId)
        startActivity(Intent(this, ChairmanMemberInfoActivity::class.java).putExtras(bundle))
    }

    override fun showOptions(model: UserModel, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.inflate(R.menu.chairman_user_options)
        if (model.status == UserStatus.ACTIVE.name)
            popup.menu.findItem(R.id.user_activate).isVisible = false
        else if (model.status == UserStatus.BLOCKED.name) popup.menu.findItem(R.id.user_block).isVisible =
            false
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.user_block -> {
                    showProgress(this@SocietyMemberListActivity, "Blocking User", null, false)
                    FireAccess.blockUser(model, object : FireAccess.CheckListener {
                        override fun listen(flag: Boolean, error: String?) {
                            dismissProgress()
                            Toast.makeText(
                                this@SocietyMemberListActivity, "${if (flag) {
                                    "User Blocked."
                                } else {
                                    error
                                }}", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                    return@setOnMenuItemClickListener true
                }
                R.id.user_activate -> {
                    showProgress(this@SocietyMemberListActivity, "Activating User", null, false)
                    FireAccess.activateUser(model, object : FireAccess.CheckListener {
                        override fun listen(flag: Boolean, error: String?) {
                            dismissProgress()
                            Toast.makeText(
                                this@SocietyMemberListActivity, "${if (flag) {
                                    "User Activated."
                                } else {
                                    error
                                }}", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popup.show()
    }

    override fun onStart() {
        adapter.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_option, menu)
        val item: MenuItem = menu!!.findItem(R.id.action_search)
        val searchView: SearchView = MenuItemCompat.getActionView(item) as SearchView
        searchView.queryHint = "Enter Name"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchData(newText)
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun searchData(newText: String?) {
        val user: UserModel = SharedPreferenceUser.getInstance().getUser(this)
        adapter = UserListAdapter(
            FireAccess.getSearchedUserRvAdapterOptions(
                societyId = user.societyId,
                search = newText.toString().toLowerCase()
            ),
            this
        )
        screen.rvUsers.adapter = adapter
        adapter.stopListening()
        adapter.startListening()
        adapter.notifyDataSetChanged()
    }
}