package com.ftg.carrepo.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SharedMemory
import android.provider.SyncStateContract.Helpers.insert
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ftg.carrepo.Adapters.SearchAdapter
import com.ftg.carrepo.Models.SearchedVehicleDetails
import com.ftg.carrepo.R
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentSearchVehiclePageBinding

import com.ftg.carrepo.viewModels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint

class SearchVehiclePage : Fragment() {
    @Inject
    lateinit var viewModel: SearchViewModel // Inject ViewModel
    @Inject
    lateinit var memory: SharedPrefManager
    private lateinit var bind: FragmentSearchVehiclePageBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSearchVehiclePageBinding.inflate(inflater, container, false)

        // Observe LiveData from ViewModel
        viewModel.searchResult.observe(viewLifecycleOwner) { result ->
            Log.d("Result",result.toString())
            val count = result?.size

//            if (count != null) {
//                if (count%2==1 && viewModel.isTwoColumn){
//                    bind.mainContent.visibility=View.VISIBLE
//                    bind.itemTxt.text = if (viewModel.searchByRc) result.lastOrNull()?.rc_no else result.lastOrNull()?.chassis_no
//                    bind.mainContent.setOnClickListener {
//                        result.lastOrNull()?.let { it1 -> showVehicleDetails(it1) }
//                    }
//                }else{
//                    bind.mainContent.visibility=View.GONE
//
//                }
//            }

            if (result.isNullOrEmpty() || memory.getUserDetails().status == "IN-ACTIVE") {
                bind.rcv.visibility = View.GONE
                bind.errorImage.visibility = View.VISIBLE
                bind.errorText.visibility = View.VISIBLE
                bind.errorImage.setImageResource(R.drawable.not_found)
                bind.errorText.text = "Not Found"
            } else {
                bind.rcv.layoutManager = if (viewModel.isTwoColumn)
                    GridLayoutManager(requireContext(), 2)
                else
                    LinearLayoutManager(requireContext())
                bind.errorText.visibility = View.GONE
                bind.errorImage.visibility = View.GONE
                bind.rcv.visibility = View.VISIBLE
                bind.rcv.adapter =
                    result?.let {
                        SearchAdapter(it, viewModel.searchByRc, viewModel.isTwoColumn) {
                            showVehicleDetails(it)
                        }
                    }

            }
        }


        return bind.root
    }
    private fun showVehicleDetails(it: SearchedVehicleDetails) {
        Log.d("SearchRc",it.rc_no)
        if(!checkPermissions()) {
            Toast.makeText(requireContext(), "Permissions not Granted\nPlease allow all permissions", Toast.LENGTH_SHORT).show()
        }

        val fragment = VehicleDetailsPage()
        val bundle = Bundle()
        bundle.putString("rc_no",it.rc_no)
        bundle.putString("id", it._id)
        bundle.putBoolean("searchOffline", viewModel.searchOffline)
        fragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .add(R.id.home_container, fragment)
            .addToBackStack(null).commit()
    }
    private fun checkPermissions():Boolean {
        return if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            !(ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED)
        }else{
            !(ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_DENIED)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.initialize(requireArguments(), requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.cancelRequests()
    }

    fun configChanged(
        input: String?,
        searchByRc: Boolean?,
        isTwoColumn: Boolean?,
        searchOffline: Boolean?
    ) {
        input?.let { viewModel.query = it }
        searchByRc?.let { viewModel.searchByRc = it }
        isTwoColumn?.let { viewModel.isTwoColumn = it }
        searchOffline?.let { viewModel.searchOffline = it }

        if (memory.getUserDetails().role=="ADMIN"){
            viewModel.searchAdminVehicle(requireContext())
        }else{
            viewModel.searchVehicle(requireContext())

        }
    }
}








