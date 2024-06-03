package com.ftg.carrepo.Screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ftg.carrepo.R
import com.ftg.carrepo.Utils.ConfigChangeListener
import com.ftg.carrepo.Utils.SharedPrefManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ConfigChangeListener {
    @Inject
    lateinit var memory: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ftg.carrepo.R.layout.activity_main)
        if(memory.getToken().isNullOrBlank()){
            supportFragmentManager.beginTransaction().replace(com.ftg.carrepo.R.id.container, LoginPage())
                .commit()
        }else{
            supportFragmentManager.beginTransaction().replace(com.ftg.carrepo.R.id.container, HomePage())
                .commit()
        }
    }

    override fun configChanged(
        input: String?,
        searchByRc: Boolean?,
        isTwoColumn: Boolean?,
        searchOffline: Boolean?
    ) {
        val frag1: SearchVehiclePage? = supportFragmentManager.findFragmentById(com.ftg.carrepo.R.id.home_container) as SearchVehiclePage?
        frag1?.configChanged(input, searchByRc, isTwoColumn, searchOffline)
    }
}