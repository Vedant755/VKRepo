package com.ftg.carrepo.Screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.R
import com.ftg.carrepo.Utils.Constant.IMAGE_BASE_URL
import com.ftg.carrepo.Utils.ServerCallInterceptor
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentAccountPageBinding
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AccountPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager

    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var bind: FragmentAccountPageBinding
    private var userDetails: UserDetails? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentAccountPageBinding.inflate(inflater, container, false)
        setData()
       setActionListeners()
        return bind.root
    }

    private fun setData() {
        userDetails = memory.getUserDetails()

//        Glide.with(requireContext()).load(IMAGE_BASE_URL + userDetails?.image).into(bind.userImage)

        bind.name.text = userDetails?.name
        bind.mobile.text = userDetails?.mobile.toString()
        bind.address.text = userDetails?.address

        try{
//            val parsed: ZonedDateTime = ZonedDateTime.parse(userDetails?.accessTo)
//            val z: ZonedDateTime = parsed.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
//            val fmt = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)
//            bind.expiry.text = fmt.format(z)
        }catch(e:Exception){
//            bind.expiry.text = "${userDetails?.accessTo}"
        }

    }

    private fun setActionListeners() {
        bind.updateAccountButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.home_container, KycDocPage())
                .addToBackStack(null).commit()
        }

        bind.logoutButton.setOnClickListener {
            memory.saveToken(null)
            requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finishAffinity()
        }
    }
}