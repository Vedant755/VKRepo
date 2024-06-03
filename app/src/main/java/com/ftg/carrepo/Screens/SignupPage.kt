package com.ftg.carrepo.Screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ftg.carrepo.Models.GeneralResponse
import com.ftg.carrepo.Models.RegistrationRequest
import com.ftg.carrepo.Models.SendOtpData
import com.ftg.carrepo.Models.SendOtpResponse
import com.ftg.carrepo.R
import com.ftg.carrepo.Utils.Constant
import com.ftg.carrepo.Utils.LoadingDialog
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentSignupPageBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import javax.inject.Inject

@AndroidEntryPoint
class SignupPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    private lateinit var bind: FragmentSignupPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private var number: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSignupPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        setActionListeners()

        return bind.root
    }

    private fun setActionListeners(){
        bind.sendOtpButton.setOnClickListener {
            closeKeyboard()
            number = bind.mobileNumber.text.toString()
            if (number!!.isBlank() || number!!.length < 10)
                Toast.makeText(requireContext(), "Enter Phone Number!", Toast.LENGTH_SHORT).show()
            else {
                loadingDialog.startLoading()
                sendOtp(number!!)
            }
        }

        bind.verifyOtpButton.setOnClickListener {
            closeKeyboard()
            val role = "USER"
            val name = bind.name.text.toString()
            val address = bind.address.text.toString()
            val mobile = bind.mobileNumber.text.toString()
            val otp = bind.otp.text.toString()
            if (role.isBlank()) Toast.makeText(requireContext(), "Enter OTP!", Toast.LENGTH_SHORT).show()
            else if (name.isBlank()) Toast.makeText(requireContext(), "Fill Name!", Toast.LENGTH_SHORT).show()
            else if (address.isBlank()) Toast.makeText(requireContext(), "Fill Address!", Toast.LENGTH_SHORT).show()
            else if (mobile.isBlank() ) Toast.makeText(requireContext(),"Enter Mobile Number!",Toast.LENGTH_SHORT).show()
            else if (otp.isBlank()) Toast.makeText(requireContext(),"Enter OTP!",Toast.LENGTH_SHORT).show()
            else if (mobile.length<10) Toast.makeText(requireContext(),"Enter Correct Mobile Number!",Toast.LENGTH_SHORT).show()
            else {
                loadingDialog.startLoading()
                registration(role, name, address,mobile,otp)
            }
        }
        bind.resendOtpButton.setOnClickListener {
            closeKeyboard()
            number = bind.mobileNumber.text.toString()
            if (number!!.isBlank() || number!!.length < 10)
                Toast.makeText(requireContext(), "Enter Phone Number!", Toast.LENGTH_SHORT).show()
            else {
                loadingDialog.startLoading()
                sendOtp(number!!)
            }
        }


        bind.back.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    private fun sendOtp(number: String){
        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Server::class.java)
        val response = retrofit.sendOtpRegistration(SendOtpData(number))
        response.enqueue(object : Callback<SendOtpResponse>{
            override fun onResponse(
                call: Call<SendOtpResponse>,
                response: Response<SendOtpResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==200){
                    bind.sendOtpButton.visibility = View.GONE
                    bind.otp.visibility = View.VISIBLE
                    bind.verifyOtpButton.visibility = View.VISIBLE
                    bind.resendOtpButton.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "OTP Sent", Toast.LENGTH_SHORT).show()
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), SendOtpResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SendOtpResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    sendOtp(number)
                } else if (call.isCanceled) {
                    sendOtp(number)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun registration(role: String, name: String, address: String,mobile: String,otp: String){
        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Server::class.java)
        val response = retrofit.userRegistration(RegistrationRequest(name,mobile, address, role,otp))
        response.enqueue(object: Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                loadingDialog.stopLoading()
                Log.d("SignUp",response.code().toString())
                if(response.code()==200){
                    Toast.makeText(requireContext(), "You request send to the admin for approval", Toast.LENGTH_LONG).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }else if (response.code()==406){
                    Toast.makeText(requireContext(), "Number is Used Before, Try Login", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(),"Check the credentials!",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    registration(role, name, address,mobile,otp)
                }else if (call.isCanceled) {
                    registration(role, name, address,mobile,otp)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun closeKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val manager = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}