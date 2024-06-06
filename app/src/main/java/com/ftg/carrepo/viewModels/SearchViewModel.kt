package com.ftg.carrepo.viewModels

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ftg.carrepo.Models.SearchVehicleData
import com.ftg.carrepo.Models.SearchVehicleResponse
import com.ftg.carrepo.Models.SearchedVehicleDetails
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.RoomDB.MyDatabase
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.SharedPrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.ftg.carrepo.Models.AllUsersResponse
import com.ftg.carrepo.Models.SearchAdminVehicleData
import com.ftg.carrepo.Models.UserDetailsResponse
import com.ftg.carrepo.Utils.Constant
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class SearchViewModel @Inject constructor(
    private val server: Server,
    private val database: MyDatabase,
    private val memory: SharedPrefManager
) : ViewModel() {
    private lateinit var userDetails: UserDetails
    var searchByRc: Boolean = true
    var query: String = ""
    var isTwoColumn: Boolean = false
    var searchOffline: Boolean = false
    private lateinit var searchOnServerCall: Call<SearchVehicleResponse>
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val _searchResult = MutableLiveData<List<SearchedVehicleDetails>?>()
    var searchResult: MutableLiveData<List<SearchedVehicleDetails>?> = _searchResult
    lateinit var status: String

    fun initialize(arguments: Bundle?, context: Context) {
        scope.launch {
            getUserStatus()
            userDetails = memory.getUserDetails()
            searchByRc = arguments?.getBoolean("isRc", true) ?: true
            isTwoColumn = arguments?.getBoolean("isTwoColumn", false) ?: false
            searchOffline = arguments?.getBoolean("searchOffline", false) ?: false
            query = arguments?.getString("query", "") ?: ""

            if (userDetails.role == "ADMIN") {
                searchAdminVehicle(context)
            } else {
                searchVehicle(context)
            }
        }
    }

    suspend fun getUserStatus() {
        return suspendCoroutine { continuation ->
            val response = server.getUserDetails()
            response.enqueue(object : Callback<UserDetailsResponse> {
                override fun onResponse(
                    call: Call<UserDetailsResponse>,
                    response: Response<UserDetailsResponse>
                ) {
                    if (response.code() == 200) {
                        status = response.body()?.data!!.status.toString()
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(RuntimeException("Failed to get user status"))
                    }
                }

                override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                    if (t is SocketTimeoutException || call.isCanceled) {
                        scope.launch {
                            getUserStatus()
                            continuation.resume(Unit)
                        }
                    } else {
                        Log.d("VK Enterprises", t.localizedMessage)
                        continuation.resumeWithException(t)
                    }
                }
            })
        }
    }

    fun searchVehicle(context: Context) {
        if (status == "ACTIVE") {
            if (!isInternetAvailable(context) || searchOffline) {
                scope.launch {
                    val result = if (searchByRc)
                        database.getDao().searchWithRc("%$query%")
                    else
                        database.getDao().searchWithChassis("%$query%")

                    if (!result.isNullOrEmpty()) {
                        val distinctResult = result.distinctBy { it.rc_no }
                        _searchResult.postValue(distinctResult)
                    } else {
                        _searchResult.postValue(emptyList())
                    }
                }
            } else {
                searchOnServerCall = if (searchByRc)
                    server.searchVehicle(SearchVehicleData(query, "rc_no",1,5000))
                else
                    server.searchVehicle(SearchVehicleData(query, "chassis_no",1,5000))

                searchOnServerCall.enqueue(object : Callback<SearchVehicleResponse> {
                    override fun onResponse(
                        call: Call<SearchVehicleResponse>,
                        response: Response<SearchVehicleResponse>
                    ) {
                        if (response.isSuccessful) {
                            var result = response.body()?.data
                            if (!result.isNullOrEmpty()) {
                                val distinctResult = result.distinctBy { it.rc_no }.toMutableList()
                                if (distinctResult.size % 2 != 0) {
                                    distinctResult.add(createDummyElement()) // Add a dummy element
                                }
                                _searchResult.postValue(distinctResult)
                            } else {
                                _searchResult.postValue(emptyList())
                            }
                        } else {
                            _searchResult.postValue(emptyList())
                        }
                    }

                    override fun onFailure(call: Call<SearchVehicleResponse>, t: Throwable) {
                        _searchResult.postValue(emptyList())
                    }
                })
            }
        }
    }
    private fun createDummyElement(): SearchedVehicleDetails {
        // Create and return a dummy element
        return SearchedVehicleDetails(
            _id = " ",
            mek_and_model = " ",
            rc_no = " ",
            chassis_no = " ",
            // Add other necessary fields here
        )
    }

    fun searchAdminVehicle(context: Context) {
        if (status == "ACTIVE") {
        if (!isInternetAvailable(context) || searchOffline) {
            scope.launch {
                val result = if (searchByRc)
                    database.getDao().searchWithRc("%$query%")
                else
                    database.getDao().searchWithChassis("%$query%")

                if (!result.isNullOrEmpty()) {
                    val distinctResult = result.distinctBy { it.rc_no }
                    _searchResult.postValue(distinctResult)
                } else {
                    _searchResult.postValue(emptyList())
                }
            }
        } else {
            searchOnServerCall = if (searchByRc)
                server.searchAdminVehicle(SearchAdminVehicleData(query, "rc_no", 1,5000))
            else
                server.searchAdminVehicle(SearchAdminVehicleData(query, "chassis_no", 1,5000))

            searchOnServerCall.enqueue(object : Callback<SearchVehicleResponse> {
                override fun onResponse(
                    call: Call<SearchVehicleResponse>,
                    response: Response<SearchVehicleResponse>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()?.data
                        if (!result.isNullOrEmpty()) {
                            val distinctResult = result.distinctBy { it.rc_no }.toMutableList()
                            if (distinctResult.size % 2 != 0) {
                                distinctResult.add(createDummyElement()) // Add a dummy element
                            }
                            _searchResult.postValue(distinctResult)
                        } else {
                            _searchResult.postValue(emptyList())
                        }
                    } else {
                        _searchResult.postValue(emptyList())
                    }
                }

                override fun onFailure(call: Call<SearchVehicleResponse>, t: Throwable) {
                    _searchResult.postValue(emptyList())
                }
            })
        }
    }}

    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
        return result
    }

    fun cancelRequests() {
        if (::searchOnServerCall.isInitialized && searchOnServerCall.isExecuted) {
            searchOnServerCall.cancel()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}