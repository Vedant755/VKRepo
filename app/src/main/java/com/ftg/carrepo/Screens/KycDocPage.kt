package com.ftg.carrepo.Screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.ftg.carrepo.Models.LoginResponse
import com.ftg.carrepo.Models.UpdateProfileData
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.Utils.Constant.BASE_URL
import com.ftg.carrepo.Utils.Constant.IMAGE_BASE_URL
import com.ftg.carrepo.Utils.LoadingDialog
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.ServerCallInterceptor
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentKycDocPageBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import javax.inject.Inject

@AndroidEntryPoint
class KycDocPage : Fragment() {
    @Inject
    lateinit var interceptor: ServerCallInterceptor
    @Inject
    lateinit var memory: SharedPrefManager

    private lateinit var bind: FragmentKycDocPageBinding
    private lateinit var loadingDialog: LoadingDialog

    private var profilePictureSelected: Boolean = true
    private var profilePictureFile: MultipartBody.Part? = null
    private var kycDocFile: MultipartBody.Part? = null
    private lateinit var userDetails: UserDetails
    private lateinit var id: RequestBody

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentKycDocPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        setActionListeners()
        return bind.root
    }

    private fun setActionListeners(){
        userDetails = memory.getUserDetails()
        id = userDetails.id.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())

//        Glide.with(requireContext()).load(IMAGE_BASE_URL+userDetails.image).into(bind.profileImageView)
//        Glide.with(requireContext()).load(IMAGE_BASE_URL+userDetails.kyc_image).into(bind.kycImageView)
        bind.name.setText(userDetails.name)
        bind.address.setText(userDetails.address)

        bind.profileImageView.setOnClickListener {
            profilePictureSelected = true
            checkPermissions()
        }

        bind.uploadKycDocument.setOnClickListener {
            profilePictureSelected = false
            checkPermissions()
        }

        bind.saveButton.setOnClickListener {
            val name = bind.name.text.toString()
            val address = bind.address.text.toString()
          //  updateProfile(name, address)
        }
    }

    private fun checkPermissions(){
        val permission = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
                Manifest.permission.READ_EXTERNAL_STORAGE
        }else{
            Manifest.permission.READ_MEDIA_IMAGES
        }

        if (requireActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(requireContext(), "Permissions not Granted\nPlease allow from device settings", Toast.LENGTH_LONG).show()
        }else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getImageFromGallery.launch(intent)
        }
    }

    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val file = if(profilePictureSelected) {
                        File(requireActivity().applicationContext.filesDir, "profile.png")}
                    else{
                        File(requireActivity().applicationContext.filesDir, "kyc.png")
                    }

                    val inputStream = requireActivity().contentResolver.openInputStream(result.data?.data!!)
                    val outputStream = FileOutputStream(file)
                    inputStream!!.copyTo(outputStream)

                    val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
                    if(profilePictureSelected){
                        profilePictureFile = MultipartBody.Part.createFormData("profile", file.name, requestFile)
                        bind.profileImageView.setImageURI(result?.data?.data)
                        Toast.makeText(requireContext(), "Profile Picture Selected", Toast.LENGTH_SHORT).show()
                    }else{
                        kycDocFile = MultipartBody.Part.createFormData("kyc", file.name, requestFile)
                        bind.kycImageView.setImageURI(result?.data?.data)
                        Toast.makeText(requireContext(), "KYC Document Selected", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.localizedMessage ?: "Something went wrong", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

//    private fun updateProfile(name: String, address: String) {
//        loadingDialog.startLoading()
//
//        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
//        val retrofit = Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .baseUrl(BASE_URL)
//            .build()
//            .create(Server::class.java)
//
//        if(name!=userDetails.name || address!=userDetails.address){
//            val response = retrofit.updateProfile(UpdateProfileData(address, userDetails.id, userDetails.mobile, name))
//            response.enqueue(object: Callback<LoginResponse>{
//                override fun onResponse(
//                    call: Call<LoginResponse>,
//                    response: Response<LoginResponse>
//                ) {
//                    if(response.code()==200){
//                        memory.saveUserDetails(response.body()?.data)
//                        updateProfilePicture(retrofit)
//                    }else{
//                        updateProfilePicture(retrofit)
//                    }
//                }
//
//                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//                    if (t is SocketTimeoutException) {
//                        updateProfile(name, address)
//                    }else if (call.isCanceled) {
//                        updateProfile(name, address)
//                    } else {
//                        updateProfilePicture(retrofit)
//                    }
//                }
//            })
//        }else{
//            updateProfilePicture(retrofit)
//        }
//    }

    private fun updateProfilePicture(retrofit: Server){
        if(profilePictureFile!=null){
            val response = retrofit.updateProfilePicture(profilePictureFile!!, id)
            response.enqueue(object: Callback<LoginResponse>{
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if(response.code()==200){
                        memory.saveUserDetails(response.body()?.data)
                        updateKycDoc(retrofit)
                    }else{
                        updateKycDoc(retrofit)
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    if (t is SocketTimeoutException) {
                        updateProfilePicture(retrofit)
                    }else if (call.isCanceled) {
                        updateProfilePicture(retrofit)
                    } else {
                        updateKycDoc(retrofit)
                    }
                }
            })

        }else{
            updateKycDoc(retrofit)
        }
    }

    private fun updateKycDoc(retrofit: Server) {
        if(kycDocFile==null){
            loadingDialog.stopLoading()
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }else{
            val response = retrofit.updateKycDoc(kycDocFile!!, id)
            response.enqueue(object: Callback<LoginResponse>{
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    loadingDialog.stopLoading()
                    if(response.code()==200){
                        memory.saveUserDetails(response.body()?.data)
                        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }else{
                        val error = Gson().fromJson(response.errorBody()?.string(), LoginResponse::class.java)
//                        Toast.makeText(requireContext(), error.message ?: "Something went wrong, try later", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    if (t is SocketTimeoutException) {
                        updateKycDoc(retrofit)
                    }else if (call.isCanceled) {
                        updateKycDoc(retrofit)
                    } else {
                        loadingDialog.stopLoading()
                        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            })
        }
    }

}