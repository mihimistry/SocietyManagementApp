package com.maxgen.societyguru.activity.admin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.R
import com.maxgen.societyguru.adapter.admin.OnSocietyOptionClick
import com.maxgen.societyguru.adapter.admin.SocietyListAdapter
import com.maxgen.societyguru.databinding.ActivitySocietyListBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.enums.SocietyStatus
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.model.UserModel
import com.maxgen.societyguru.utils.MyUtils.dismissProgress
import com.maxgen.societyguru.utils.MyUtils.showProgress

class SocietyListActivity : AppCompatActivity(), OnSocietyOptionClick,
    FireAccess.OnSocietyEntryStatusChanging,
    FireAccess.OnDeleteSocietyListener
//FireAccess.OnSocietyUsersDeleted
{

    private lateinit var screen: ActivitySocietyListBinding
    private lateinit var adapter: SocietyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivitySocietyListBinding.inflate(layoutInflater)
        setContentView(screen.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        screen.btnAddSociety.setOnClickListener {
            startActivity(Intent(this@SocietyListActivity, CreateSocietyActivity::class.java))
        }

        val query =
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
        val optionTest = FirestoreRecyclerOptions.Builder<SocietyModel>().setLifecycleOwner(this)
            .setQuery(query, SocietyModel::class.java)
            .build()

        adapter = SocietyListAdapter(optionTest, this)

        screen.rvSocieties.layoutManager = LinearLayoutManager(this)
        screen.rvSocieties.adapter = adapter
        adapter.startListening()
    }

    override fun onDestroy() {
        adapter.stopListening()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun showOptions(model: SocietyModel, anchor: View) {
        val popup = PopupMenu(this@SocietyListActivity, anchor)
        popup.inflate(R.menu.admin_society_options)
        when (model.status) {
            SocietyStatus.ACTIVE.name -> popup.menu.findItem(R.id.society_unblock).isVisible = false
            SocietyStatus.BLOCKED.name -> {
                popup.menu.findItem(R.id.society_update).isVisible = false
                popup.menu.findItem(R.id.society_block).isVisible = false
            }
        }

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.society_update -> {
                    updateSociety(model)
                    return@setOnMenuItemClickListener true
                }
                R.id.society_block -> {
                    blockSociety(model)
                    return@setOnMenuItemClickListener true
                }
                R.id.society_unblock -> {
                    unblockSociety(model)
                    return@setOnMenuItemClickListener true
                }
                R.id.society_delete -> {

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Are you sure?")
                    builder.setMessage("Be careful!\nDeleting Society will Also delete Society Chairman and Society Member")
                    builder.setPositiveButton("Yes, Delete",
                        DialogInterface.OnClickListener { dialog, id ->
                            FireAccess.deleteSociety(model.societyId, this@SocietyListActivity)
                        })
                    builder.setNegativeButton("No, Cancel",
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
                    val alertDialog=builder.create()
                    alertDialog.show()

                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popup.show()
    }


    override fun showSocietyInfo(model: SocietyModel) {
        val bundle = Bundle()
        bundle.putString("id", model.societyId)
        startActivity(Intent(this, SocietyInfoActivity::class.java).putExtras(bundle))
    }

    private fun updateSociety(model: SocietyModel) {
        val bundle = Bundle()
        bundle.putString(SocietyModel.SocietyEnum.societyId.name, model.societyId)
        bundle.putString(SocietyModel.SocietyEnum.chairmanEmail.name, model.chairmanEmail)
        startActivity(
            Intent(this@SocietyListActivity, UpdateSocietyActivity::class.java)
                .putExtras(bundle)
        )
    }

    private fun blockSociety(model: SocietyModel) {
        showProgress(this, "Blocking", null, false)
        FireAccess.changeSocietyRegistrationStatus(
            model.societyId,
            SocietyStatus.BLOCKED.name,
            this
        )
    }

    private fun unblockSociety(model: SocietyModel) {
        showProgress(this, "Activating", null, false)
        FireAccess.changeSocietyRegistrationStatus(
            model.societyId,
            SocietyStatus.ACTIVE.name,
            this
        )

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_option, menu)
        val item: MenuItem = menu!!.findItem(R.id.action_search)
        val searchView: SearchView = MenuItemCompat.getActionView(item) as SearchView
        searchView.queryHint = "Enter Society Name"
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
        val query =
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
                .orderBy(UserModel.UserEnum.searchName.name)
                .startAt(search).endAt("$search\uf8ff")

        val optionTest = FirestoreRecyclerOptions.Builder<SocietyModel>().setLifecycleOwner(this)
            .setQuery(query, SocietyModel::class.java)
            .build()

        adapter = SocietyListAdapter(optionTest, this)

        screen.rvSocieties.layoutManager = LinearLayoutManager(this)
        screen.rvSocieties.adapter = adapter

        adapter.stopListening()
        adapter.startListening()
        adapter.notifyDataSetChanged()
    }

    override fun societyEntryStatusChanged(flag: Boolean, error: String?) {
        dismissProgress()
        if (!flag) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun societyDeleted(flag: Boolean, error: String?) {
        dismissProgress()
        if (!flag) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this, "Society Deleted Successfully", Toast.LENGTH_LONG).show()
    }


}

