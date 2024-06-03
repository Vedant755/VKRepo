package com.ftg.carrepo.Screens

import android.os.Bundle
import android.os.SharedMemory
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.ftg.carrepo.Models.GeneralResponse
import com.ftg.carrepo.Models.PlanAllRequest
import com.ftg.carrepo.Models.PlanAllResponse
import com.ftg.carrepo.Models.PlanDetails
import com.ftg.carrepo.Models.PlanDetailsResp
import com.ftg.carrepo.Models.UserData
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.Models.UserDetailsResponse
import com.ftg.carrepo.Models.UserStatusData
import com.ftg.carrepo.Models.UserTypeData
import com.ftg.carrepo.Utils.Constant
import com.ftg.carrepo.Utils.Constant.IMAGE_BASE_URL
import com.ftg.carrepo.Utils.LoadingDialog
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.ServerCallInterceptor
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentUserPlanChangePageBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class UserPlanChangePage : Fragment() {
    @Inject
    lateinit var interceptor: ServerCallInterceptor
    @Inject
    lateinit var memory: SharedPrefManager
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var bind: FragmentUserPlanChangePageBinding
    private lateinit var userDetails: UserDetails
    private lateinit var startDate: String
    private lateinit var endDate: String
    private var chosenPlanDays = 0
    private lateinit var server: Server
    private lateinit var status: String
    private lateinit var role:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentUserPlanChangePageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Constant.BASE_URL)
            .build()
            .create(Server::class.java)

        setUserDetails()
        setActionListeners()
        getPlan(userDetails.id!!)
        return bind.root
    }

    private fun setUserDetails() {
        userDetails = UserDetails(
            arguments?.getString("id", null),
            arguments?.getString("name", null),
            arguments?.getString("mobile", null),
            arguments?.getString("address", null),
            arguments?.getString("role", "USER"),
            arguments?.getString("status", null),
            arguments?.getString("accessTo", null),
            null,
            0
        )

        if(userDetails.role=="ADMIN"){
            bind.adminSwitch.isChecked = true

        }

        if(userDetails.status=="ACTIVE"){
            bind.statusSwitch.isChecked = true
        }

        bind.name.text = userDetails.name
        bind.mobile.text = userDetails.mobile.toString()
        bind.address.text = userDetails.address
    }

    private fun setActionListeners(){
        bind.oneMonth.setOnClickListener {
            chosenPlanDays = 30
            bind.updateButton.text = "+$chosenPlanDays Days"
        }

        bind.expire.setOnClickListener {
            chosenPlanDays = 0
            bind.updateButton.text = "Expire"
        }

        bind.custom.addTextChangedListener {
            if(it.toString().isBlank()){
                chosenPlanDays = 0
                bind.updateButton.text = "Choose Plan"
            }else{
                chosenPlanDays = it.toString().toInt()
                bind.updateButton.text = "+$chosenPlanDays"
            }
        }

        bind.updateButton.setOnClickListener {
            updatePlan()
        }

        bind.adminSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                switchUserType("ADMIN")
            else
                switchUserType("USER")
        }

        bind.statusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                switchUserStatus("ACTIVE")
            else
                switchUserStatus("IN-ACTIVE")
        }
    }
    private fun getPlan(userid: String){
        val response = server.planAll(PlanAllRequest(userid))
        response.enqueue(object : Callback<PlanAllResponse> {
            override fun onResponse(
                call: Call<PlanAllResponse>,
                response: Response<PlanAllResponse>
            ) {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
                val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyy", Locale.ENGLISH)
                if (response.isSuccessful) {
                    val userDataList = response.body()?.data
                    if (!userDataList.isNullOrEmpty()) {
                        val firstUserData = userDataList[0]
                        try{
                            val access_from = LocalDate.parse(firstUserData.startDate, inputFormatter)
                            startDate = firstUserData.startDate
                            bind.accessFrom.text = outputFormatter.format(access_from)
                        }catch(e: Exception){
                            bind.accessFrom.text = firstUserData.startDate
                        }
                        try{
                            val access_from = LocalDate.parse(firstUserData.endDate, inputFormatter)
                            endDate = firstUserData.endDate
                            bind.accessTo.text = outputFormatter.format(access_from)
                        }catch(e: Exception){
                            bind.accessTo.text = firstUserData.endDate
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrBlank()) {
                        Log.d("VK Enterprises", "Error Body: $errorBody")
                        val error = Gson().fromJson(errorBody, GeneralResponse::class.java)
                        Toast.makeText(
                            requireContext(),
                            error.message ?: "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.d("VK Enterprises", "Error Body is null or empty")
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong. Error body is null or empty.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<PlanAllResponse>, t: Throwable) {

            }
        })
    }
    private fun switchUserStatus(status: String) {
        if(bind.adminSwitch.isChecked){
            role = "ADMIN"
        }else{
            role = "USER"
        }
        val response = server.changeUserStatus(UserStatusData(userDetails.id!!, status,role))

        response.enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                // Log the response body in detail
                Log.d("VK Enterprises", "Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
//                        Log.d("VK Enterprises", "Response Body: ${Gson().toJson(responseBody)}")
                        Toast.makeText(
                            requireContext(),
                            responseBody.message ?: "Status changed successfully",

                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        Log.d("VK Enterprises", "Response Body is null")
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong. Response body is null.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrBlank()) {
                        Log.d("VK Enterprises", "Error Body: $errorBody")
                        val error = Gson().fromJson(errorBody, GeneralResponse::class.java)
                        Toast.makeText(
                            requireContext(),
                            error.message ?: "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.d("VK Enterprises", "Error Body is null or empty")
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong. Error body is null or empty.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException || call.isCanceled) {
                    switchUserStatus(status)
                } else {
                    Log.d("VK Enterprises", t.localizedMessage)
                }
            }
        })
    }


    private fun switchUserType(type: String){
        if(bind.statusSwitch.isChecked){
            status = "ACTIVE"
        }else{
            status = "IN-ACTIVE"
        }
        val response = server.changeUserType(UserTypeData(userDetails.id!!, status,type))
        response.enqueue(object: Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
//                Toast.makeText(requireContext(), "Code: ${response.code()}", Toast.LENGTH_SHORT).show()
                if(response.code()==200){

                    Toast.makeText(requireContext(), response.body()?.message ?: "Access changed successfully", Toast.LENGTH_SHORT).show()
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    switchUserType(type)
                } else if (call.isCanceled) {
                    switchUserType(type)
                } else {
                    Log.d("VK Enterprises", t.localizedMessage)
                }
            }

        })
    }


    private fun updatePlan() {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val startDateObj: Date = dateFormat.parse(endDate)!!

            val calendar = Calendar.getInstance()
            calendar.time = startDateObj
            calendar.add(Calendar.DAY_OF_YEAR, chosenPlanDays)
            loadingDialog.startLoading()
            val response = server.changeUserPlan(PlanDetails(userDetails.id, startDate,dateFormat.format(calendar.time)))
            response.enqueue(object : Callback<PlanDetailsResp>{
                override fun onResponse(
                    call: Call<PlanDetailsResp>,
                    response: Response<PlanDetailsResp>
                ) {
                    loadingDialog.stopLoading()
                    if(response.code()==200){
                        Toast.makeText(requireContext(), "Plan changes successfully", Toast.LENGTH_SHORT).show()
                        getPlan(userDetails.id!!)
                    }else{
                        Log.d("Date",dateFormat.format(calendar.time))
                        Log.d("StartDate",startDate)
                        Log.d("PlanReg",response.code().toString())
                        val responseBody = response.errorBody()?.string()
                        Log.d("Response", responseBody ?: "Response body is null or empty")
                        Toast.makeText(requireContext(), "A plan needs to be assigned to be updated", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PlanDetailsResp>, t: Throwable) {
                    if (t is SocketTimeoutException) {
                        updatePlan()
                    } else if (call.isCanceled) {
                        updatePlan()
                    } else {
                        loadingDialog.stopLoading()
                        Log.d("VK Enterprises", t.localizedMessage)
                    }
                }
            })
        }catch (e:Exception){
            Toast.makeText(requireContext(), "A plan needs to be assigned to be updated", Toast.LENGTH_SHORT).show()

        }

    }
}