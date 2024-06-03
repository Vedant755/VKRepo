package com.ftg.carrepo.Screens

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.contains
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.ftg.carrepo.R
import com.ftg.carrepo.Utils.ConfigChangeListener
import com.ftg.carrepo.Utils.ServerCallInterceptor
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentHomePageBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomePage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager

    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var bind: FragmentHomePageBinding

    private var inputChanged: ConfigChangeListener? = null
    private var isTwoColumn: Boolean = false
    private var searchOffline: Boolean = false
    private var searchByRC: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHomePageBinding.inflate(inflater, container, false)

        setActionListeners()

        return bind.root
    }

    override fun onStart() {
        super.onStart()

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (bind.settingView.isVisible) {
                        bind.settingView.visibility = View.GONE
                        bind.homeContainer.visibility = View.VISIBLE
                    } else {
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        )
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

    private fun setActionListeners() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.home_container, DashboardPage()).commit()

        bind.download.setOnClickListener {
            if (requireActivity().supportFragmentManager.findFragmentById(R.id.home_container) !is DownloadPage) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.home_container, DownloadPage())
                    .addToBackStack(null).commit()
            }
        }
        if(requireActivity().supportFragmentManager.findFragmentById(R.id.home_container) !is DashboardPage){
            bind.backButton.visibility = View.VISIBLE
        }
        bind.setting.setOnClickListener {
            if (bind.settingView.visibility != View.VISIBLE) {
                bind.homeContainer.visibility = View.GONE
                bind.settingView.visibility = View.VISIBLE
            } else {
                bind.settingView.visibility = View.GONE
                bind.homeContainer.visibility = View.VISIBLE
            }
        }

        bind.searchChassisNumberCB.setOnCheckedChangeListener { buttonView, isChecked ->
            searchByRC = !isChecked
            if (searchByRC) {
                bind.search.inputType = InputType.TYPE_CLASS_NUMBER
                bind.search.hint = "1234"
            } else {
                bind.search.inputType = InputType.TYPE_CLASS_TEXT
                bind.search.hint = "VIN 5D"
            }
        }

        bind.twoColumnCB.setOnCheckedChangeListener { buttonView, isChecked ->
            isTwoColumn = isChecked

            if (requireActivity().supportFragmentManager.findFragmentById(R.id.home_container) is SearchVehiclePage)
                inputChanged?.configChanged(null, null, isTwoColumn, null)
        }


        bind.offlineSearchCB.setOnCheckedChangeListener { buttonView, isChecked ->
            searchOffline = isChecked

            if (requireActivity().supportFragmentManager.findFragmentById(R.id.home_container) is SearchVehiclePage)
                inputChanged?.configChanged(null, null, null, searchOffline)
        }


        bind.backButton.setOnClickListener {
            if (bind.settingView.visibility == View.VISIBLE) {
                bind.settingView.visibility = View.GONE
                bind.homeContainer.visibility = View.VISIBLE
            } else {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        bind.changeType.setOnClickListener {
            searchByRC = !searchByRC
            if (searchByRC) {
                bind.search.inputType = InputType.TYPE_CLASS_NUMBER
                bind.search.hint = "1234"
            } else {
                bind.search.inputType = InputType.TYPE_CLASS_TEXT
                bind.search.hint = "VIN 5D"
            }
//            inputChanged?.configChanged(null, searchByRC, null, null)
        }

        bind.search.addTextChangedListener {
            val input = it.toString()
            if (input.isNotEmpty()) {
                if (searchByRC) {
                    if (input.length < 4)
                        return@addTextChangedListener
                } else {
                    if (input.length < 5 )
                        return@addTextChangedListener
                }


                if (bind.settingView.visibility == View.VISIBLE) {
                    bind.settingView.visibility = View.GONE
                    bind.homeContainer.visibility = View.VISIBLE
                }

                if (requireActivity().supportFragmentManager.findFragmentById(R.id.home_container) is VehicleDetailsPage) {
                    val fragment = SearchVehiclePage()
                    val bundle = Bundle()
                    bundle.putString("query", input)
                    bundle.putBoolean("isRc", searchByRC)
                    bundle.putBoolean("isTwoColumn", isTwoColumn)
                    bundle.putBoolean("searchOffline", searchOffline)
                    fragment.arguments = bundle

                    requireActivity().supportFragmentManager.popBackStack()
                    requireActivity().supportFragmentManager.popBackStack()

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.home_container, fragment)
                        .addToBackStack(null).commit()
                } else if (requireActivity().supportFragmentManager.findFragmentById(R.id.home_container) is SearchVehiclePage) {
                    inputChanged?.configChanged(input, searchByRC, isTwoColumn, searchOffline)
                } else {
                    val fragment = SearchVehiclePage()
                    val bundle = Bundle()
                    bundle.putString("query", input)
                    bundle.putBoolean("isRc", searchByRC)
                    bundle.putBoolean("isTwoColumn", isTwoColumn)
                    bundle.putBoolean("searchOffline", searchOffline)
                    fragment.arguments = bundle
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.home_container, fragment)
                        .addToBackStack(null).commit()
                }
                bind.search.setText("")
            } else {
//                requireActivity().supportFragmentManager.beginTransaction()
//                    .replace(R.id.home_container, DashboardPage()).commit()
            }
        }
    }
}

/*
val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .baseUrl(BASE_URL)
    .build()
    .create(Server::class.java)
 */