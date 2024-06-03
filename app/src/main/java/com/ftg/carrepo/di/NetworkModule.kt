package com.ftg.carrepo.di

import android.content.Context
import androidx.room.Room
import com.ftg.carrepo.RoomDB.MyDatabase
import com.ftg.carrepo.Utils.Constant
import com.ftg.carrepo.Utils.Constant.BASE_URL
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.ServerCallInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext as ApplicationContext1

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext1 appContext: Context): MyDatabase {
        return Room.databaseBuilder(
            appContext,
            MyDatabase::class.java,
            "MyOfflineDatabase"
        ).build()
    }
    @Singleton
    @Provides
    fun provideServer(interceptor: ServerCallInterceptor): Server {
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(Server::class.java)
    }


}