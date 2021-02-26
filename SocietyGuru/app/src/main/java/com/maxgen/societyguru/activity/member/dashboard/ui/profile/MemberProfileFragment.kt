package com.maxgen.societyguru.activity.member.dashboard.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.maxgen.societyguru.activity.chairman.EditProfileActivity
import com.maxgen.societyguru.databinding.FragmentMemberProfileBinding
import com.maxgen.societyguru.firebaseAccess.FireAccess
import com.maxgen.societyguru.model.SocietyModel
import com.maxgen.societyguru.utils.MyUtils
import com.maxgen.societyguru.utils.SharedPreferenceUser

class MemberProfileFragment : Fragment(), FireAccess.SocietyInfoListener {

    private lateinit var screen: FragmentMemberProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        screen = FragmentMemberProfileBinding.inflate(inflater, container, false)

        val user = SharedPreferenceUser.getInstance().getUser(activity)
        screen.user = user
        screen.imgEdit.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }
        FireAccess.getSocietyInfo(user.societyId, this)
        return screen.root
    }

    override fun listenSocietyInfo(model: SocietyModel?, flag: Boolean, error: String?) {
        if (flag)
            model?.let { societyModel ->
                screen.societyName = societyModel.sname
                context?.let {
                    screen.societyAddress = MyUtils.getUserAddress(it, societyModel)
                }
            }
        else Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

}