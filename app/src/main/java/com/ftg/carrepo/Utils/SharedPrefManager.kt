package com.ftg.carrepo.Utils

import android.content.Context
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.Utils.Constant.PREF_FILE
import com.ftg.carrepo.Utils.Constant.USER_TOKEN
import com.ftg.carrepo.Utils.Constant.role
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPrefManager  @Inject constructor(@ApplicationContext context: Context) {

    private var pref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    fun saveToken(token: String?){
        val editor = pref.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }
    fun saveRole(role:String?){
        val editor =pref.edit()
        editor.putString(Constant.role,role)
        editor.apply()
    }
    fun getRole(): String?{
        return pref.getString(role, null)
    }
    fun getToken(): String?{
        return pref.getString(USER_TOKEN, null)
    }

    fun saveUserDetails(data: UserDetails?) {
        val editor = pref.edit()
        editor.putString("_id", data?.id)
        editor.putString("name", data?.name)
        editor.putString("mobile", data?.mobile)
        editor.putString("address", data?.address)
        editor.putString("role", data?.role)
        editor.putString("status", data?.status)
        editor.putString("createdAt", data?.createdAt)
        editor.putString("updatedAt", data?.updatedAt)
        editor.putInt("__v", data?.v ?: 0)
        editor.apply()
    }


    fun getUserDetails(): UserDetails {
        return UserDetails(
            pref.getString("_id", null) ?: "",
            pref.getString("name", null) ?: "",
            pref.getString("mobile", null) ?: "",
            pref.getString("address", null) ?: "",
            pref.getString("role", null) ?: "",
            pref.getString("status", null) ?: "",
            pref.getString("createdAt", null) ?: "",
            pref.getString("updatedAt", null) ?: "",
            pref.getInt("__v", 0)
        )
    }


}