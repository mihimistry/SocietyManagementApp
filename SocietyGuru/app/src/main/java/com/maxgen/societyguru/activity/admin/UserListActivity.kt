package com.maxgen.societyguru.activity.admin

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
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.R
import com.maxgen.societyguru.adapter.OnUserOptionClick
import com.maxgen.societyguru.adapter.UserListAdapter
import com.maxgen.societyguru.databinding.ActivityUsersBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.enums.UserStatus
import com.maxgen.societyguru.enums.UserType
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.UserModel
import com.maxgen.societyguru.utils.MyUtils.dismissProgress
import com.maxgen.societyguru.utils.MyUtils.showProgress
import com.maxgen.societyguru.utils.SharedPreferenceUser

class UserListActivity : AppCompatActivity(),
    OnUserOptionClick {

    private lateinit var screen: ActivityUsersBinding

    private lateinit var adapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        screen.rvUsers.layoutManager = LinearLayoutManager(this)

        val query = FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.CHAIRMAN)
        val optionTest = FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java).build()

        adapter = UserListAdapter(optionTest, this)
        screen.rvUsers.adapter = adapter
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

    override fun showUserInfo(model: UserModel) {
        val bundle = Bundle()
        bundle.putString("email", model.email)
        bundle.putString("id", model.societyId)
        startActivity(Intent(this, ChairmanInfoActivity::class.java).putExtras(bundle))
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
                else -> return@setOnMenuItemClickListener false
            }
        }
        popup.show()
    }

    private fun blockUser(model: UserModel) {
        showProgress(this, "Blocking User", null, false)
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.status.name] = UserStatus.BLOCKED.name
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(model.email)
            .update(map)
            .addOnSuccessListener {
                dismissProgress()
                Toast.makeText(this@UserListActivity, "User Blocked.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                dismissProgress()
                Toast.makeText(this@UserListActivity, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unblockUser(model: UserModel) {
        showProgress(this, "Activating", null, false)
        val map = HashMap<String, Any>()
        map[UserModel.UserEnum.status.name] = UserStatus.ACTIVE.name
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .document(model.email)
            .update(map)
            .addOnSuccessListener {
                dismissProgress()
                Toast.makeText(this@UserListActivity, "User Activated.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                dismissProgress()
                Toast.makeText(this@UserListActivity, "${it.message}", Toast.LENGTH_SHORT).show()
            }
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
        val search: String = newText.toString().toLowerCase()

        val query = FirebaseFirestore.getInstance().collection(FirebaseCollectionName.USERS.name)
            .whereEqualTo(UserModel.UserEnum.userType.name, UserType.CHAIRMAN)
            .orderBy(UserModel.UserEnum.searchName.name)
            .startAt(search).endAt("$search\uf8ff")

        val optionTest = FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java).build()

        adapter = UserListAdapter(optionTest, this)

        screen.rvUsers.adapter = adapter
        adapter.stopListening()
        adapter.startListening()
        adapter.notifyDataSetChanged()
    }
}