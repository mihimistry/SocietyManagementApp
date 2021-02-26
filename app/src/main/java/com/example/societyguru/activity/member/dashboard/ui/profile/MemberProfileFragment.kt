package com.example.societyguru.activity.member.dashboard.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.societyguru.activity.chairman.EditProfileActivity
import com.example.societyguru.databinding.FragmentMemberProfileBinding
import com.example.societyguru.firebaseAccess.FireAccess
import com.example.societyguru.model.SocietyModel
import com.example.societyguru.utils.MyUtils
import com.example.societyguru.utils.SharedPreferenceUser

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