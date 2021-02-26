package com.maxgen.societyguru.activity.chairman

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.maxgen.societyguru.adapter.admin.OnSocietyOptionClick
import com.maxgen.societyguru.adapter.admin.SocietyListAdapter
import com.maxgen.societyguru.databinding.ActivityChairmanSocietyListBinding
import com.maxgen.societyguru.enums.FirebaseCollectionName
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.utils.MyUtils

class ChairmanSocietyListActivity : AppCompatActivity(), OnSocietyOptionClick {
    private lateinit var screen: ActivityChairmanSocietyListBinding
    private lateinit var societyListAdapter: SocietyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityChairmanSocietyListBinding.inflate(layoutInflater)
        setContentView(screen.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val query =
            FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
                .whereEqualTo("chairmanEmail", MyUtils.getUserId(this))
        val optionTest = FirestoreRecyclerOptions.Builder<SocietyModel>().setLifecycleOwner(this)
            .setQuery(query, SocietyModel::class.java)
            .build()

        societyListAdapter = SocietyListAdapter(optionTest, this)

        screen.rvSocieties.layoutManager = LinearLayoutManager(this)
        screen.rvSocieties.adapter = societyListAdapter
        societyListAdapter.startListening()
    }

    override fun onDestroy() {
        societyListAdapter.stopListening()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun showOptions(model: SocietyModel, anchor: View) {
        Toast.makeText(this, "Society Info", Toast.LENGTH_SHORT).show()
    }

    override fun showSocietyInfo(model: SocietyModel) {
        Toast.makeText(this, "Society Info", Toast.LENGTH_SHORT).show()
    }
/*
    override fun showOptions(model: SocietyModel, anchor: View) {
        val popup = PopupMenu(this, anchor)
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
        startActivity(
            Intent(this, UpdateSocietyActivity::class.java)
                .putExtras(bundle)
        )
    }

    private fun blockSociety(model: SocietyModel) {
        MyUtils.showProgress(this, "Blocking", null, false)
        val map = HashMap<String, Any>()
        map[SocietyModel.SocietyEnum.status.name] = SocietyStatus.BLOCKED
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(model.societyId)
            .update(map).addOnSuccessListener {
                MyUtils.dismissProgress()
                Toast.makeText(this, "Society Blocked.", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                MyUtils.dismissProgress()
                Toast.makeText(
                    this,
                    "Could not block. ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun unblockSociety(model: SocietyModel) {
        MyUtils.showProgress(this, "Activating", null, false)
        val map = HashMap<String, Any>()
        map[SocietyModel.SocietyEnum.status.name] = SocietyStatus.ACTIVE
        FirebaseFirestore.getInstance().collection(FirebaseCollectionName.SOCIETIES.name)
            .document(model.societyId)
            .update(map).addOnSuccessListener {
                MyUtils.dismissProgress()
                Toast.makeText(this, "Society Unblocked.", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                MyUtils.dismissProgress()
                Toast.makeText(
                    this,
                    "Could not block. ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

 */

}

