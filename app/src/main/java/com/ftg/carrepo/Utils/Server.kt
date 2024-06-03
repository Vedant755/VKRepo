package com.ftg.carrepo.Utils

import com.ftg.carrepo.Models.AllUsersResponse
import com.ftg.carrepo.Models.DeleteResponse
import com.ftg.carrepo.Models.DownloadResponse
import com.ftg.carrepo.Models.DownloadingApiRequestData
import com.ftg.carrepo.Models.GeneralResponse
import com.ftg.carrepo.Models.GetVehicleAdminData
import com.ftg.carrepo.Models.GetVehicleDetailsData
import com.ftg.carrepo.Models.LoadBranchRequest
import com.ftg.carrepo.Models.LoadBranchResponse
import com.ftg.carrepo.Models.LoginResponse
import com.ftg.carrepo.Models.PlanAllRequest
import com.ftg.carrepo.Models.PlanAllResponse
import com.ftg.carrepo.Models.PlanDetails
import com.ftg.carrepo.Models.PlanDetailsResp
import com.ftg.carrepo.Models.RegistrationRequest
import com.ftg.carrepo.Models.SearchAdminVehicleData
import com.ftg.carrepo.Models.SearchVehicleData
import com.ftg.carrepo.Models.SearchVehicleResponse
import com.ftg.carrepo.Models.SendOtpData
import com.ftg.carrepo.Models.SendOtpResponse
import com.ftg.carrepo.Models.UpdateProfileData
import com.ftg.carrepo.Models.UserDetailsResponse
import com.ftg.carrepo.Models.UserPlanResponse
import com.ftg.carrepo.Models.UserTypeData
import com.ftg.carrepo.Models.VehicleDetailsResponse
import com.ftg.carrepo.Models.UserStatusData
import com.ftg.carrepo.Models.VehicleAdminDetailsResponse
import com.ftg.carrepo.Models.VerifyOtpData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part


interface Server {
    @POST("auth/send")
    fun sendOtpLogin(
        @Body mobile: SendOtpData
    ): retrofit2.Call<SendOtpResponse>


    @POST("auth/android/login")
    fun login(
        @Body body: VerifyOtpData
    ):retrofit2.Call<LoginResponse>

    @POST("plan/all")
    fun planAll(
        @Body body: PlanAllRequest
    ): retrofit2.Call<PlanAllResponse>



    @POST("user/registration")
    fun userRegistration(
        @Body details: RegistrationRequest
    ): retrofit2.Call<GeneralResponse>

    @GET("user")
    fun getAllUsers(): retrofit2.Call<AllUsersResponse>
    @GET("plan/plan")
    fun getUserPlan(): Call<UserPlanResponse>

    @POST("user/send/otp")
    fun sendOtpRegistration(
        @Body mobile: SendOtpData
    ):Call<SendOtpResponse>
    @POST("plan/registration")
    fun changeUserPlan(
        @Body plan: PlanDetails
    ):retrofit2.Call<PlanDetailsResp>

    @POST("auth/login")
    fun verifyOtpLogin(
        @Body details: VerifyOtpData
    ): retrofit2.Call<LoginResponse>

    @PUT("user/update")
    fun updateProfile(
        @Body details: UpdateProfileData
    ): retrofit2.Call<LoginResponse>

    @POST("user/details")
    fun getUserDetails(
        @Body id: UpdateProfileData
    ):retrofit2.Call<LoginResponse>
    @POST("vehicle/admin/pagination")
    fun searchAdminVehicle(
        @Body number: SearchAdminVehicleData,
    ): retrofit2.Call<SearchVehicleResponse>
    @POST("vehicle/user/pagination")
     fun searchVehicle(
        @Body number: SearchVehicleData,
    ): retrofit2.Call<SearchVehicleResponse>

    @POST("vehicle/user/details/id")
    fun getVehicleDetails(
        @Body data: GetVehicleDetailsData
    ):retrofit2.Call<VehicleDetailsResponse>



    @POST("vehicle/admin/details/duplicate")
    fun getVehicleAdminDetails(
        @Body data: GetVehicleAdminData
    ):retrofit2.Call<VehicleAdminDetailsResponse>

    @Multipart
    @PUT("user/profile/image")
    fun updateProfilePicture(
        @Part file: MultipartBody.Part,
        @Part("id") id: RequestBody
    ):retrofit2.Call<LoginResponse>

    @Multipart
    @PUT("user/kyc/image")
    fun updateKycDoc(
        @Part file: MultipartBody.Part,
        @Part("id") id: RequestBody
    ):retrofit2.Call<LoginResponse>

    @POST("processing/download")
    fun downloadNewData(
        @Body data: DownloadingApiRequestData
    ): retrofit2.Call<DownloadResponse>

    @HTTP(method = "DELETE", path = "processing/delete", hasBody = true)
    fun getRemainingVehicles(
        @Body data: DownloadingApiRequestData
    ): retrofit2.Call<DeleteResponse>

    @PUT("user/details") // 200
    fun changeUserType(
        @Body data: UserTypeData
    ): retrofit2.Call<GeneralResponse>

    @PUT("user/details") // 200
    fun changeUserStatus(
        @Body data: UserStatusData
    ): retrofit2.Call<GeneralResponse>

    @GET("user/details")
    fun getUserDetails(): Call<UserDetailsResponse>

}