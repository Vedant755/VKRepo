package com.ftg.carrepo.Screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ftg.carrepo.Adapters.DetailsAdapter
import com.ftg.carrepo.Adapters.ItemAdapter
import com.ftg.carrepo.Models.Branch
import com.ftg.carrepo.Models.GetVehicleAdminData
import com.ftg.carrepo.Models.GetVehicleDetailsData
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.Models.UserDetailsResponse
import com.ftg.carrepo.Models.VehicleAdminDetailsResponse
import com.ftg.carrepo.Models.VehicleDetails
import com.ftg.carrepo.Models.VehicleDetailsResponse
import com.ftg.carrepo.R
import com.ftg.carrepo.RoomDB.MyDatabase
import com.ftg.carrepo.Utils.LoadingDialog
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.ServerCallInterceptor
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentVehicleDetailspageBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class VehicleDetailsPage : Fragment(), ItemAdapter.ItemChangeListener, DetailsAdapter.ContactListener{
    @Inject
    lateinit var interceptor: ServerCallInterceptor

    @Inject
    lateinit var server: Server

    @Inject
    lateinit var memory: SharedPrefManager
    private lateinit var bind: FragmentVehicleDetailspageBinding
    private var vehicleId: String = ""
    private var rcNo: String = ""
    private var tableNo: Int = 1
    private lateinit var requestBody: Branch
    private var contact1: String = " "
    private var contact2: String = " "
    private var contact3: String = " "
    private var headofficeName: String = " "

    private var contact1name: String = " "
    private var contact2name: String = " "
    private var contact3name: String = " "

    private var type: Int = 1
    private var searchOffline: Boolean = false
    private var vehicleDetailsForAdapter: MutableList<LinkedHashMap<String, String>> =
        java.util.ArrayList()
    private var ListvehicleDetails: List<VehicleDetails>? = null
    private var ListBranchDetails: List<Branch>? = null
    private var details: VehicleDetails? = null
    private lateinit var database: MyDatabase
    private lateinit var userDetails: UserDetails
    private lateinit var adapter: DetailsAdapter
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private var currentLocation: Location? = null
    private lateinit var locationManager: LocationManager
    private var address: String = "Local"
    private var latitude: Double = 1.00
    private var longitude: Double = 1.00
    private var level1cont: String = ""
    private var level2cont: String = ""
    private var level3cont: String = ""
    private var level4cont: String = ""
    lateinit var role: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var dialog: BottomSheetDialog
    private lateinit var finance: String
    val gpsLocationListener: LocationListener = LocationListener { location ->
        locationByGps = location
    }
    val networkLocationListener: LocationListener = LocationListener { location ->
        locationByNetwork = location
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
    setOnClickListener()
        return bind.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind = FragmentVehicleDetailspageBinding.inflate(layoutInflater)
        bind.rcv.layoutManager = LinearLayoutManager(requireContext())
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        vehicleId = arguments?.getString("id", "") ?: ""
        rcNo = arguments?.getString("rc_no", "") ?: ""
        searchOffline = arguments?.getBoolean("searchOffline", false) ?: false
        database = MyDatabase.getDatabase(requireContext())
        userDetails = memory.getUserDetails()

        // Run getUserStatus and handle the rest in the callback
        getUserStatus()
    }

    override fun onStart() {
        setOnClickListener()
        super.onStart()
    }

    private fun getUserStatus() {
        val response = server.getUserDetails()
        response.enqueue(object : Callback<UserDetailsResponse> {
            override fun onResponse(call: Call<UserDetailsResponse>, response: Response<UserDetailsResponse>) {
                if (response.code() == 200) {
                    role = response.body()!!.data.role.toString()
                    continueInitialization()
                }
            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                if (t is SocketTimeoutException || call.isCanceled) {
                    getUserStatus()
                } else {
                    Log.d("VK Enterprises", t.localizedMessage)
                }
            }
        })
    }

    private fun continueInitialization() {
        if (role == "ADMIN") {
            fetchDetailsAdmin()
            bind.showCheckBox.setOnCheckedChangeListener { _, isChecked ->
                adapter.updateShowCheckBox(isChecked)
                fetchDetailsAdmin()
            }
        }

        if (role == "ADMIN") {
            if (!isInternetAvailable() || searchOffline) {
                // offlineSearch()
            }
        }

        if (role != "ADMIN") {
            if (!isInternetAvailable() || searchOffline) {
                // offlineSearch()
            } else {
                isLocationOn(true)
                // fetchVehicleDetails()
            }
        }

        if (role == "ADMIN") {
            bind.showCheckBox.visibility = View.VISIBLE
        } else {
            bind.showCheckBox.visibility = View.GONE
        }
    }

    private fun fetchDetailsAdmin() {
        Log.d("ADMIN","ADMIN_DETAILS")
        val response = server.getVehicleAdminDetails(GetVehicleAdminData(rcNo))
        response.enqueue(object : Callback<VehicleAdminDetailsResponse> {
            override fun onResponse(
                call: Call<VehicleAdminDetailsResponse>,
                response: Response<VehicleAdminDetailsResponse>
            ) {

                if (response.code() == 200) {
                    ListvehicleDetails = response.body()!!.data.vehicles
                    ListBranchDetails = response.body()!!.data.branches
                    loadBottomSheet(response.body()!!.data.branches)
                }else{
                    Log.d("Errorrrr",response.code().toString())
                    bind.rcv.visibility = View.GONE
                    bind.userBottomButtons.visibility = View.GONE
                    bind.adminBottomButtons.visibility = View.GONE
                    bind.errorImage.visibility = View.VISIBLE
                    bind.errorText.visibility = View.VISIBLE
                    bind.showCheckBox.visibility = View.GONE
                    bind.top.visibility =View.GONE
                    bind.errorImage.setImageResource(R.drawable.server_error)
                    bind.errorText.text =  "Your role has been changed please ReLogin"
                }
            }

            override fun onFailure(call: Call<VehicleAdminDetailsResponse>, t: Throwable) {
            }

        })
    }

    private fun loadBottomSheet(ListBranchDetail: List<Branch>) {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogView)
        recyclerView = dialogView.findViewById(R.id.rvItem)
        recyclerView.adapter = ItemAdapter(ListBranchDetail, this@VehicleDetailsPage)
        dialog.show()
    }

    override fun onBranchChange(item: Branch) {
        requestBody = item
        fetchVehicleDetailsSpecific(requestBody)
        dialog.dismiss()
    }
    override fun onContactClicked(number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$number")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("VehicleDetailsPage", "Failed to open dialer: ${e.message}")
            Toast.makeText(requireContext(), "Failed to open dialer", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchVehicleDetailsSpecific(requestItem: Branch) {
        details = ListvehicleDetails!!.find { it.branch_id == requestItem._id }
        finance = requestItem.name
        contact1 = requestItem.contact_one.name
        contact2 = requestItem.contact_two.name
        contact3 = requestItem.contact_three.name
        headofficeName = requestItem.head_offices[0].name
        contact1name = requestItem.contact_one.name
        contact2name = requestItem.contact_two.name
        contact3name = requestItem.contact_three.name
        buildDetails(details!!)
        adapter = DetailsAdapter(
            vehicleDetailsForAdapter,
            userDetails.role == "ADMIN",
            requireContext(),
            bind.showCheckBox.isChecked,this
        )

        bind.rcv.adapter = adapter
        adapter.setOnClickListener(object : DetailsAdapter.OnClickListener {


            override fun onClick(value: String) {
            }
        })

    }


    private fun setOnClickListener() {
        if (userDetails.role == "ADMIN") {
            bind.userBottomButtons.visibility = View.GONE
            bind.adminBottomButtons.visibility = View.VISIBLE
        } else {
            bind.userBottomButtons.visibility = View.VISIBLE
            bind.adminBottomButtons.visibility = View.GONE
        }
        bind.userCopyButton.setOnClickListener {
            if (adapter.sb.toString().isBlank()) {
                adapter.copyDetails(requireContext(), getFullMessage())
            } else {
                adapter.copyDetails(requireContext(), null)
            }
        }
        bind.userWhatsappButton.setOnClickListener {
            if (adapter.sb.toString().isBlank()) {
                adapter.whatsapp(requireContext(), getFullMessage())
            } else {
                adapter.whatsapp(requireContext(), null)
            }
        }

        bind.adminCopyButton.setOnClickListener {
            if (adapter.sb.toString().isBlank()) {
                adapter.copyDetails(requireContext(), getFullMessage())
            } else {
                adapter.copyDetails(requireContext(), null)
            }
        }
        bind.adminOkButton.setOnClickListener {
            showButtonDetails(details!!, "OKRepo")
        }
        bind.adminConfirmButton.setOnClickListener {
            showButtonDetails(details!!, "ConfirmButton")
        }
        bind.adminCancelButton.setOnClickListener {
            showButtonDetails(details!!, "CancelButton")
        }

        bind.adminWhatsappButton.setOnClickListener {
            if (adapter.sb.toString().isBlank()) {
                adapter.whatsapp(requireContext(), getFullMessage())
            } else {
                adapter.whatsapp(requireContext(), null)
            }
        }


    }

    //    private fun offlineSearch(){
//        CoroutineScope(Dispatchers.IO).launch {
//            details = database.getDao().getVehicleDetails(tableNo, 1)
//            Log.d("OFFLINE",details.toString())
//            withContext(Dispatchers.Main) {
//                if (details != null) {
//                    buildDetails(details!!)
//                    adapter = DetailsAdapter(vehicleDetailsForAdapter, userDetails.role == "ADMIN",requireContext(),false)
//                    bind.rcv.adapter = adapter
//                }else{
//                    bind.rcv.visibility = View.GONE
//                    bind.userBottomButtons.visibility = View.GONE
//                    bind.adminBottomButtons.visibility = View.GONE
//                    bind.errorImage.visibility = View.VISIBLE
//                    bind.errorText.visibility = View.VISIBLE
//                    bind.errorImage.setImageResource(R.drawable.not_found)
//                    bind.errorText.text = "Not Found"
//                }
//            }
//        }
//    }
    @SuppressLint("MissingPermission")
    fun isLocationOn(isGettingLocationFirstTime: Boolean) {
        try {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT)
                    .show()

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                requireActivity().startActivity(intent)
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!hasNetwork) {
                    // Network provider is not enabled, prompt user to enable it
                    Toast.makeText(
                        requireContext(),
                        "Network provider is not enabled",
                        Toast.LENGTH_SHORT
                    ).show()
                    val networkIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    requireActivity().startActivity(networkIntent)
                    requireActivity().supportFragmentManager.popBackStack()
                }

                if (hasGps) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        0F,
                        gpsLocationListener
                    )
                }
                if (hasNetwork) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000,
                        0F,
                        networkLocationListener
                    )
                }

                var currentLocation: Location? = null

                val lastKnownLocationByGps =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnownLocationByGps != null) {
                    currentLocation = lastKnownLocationByGps
                }

                val lastKnownLocationByNetwork =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnownLocationByNetwork != null && (currentLocation == null || lastKnownLocationByNetwork.accuracy > currentLocation.accuracy)) {
                    currentLocation = lastKnownLocationByNetwork
                }

                if (currentLocation != null) {
                    val latitude = currentLocation.latitude
                    val longitude = currentLocation.longitude

                    val address: Address?
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        address = addresses[0]
                        val locality = address.locality ?: ""
                        val postalCode = address.postalCode ?: ""
                        val countryName = address.countryName ?: ""
                        this.address = "$locality, $postalCode, $countryName"
                        this.latitude = latitude
                        this.longitude = longitude
                        fetchVehicleDetails()
                    } else {
                        Log.e(
                            "LocationDebug",
                            "No address found for location: ($latitude, $longitude)"
                        )
                    }
                } else {
                    Log.e("LocationDebug", "No location found.")
                }
            }
        } catch (e: Exception) {
            Log.e("LocationDebug", "Exception occurred: ${e.message}")
            e.printStackTrace()
            address = "India"
            latitude = 20.593684
            longitude = 78.96288
            fetchVehicleDetails()
        }
    }


    private fun fetchVehicleDetails() {
        Log.d("USER","USER_DETAILS")
        val response = server.getVehicleDetails(
            GetVehicleDetailsData(
                vehicleId,
                address,
                latitude.toString(),
                longitude.toString()
            )
        )
        response.enqueue(object : Callback<VehicleDetailsResponse> {
            override fun onResponse(
                call: Call<VehicleDetailsResponse>,
                response: Response<VehicleDetailsResponse>
            ) {
                if (response.code() == 200) {
                    details = response.body()?.data
                    if (details != null) {
                        buildDetails(details!!)
                        adapter = DetailsAdapter(
                            vehicleDetailsForAdapter,
                            userDetails.role == "ADMIN",
                            requireContext(),
                            bind.showCheckBox.isChecked,this@VehicleDetailsPage
                        )
                        bind.rcv.adapter = adapter
                    } else {
                        bind.rcv.visibility = View.GONE
                        bind.userBottomButtons.visibility = View.GONE
                        bind.adminBottomButtons.visibility = View.GONE
                        bind.errorImage.visibility = View.VISIBLE
                        bind.errorText.visibility = View.VISIBLE
                        bind.errorImage.setImageResource(R.drawable.not_found)
                        bind.errorText.text = "Not Found"
                    }
                } else {
                    bind.rcv.visibility = View.GONE
                    bind.userBottomButtons.visibility = View.GONE
                    bind.adminBottomButtons.visibility = View.GONE
                    bind.errorImage.visibility = View.VISIBLE
                    bind.errorText.visibility = View.VISIBLE
                    bind.showCheckBox.visibility = View.GONE
                    bind.top.visibility =View.GONE
                    bind.errorImage.setImageResource(R.drawable.server_error)
                    bind.errorText.text =  "Your role has been changed please ReLogin"
                }
            }

            override fun onFailure(call: Call<VehicleDetailsResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    fetchVehicleDetails()
                } else if (call.isCanceled) {
                    fetchVehicleDetails()
                } else {
                    bind.rcv.visibility = View.GONE
                    bind.userBottomButtons.visibility = View.GONE
                    bind.adminBottomButtons.visibility = View.GONE
                    bind.errorImage.visibility = View.VISIBLE
                    bind.errorText.visibility = View.VISIBLE
                    bind.errorImage.setImageResource(R.drawable.server_error)
                    bind.errorText.text =  "Something went wrong /n Your role has been changed please ReLogin"
                }
            }
        })
    }


    private fun buildDetails(it: VehicleDetails) {
        if (userDetails.role == "ADMIN") {
            buildDetailsMap("Excel Finance", it.financer ?: "")
            buildDetailsMap("Agreement No", it.contract_no ?: "")
            buildDetailsMap("Customer Name", it.customer_name ?: "")
            buildDetailsMap("RC Number", it.rc_no ?: "")
            buildDetailsMap("Model/Make", it.mek_and_model ?: "")
            buildDetailsMap("Chassis No", it.chassis_no ?: "")
            buildDetailsMap("Engine No", it.engine_no ?: "")
            bind.headingText.text = "Details for :${it.rc_no}"
            buildDetailsMap("Region", it.region ?: "")
            buildDetailsMap("Branch", it.branch ?: "")
            buildDetailsMap("Area", it.area ?: "")
            buildDetailsMap("Bucket", it.bkt ?: "")
            buildDetailsMap("OD", it.od ?: "")
            buildDetailsMap("POS", it.poss ?: "")
            buildDetailsMap("GV", it.gv ?: "")
            buildDetailsMap("Ses 9", it.ses9 ?: "")
            buildDetailsMap("Ses 17",it.ses17?:"")
            buildDetailsMap("TBR", it.tbr ?: "")
            buildDetailsMap("Ex Name", it.ex_name ?: "")
            buildDetailsMap("Level 1", it.level1 ?: "")
            try {

                val inputString = it.level1
                val phoneNumberRegex =
                    Regex("\\d{10}") // Assuming the phone number consists of 10 digits
                val phoneNumberMatch = phoneNumberRegex.find(inputString!!)
                level1cont = phoneNumberMatch?.value ?: ""
                if (it.level1con != null) {
                    buildDetailsMap("Level 1con", it.level1con)
                } else {
                    buildDetailsMap("Level 1con", level1cont)
                }
            } catch (e: NullPointerException) {
                // Handle if level1 is null
                buildDetailsMap("Level 1", " ")
                buildDetailsMap("Level 1con", it.level1con ?: " ")
            }

            try {
                buildDetailsMap("Level 2", it.level2 ?: "")
                val inputString2 = it.level2
                val phoneNumberRegex2 =
                    Regex("\\d{10}") // Assuming the phone number consists of 10 digits
                val phoneNumberMatch2 = phoneNumberRegex2.find(inputString2!!)
                level2cont = phoneNumberMatch2?.value ?: ""
                if (it.level2con != null) {
                    buildDetailsMap("Level 2con", it.level2con)
                } else {
                    buildDetailsMap("Level 2con", level2cont)
                }
            } catch (e: NullPointerException) {
                // Handle if level2 is null
                buildDetailsMap("Level 2", " ")
                buildDetailsMap("Level 2con", it.level2con ?: " ")
            }

            try {
                buildDetailsMap("Level 3", it.level3 ?: "")
                val inputString3 = it.level3
                val phoneNumberRegex3 =
                    Regex("\\d{10}") // Assuming the phone number consists of 10 digits
                val phoneNumberMatch3 = phoneNumberRegex3.find(inputString3!!)
                level3cont = phoneNumberMatch3?.value ?: ""
                if (it.level3con != null) {
                    buildDetailsMap("Level 3con", it.level3con)
                } else {
                    buildDetailsMap("Level 3con", level3cont)
                }
            } catch (e: NullPointerException) {
                // Handle if level3 is null
                buildDetailsMap("Level 3", " ")
                buildDetailsMap("Level 3con", it.level3con ?: " ")
            }

            try {
                buildDetailsMap("Level 4", it.level4 ?: "")
                val inputString4 = it.level4
                val phoneNumberRegex4 =
                    Regex("\\d{10}") // Assuming the phone number consists of 10 digits
                val phoneNumberMatch4 = phoneNumberRegex4.find(inputString4!!)
                level4cont = phoneNumberMatch4?.value ?: ""
                if (it.level4con != null) {
                    buildDetailsMap("Level 4con", it.level4con)
                } else {
                    buildDetailsMap("Level 4con", level4cont)
                }
            } catch (e: NullPointerException) {
                // Handle if level4 is null
                buildDetailsMap("Level 4", " ")
                buildDetailsMap("Level 4con", it.level4con ?: " ")
            }

            buildDetailsMap("Finance", headofficeName)
            buildDetailsMap("Branch", finance)
            buildDetailsMap("Contact 1", contact1)
            buildDetailsMap("Contact 2", contact2)
            buildDetailsMap("Contact 3", contact3)
            buildDetailsMap("Seasoning", "")
            buildDetailsMap("Executive Name", "")
            buildDetailsMap("Remark", "")
            val parsed: ZonedDateTime = ZonedDateTime.parse(it.updatedAt)
            val z: ZonedDateTime = parsed.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
            val fmt = DateTimeFormatter.ofPattern("dd MMM, yyyy hh:mm a", Locale.ENGLISH)
            buildDetailsMap("Uploaded On", fmt.format(z))



        } else {
            var map: LinkedHashMap<String, String> = LinkedHashMap()
            map["VEHICLE DETAILS:-"] = ""
            vehicleDetailsForAdapter.add(map)
            bind.headingText.text = "Details for :${it.rc_no}"
            buildDetailsMap("Vehicle No", it.rc_no ?: "")
            buildDetailsMap("Chassis No", it.chassis_no ?: "")
            buildDetailsMap("Model/Make", it.mek_and_model ?: "")
            buildDetailsMap("Engine No", it.engine_no ?: "")
            buildDetailsMap("Customer Name", it.customer_name ?: "")
            buildDetailsMap("Load Details", "")

            map = LinkedHashMap()
            map[""] = ""
            vehicleDetailsForAdapter.add(map)

            map = LinkedHashMap()
            map["AGENCY:-"] = ""
            vehicleDetailsForAdapter.add(map)

            buildDetailsMap("Name", "VK Enterprises")
            buildDetailsMap("Contact", "9112200030, 9112200040, 9112200050")
        }
    }

    private fun buildDetailsMap(key: String, value: String?) {
        val map: LinkedHashMap<String, String> = LinkedHashMap(2)
        if (value == "null" || value.isNullOrBlank()) {
            map[key] = ""
        } else {
            map[key] = value
        }
        vehicleDetailsForAdapter.add(map)
    }

    private fun showButtonDetails(it: VehicleDetails, buttonName: String) {
        val fragment = CustomButtonDetail()
        val bundle = Bundle()
        bundle.putString("name", userDetails.name)
        bundle.putString("buttonName", buttonName)
        bundle.putString("branch",it.branch)
        bundle.putString("loan_no", it.contract_no ?: "-")
        bundle.putString("cust_name", it.customer_name ?: "-")
        bundle.putString("vehicle_no", it.rc_no ?: "-")
        bundle.putString("model_no", it.mek_and_model ?: "-")
        bundle.putString("chassis_no", it.chassis_no ?: "-")
        bundle.putString("level1", it.level1con?:level1cont)
        bundle.putString("level2", it.level2con?:level2cont)
        bundle.putString("level3", it.level3con?:level3cont)
        bundle.putString("level4", it.level4con?:level4cont)


        bundle.putString("contact_person1", contact1)
        bundle.putString("contact_person2", contact2)
        bundle.putString("contact_person3", contact3)
        fragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .add(R.id.home_container, fragment)
            .addToBackStack(null).commit()
    }

    private fun getFullMessage(): String {
        val userDetails = memory.getUserDetails()

        val name = userDetails.name
        val mobile = userDetails.mobile

        return if (userDetails.role == "ADMIN") {
            "*Respected Sir*,\nA vehicle has been traced out by our ground team. The details of the vehicle and customer are as below.\n\n" +
                    "Loan No - *${details?.contract_no ?: ""}*\n" +
                    "Customer Name - *${details?.customer_name ?: ""}*\n" +
                    "Branch - *${details?.branch ?: ""}*\n" +
                    "Vehicle No - *${details?.rc_no ?: ""}*\n" +
                    "Chassis No - *${details?.chassis_no ?: ""}*\n" +
                    "Vehicle Model - *${details?.mek_and_model ?: ""}*\n" +
                    "Bucket - *${details?.bkt ?: ""}*\n" +
                    "OD - *${details?.od ?: ""}*\n" +
                    "Region - *${details?.region ?: ""}*\n" +
                    "Level1 - *${details?.level1 ?: ""}*\n" +
                    "Level2 - *${details?.level2 ?: ""}*\n" +
                    "Level3 - *${details?.level3 ?: ""}*\n" +
                    "Level4 - *${details?.level4 ?: ""}*\n" +

                    "We urgently need you to confirm the status of this vehicle, whether it is to be Repo or released. *V K Enterprises* contact person:\n$name: $mobile.\nThank you for your cooperation.\nBest regards,\n*V K Enterprises, Maharashtra*"
        } else {
            "*Respected Sir*,\n" +
                    "Customer Name: *${details?.customer_name ?: "-"}*\n" +
                    "Vehicle No: *${details?.rc_no ?: "-"}*\n" +
                    "Chassis No: *${details?.chassis_no ?: "-"}*\n" +
                    "Model/Maker: *${details?.mek_and_model ?: "-"}*\n" +
                    "Engine No: *${details?.engine_no ?: "-"}*\n" +
                    "Load Details: *-* \n" +
                    "Vehicle Location: *${address}*\n" +
                    "Status: *Please confirm this vehicle*\n" +
                    "$name - $mobile\n" +
                    "Agency name: *V K Enterprises*"
        }
    }

    private fun isInternetAvailable(): Boolean {
        (requireActivity().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            return this.getNetworkCapabilities(this.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }
    }



}