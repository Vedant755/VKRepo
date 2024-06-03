package com.ftg.carrepo.Screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ftg.carrepo.Utils.ConfigChangeListener
import com.ftg.carrepo.databinding.FragmentSettingPageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingPage : Fragment() {

    private lateinit var bind: FragmentSettingPageBinding
    private var searchByRc: Boolean = true
    private var isTwoColumn: Boolean = false
    private var offlineSearch: Boolean = false
    private var inputChanged: ConfigChangeListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSettingPageBinding.inflate(inflater, container, false)
        setActionListeners()
        return bind.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            inputChanged = activity as ConfigChangeListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                "$activity must implement TextClicked"
            )
        }
    }

    override fun onDetach() {
        inputChanged = null
        super.onDetach()
    }


    private fun setActionListeners(){
        searchByRc = arguments?.getBoolean("isRc", true) ?: true
        isTwoColumn = arguments?.getBoolean("isTwoColumn", false) ?: false
        offlineSearch = arguments?.getBoolean("searchOffline", false) ?: false

        bind.searchChassisNumberCB.isChecked = !searchByRc
        bind.twoColumnCB.isChecked = isTwoColumn
        bind.offlineSearchCB.isChecked = offlineSearch

        bind.searchChassisNumberCB.setOnCheckedChangeListener { buttonView, isChecked ->
            searchByRc = !isChecked
        }
        bind.twoColumnCB.setOnCheckedChangeListener { buttonView, isChecked ->
            isTwoColumn = isChecked
        }
        bind.offlineSearchCB.setOnCheckedChangeListener { buttonView, isChecked ->
            offlineSearch = isChecked
        }
    }
}