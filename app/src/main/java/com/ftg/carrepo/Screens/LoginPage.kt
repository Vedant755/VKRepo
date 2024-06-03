package com.ftg.carrepo.Screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ftg.carrepo.Models.LoginResponse
import com.ftg.carrepo.Models.SendOtpData
import com.ftg.carrepo.Models.SendOtpResponse
import com.ftg.carrepo.Models.RegistrationRequest
import com.ftg.carrepo.Models.VerifyOtpData
import com.ftg.carrepo.R
import com.ftg.carrepo.Utils.Constant.BASE_URL
import com.ftg.carrepo.Utils.LoadingDialog
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentLoginPageBinding
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
class LoginPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    private lateinit var bind: FragmentLoginPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private var number: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentLoginPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        setActionListeners()

        return bind.root
    }

    private fun setActionListeners(){
        bind.registerButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.container, SignupPage())
                .addToBackStack(null).commit()
        }

        bind.loginButton.setOnClickListener {
            closeKeyboard()
            number = bind.mobileNumber.text.toString()
            val otp = bind.otp.text.toString()
            if (number!!.isBlank() || number!!.length < 10)
                Toast.makeText(requireContext(), "Enter Phone Number!", Toast.LENGTH_SHORT).show()
            else if (otp.isBlank()){
                Toast.makeText(requireContext(), "Enter OTP!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                loadingDialog.startLoading()
                login(otp)
            }
        }




    }
    private fun  login(otp: String){
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Server::class.java)
        val response = retrofit.login(VerifyOtpData(number!!, otp))
        response.enqueue(object: Callback<LoginResponse>{
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==200){
                    val result = response.body()
                    if(!result?.token.isNullOrBlank()){
                        memory.saveToken(result?.token)
                        memory.saveUserDetails(result?.data)
                        Toast.makeText(requireContext(), "Verified", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                            .replace(R.id.container, HomePage()).commit()
                    } else {
                        Toast.makeText(requireContext(), "Something went wrong\nPlease try later", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), "Something went wrong, try later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    login(otp)
                }else if (call.isCanceled) {
                    login(otp)
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