package com.ftg.carrepo.Screens

import android.os.Build.VERSION_CODES.R
import android.os.Bundle

import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.ftg.carrepo.Adapters.UsersAdapter
import com.ftg.carrepo.Models.AllUsersResponse
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.R
import com.ftg.carrepo.Utils.Constant
import com.ftg.carrepo.Utils.LoadingDialog
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.ServerCallInterceptor
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentAllUsersPageBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class AllUsersPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var bind: FragmentAllUsersPageBinding
    private var allUsers = ArrayList<UserDetails>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentAllUsersPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        bind.rcv.layoutManager = LinearLayoutManager(requireContext())
        getAllUsers()
        setActionListeners()

        return bind.root
    }

    private fun getAllUsers(){
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Constant.BASE_URL)
            .build()
            .create(Server::class.java)

        val response = retrofit.getAllUsers()
        response.enqueue(object: Callback<AllUsersResponse>{
            override fun onResponse(
                call: retrofit2.Call<AllUsersResponse>,
                response: Response<AllUsersResponse>
            ) {
                if(response.code()==200){
                    val result = response.body()?.data
                    if(!result.isNullOrEmpty()){
                        allUsers = result as ArrayList<UserDetails>
                        showUsers(result)
                    }else{
                        Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Log.e("ERROR",response.body().toString())
                    val error = Gson().fromJson(response.errorBody()?.string(), AllUsersResponse::class.java)
                    Toast.makeText(requireContext(), "Please Re-login \n Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AllUsersResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllUsers()
                } else if (call.isCanceled) {
                    getAllUsers()
                } else {
                    Log.d("VK Enterprises", t.localizedMessage)
                }
            }

        })
    }

    private fun showUsers(users: List<UserDetails>) {
        bind.rcv.adapter = UsersAdapter(users){
            val fragment = UserPlanChangePage()
            val bundle = Bundle()

            bundle.putString("accessFrom", it.createdAt)
            bundle.putString("address", it.address)
            bundle.putString("id", it.id)
            bundle.putString("mobile", it.mobile)
            bundle.putString("name", it.name)
            bundle.putString("role", it.role)
            bundle.putString("status", it.status)

            fragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(com.ftg.carrepo.R.anim.enter, com.ftg.carrepo.R.anim.exit, com.ftg.carrepo.R.anim.pop_enter, com.ftg.carrepo.R.anim.pop_exit)
                .replace(com.ftg.carrepo.R.id.container, fragment).addToBackStack(null)
                .commit()
        }
    }
    private fun setActionListeners(){
        bind.search.addTextChangedListener { text ->
            val input = text.toString()
            if(input.isBlank()){
                showUsers(allUsers)
            }else{
                val filteredUsers = allUsers.filter { it.name?.contains(input, true) == true || it.mobile?.toString()?.contains(input, true) == true }
                showUsers(filteredUsers)
            }
        }
    }
}