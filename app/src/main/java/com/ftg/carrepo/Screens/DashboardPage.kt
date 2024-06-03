package com.ftg.carrepo.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.ftg.carrepo.Models.LoginResponse
import com.ftg.carrepo.Models.UpdateProfileData
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.Models.UserDetailsResponse
import com.ftg.carrepo.Models.UserPlanResponse
import com.ftg.carrepo.R
import com.ftg.carrepo.RoomDB.MyDatabase
import com.ftg.carrepo.Utils.Constant
import com.ftg.carrepo.Utils.Constant.BASE_URL
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.ServerCallInterceptor
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentDashboardPageBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class DashboardPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    @Inject
    lateinit var server: Server
    @Inject
    lateinit var interceptor: ServerCallInterceptor
    private lateinit var status: String
    private lateinit var role: String
    private lateinit var bind: FragmentDashboardPageBinding
    private var userDetails: UserDetails? = null
    private lateinit var database: MyDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentDashboardPageBinding.inflate(inflater, container, false)
        database = MyDatabase.getDatabase(requireContext())
        getUserStatus()

        setUserData()

        if (isInternetAvailable())
        //getUserData()

            setActionListeners()
        return bind.root
    }

    private fun setActionListeners() {
        if (!checkPermissions()) {
            askPermissions()
        }



        // Subscription
        bind.b3.setOnClickListener {
            navigateToFragment(SubscriptionPage())
        }

        // My Account
        bind.b4.setOnClickListener {
            navigateToFragment(AccountPage())
        }

        // Admin
        bind.admin.setOnClickListener {
            if (userDetails?.role == "ADMIN") {
                navigateToFragment(AllUsersPage())
            } else {
                showToast("You are not an Admin")
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            .replace(R.id.home_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("SetTextI18n")
    private fun setUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            //  bind.b2.text = "Offline Records\n${database.getDao().getOfflineRecordCount()}"
            val fmt = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl(Constant.BASE_URL)
                .build()
                .create(Server::class.java)

            val response = retrofit.getUserPlan()
            response.enqueue(object : retrofit2.Callback<UserPlanResponse> {


                override fun onResponse(
                    call: Call<UserPlanResponse>,
                    response: Response<UserPlanResponse>
                ) {
                    Log.d("responseExp", response.code().toString())
                    if (response.isSuccessful) {

                        try {


                            val parseded: ZonedDateTime =
                                ZonedDateTime.parse(response.body()!!.data.endDate)
                            val zed: ZonedDateTime =
                                parseded.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                            if(memory.getUserDetails().role!="ADMIN")
                                bind.b1.text = "Plan Expiry\n${fmt.format(zed)}"
                        } catch (e: Exception) {
                            bind.b1.text = "Plan Expiry\n${response.body()!!.data.endDate}"
                        }

                    }
                }

                override fun onFailure(call: Call<UserPlanResponse>, t: Throwable) {
                    Log.e("Error", t.message.toString())
                }

            })
        }

        userDetails = memory.getUserDetails()

    }
    private fun getUserStatus(){

        val response = server.getUserDetails()
        response.enqueue(object: Callback<UserDetailsResponse>{
            override fun onResponse(
                call: Call<UserDetailsResponse>,
                response: Response<UserDetailsResponse>
            ) {
                if(response.code()==200){
                    if (response.body()!!.data.role == "ADMIN") {
                        bind.adminLL.visibility = View.VISIBLE
                        bind.admin.setOnClickListener {
                                navigateToFragment(AllUsersPage())
                        }
                    } else {
                        bind.adminLL.visibility = View.GONE
                    }
                    role = response.body()!!.data.role.toString()
                    status = response.body()?.data!!.status.toString()
                }
            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getUserStatus()
                } else if (call.isCanceled) {
                    getUserStatus()
                } else {
                    Log.d("VK Enterprises", t.localizedMessage)
                }
            }

        })
    }
    private fun getUserData() {
//        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
//        val retrofit = Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .baseUrl(BASE_URL)
//            .build()
//            .create(Server::class.java)
//        val response = retrofit.getUserDetails(UpdateProfileData(null, userDetails?.id, null, null))
//        response.enqueue(object : Callback<LoginResponse> {
//            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    if (responseBody != null) {
//                        // Log the entire response body
//                        Log.d("VK Enterprises", "Response Body: ${Gson().toJson(responseBody)}")
//
//                        // Save user details and update UI
//                        memory.saveUserDetails(responseBody.data)
//                        setUserData()
//                    } else {
//                        Log.e("VK Enterprises", "Response body is null")
//                    }
//                } else {
//                    try {
//                        val errorBody = response.errorBody()?.string()
//                        Log.e("VK Enterprises", "Error Body: $errorBody")
//
//                        val error = Gson().fromJson(errorBody, LoginResponse::class.java)
//                        Toast.makeText(
//                            requireContext(),
//                             "Something went wrong",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } catch (e: Exception) {
//                        Log.e("VK Enterprises", "Error parsing error body", e)
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//                if (t is SocketTimeoutException) {
//                    getUserData()
//                } else if (call.isCanceled) {
//                    getUserData()
//                } else {
//                    Log.d("VK Enterprises", t.localizedMessage)
//                }
//            }
//
//        })
    }

    private fun isInternetAvailable(): Boolean {
        (requireActivity().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            return this.getNetworkCapabilities(this.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
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
        } else {
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

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                101
            )
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_MEDIA_IMAGES
                ),
                101
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED
                || grantResults[1] == PackageManager.PERMISSION_DENIED
                || grantResults[2] == PackageManager.PERMISSION_DENIED
            ) {
                Toast.makeText(
                    requireContext(),
                    "All permission required\nGive permissions from device settings",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}