package com.ftg.carrepo.Screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ftg.carrepo.Models.AllUsersResponse
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.Models.UserPlanResponse
import com.ftg.carrepo.Utils.Constant
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.ServerCallInterceptor
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentSubscriptionPageBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.ArrayList
import java.util.Locale
import javax.inject.Inject
import javax.security.auth.callback.Callback

@AndroidEntryPoint
class SubscriptionPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    @Inject
    lateinit var interceptor: ServerCallInterceptor
    @Inject
    lateinit var server: Server
    private lateinit var bind: FragmentSubscriptionPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSubscriptionPageBinding.inflate(inflater, container, false)
       setData()
        return bind.root
    }

    private fun setData(){
            val fmt = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl(Constant.BASE_URL)
                .build()
                .create(Server::class.java)

            val response = retrofit.getUserPlan()
            response.enqueue(object: retrofit2.Callback<UserPlanResponse> {


                override fun onResponse(
                    call: Call<UserPlanResponse>,
                    response: Response<UserPlanResponse>
                ) {
                    Log.d("responseExp",response.code().toString())
                    if (response.isSuccessful){
                        val parsed: ZonedDateTime = ZonedDateTime.parse(response.body()!!.data.startDate)
                        val z: ZonedDateTime = parsed.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                        bind.from.text = fmt.format(z)

                        val parseded: ZonedDateTime = ZonedDateTime.parse(response.body()!!.data.endDate)
                        val zed: ZonedDateTime = parseded.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                        bind.to.text = fmt.format(zed)
                        val daysBetween = ChronoUnit.DAYS.between(parsed.toLocalDate(), parseded.toLocalDate())
                        bind.remainingDays.text=daysBetween.toString()
                    }
                }

                override fun onFailure(call: Call<UserPlanResponse>, t: Throwable) {
                    Log.e("Error",t.message.toString())
                }

            })


    }
}