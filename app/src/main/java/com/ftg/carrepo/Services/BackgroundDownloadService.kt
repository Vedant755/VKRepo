package com.ftg.carrepo.Services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ftg.carrepo.Models.DeleteResponse
import com.ftg.carrepo.Models.DownloadResponse
import com.ftg.carrepo.Models.DownloadingApiRequestData
import com.ftg.carrepo.R
import com.ftg.carrepo.RoomDB.MyDatabase
import com.ftg.carrepo.Screens.MainActivity
import com.ftg.carrepo.Utils.Constant.BASE_URL
import com.ftg.carrepo.Utils.Server
import com.ftg.carrepo.Utils.ServerCallInterceptor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundDownloadService : Service() {
    @Inject
    lateinit var interceptor: ServerCallInterceptor
    lateinit var notificationManager: NotificationManagerCompat
    private val channelId = "VK Enterprises"
    private val max = 100
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d("VK_Enterprises", "Service Created")
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d("VK_Enterprises", "Service Destroyed")
        job.cancel()
        super.onDestroy()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val database = MyDatabase.getDatabase(this)

            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Server::class.java)

            val notificationIntent = Intent(this, MainActivity::class.java)

            val pIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            val notification: NotificationCompat.Builder = NotificationCompat.Builder(
                this,
                channelId
            )
                .setSmallIcon(R.drawable.vk_logo)
                .setContentIntent(pIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setOngoing(true)
                .setProgress(max, 0, true)

            scope.launch {
                notification.setContentTitle("Clearing Garbage Records")
                startForeground(1, notification.build())

                // Using async to perform clearOfflineGarbage and downloadNewData concurrently
                val clearDeferred = async { clearOfflineGarbage(database, retrofit, notification, 0, 1) }
                val downloadDeferred = async { downloadNewData(database, retrofit, notification, 0, 1) }

                // Awaiting completion of both tasks
                clearDeferred.await()
                downloadDeferred.await()
            }
            return START_STICKY
        }catch (e: Exception){
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        return START_STICKY
    }

    private suspend fun clearOfflineGarbage(database: MyDatabase, retrofit: Server, notification: NotificationCompat.Builder, tableNo: Int, type: Int) {
        val start = database.getDao().getStartVehicleFromTable(tableNo)?.id ?: 0
        val end = database.getDao().getEndVehicleFromTable(tableNo)?.id ?: 0

        Log.d("VK_Enterprises", "Clearing Garbage Table No: $tableNo, Type: $type")

        if(tableNo == 0 && end == 0){
            Log.d("VK_Enterprises", "Clearing Garbage not required")

            notification.setContentTitle("Downloading")
            startForeground(1, notification.build())

            downloadNewData(database, retrofit, notification, 0, 1)
        }else if(type == 2 && tableNo > 99){
            downloadNewData(database, retrofit, notification, 0, 1)
        }else if(type == 1 && tableNo > 99){
            clearOfflineGarbage(database, retrofit, notification, 0, 2)
        }else{
            val response = retrofit.getRemainingVehicles(DownloadingApiRequestData(end, start, tableNo, type))
            response.enqueue(object: Callback<DeleteResponse>{
                override fun onResponse(
                    call: Call<DeleteResponse>,
                    response: Response<DeleteResponse>
                ) {
                    scope.launch {
                        if (response.code() == 200) {
                            val result = response.body()?.data
                            if (!result.isNullOrEmpty()){
                                val existingRecord = database.getDao().getVehiclesOfTable(tableNo)
                                existingRecord.forEach { existing ->
                                    var isPresent = false
                                    result.forEach {
                                        if(it?.id == existing.id)
                                            isPresent = true
                                    }
                                    if(!isPresent)
                                        database.getDao().deleteVehicle(existing.table_no!!, existing.id!!)
                                }

                                nextDeleteStep(database, retrofit, notification, tableNo, type)
                            }else{
                                Log.d("VK_Enterprises", "Clearing Garbage result is null or empty")
                                nextDeleteStep(database, retrofit, notification, tableNo, type)
                            }
                        } else {
                            Log.d("VK_Enterprises", "Clearing Garbage response code: ${response.code()}")
                            nextDeleteStep(database, retrofit, notification, tableNo, type)
                        }
                    }
                }

                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    if (t is SocketTimeoutException) {
                        scope.launch { clearOfflineGarbage(database, retrofit, notification, tableNo, type) }
                    }else{
                        Log.d("VK_Enterprises", "Clearing Garbage Failed: ${t.localizedMessage}")
                        scope.launch {
                            nextDeleteStep(database, retrofit, notification, tableNo, type)
                        }
                    }
                }
            })
        }
    }

    private suspend fun downloadNewData(database: MyDatabase, retrofit: Server, notification: NotificationCompat.Builder, tableNo: Int, type: Int) {
        val end = database.getDao().getEndVehicleFromTable(tableNo)?.id ?: 0

        Log.d("VK_Enterprises", "Downloading Table No: $tableNo, Type: $type")

        if(type == 2 && tableNo > 99){
            notification.setContentTitle("Download Completed")
            notification.setProgress(0, 0, false)
            notification.setOngoing(false)
            notification.setAutoCancel(true)
            startForeground(1, notification.build())
            job.cancel()
            stopSelf()
        }else if(type == 1 && tableNo > 99){
            downloadNewData(database, retrofit, notification, 0, 2)
        }else{
            val response = retrofit.downloadNewData(DownloadingApiRequestData(end, null, tableNo, type))
            response.enqueue(object: Callback<DownloadResponse>{
                override fun onResponse(
                    call: Call<DownloadResponse>,
                    response: Response<DownloadResponse>
                ) {
                    scope.launch {
                        if (response.code() == 200) {
                            val result = response.body()?.data
                            if (!result.isNullOrEmpty()){
                                database.getDao().addNewVehicles(result)
                                nextDownloadStep(database, retrofit, notification, tableNo, type)
                            }else{
                                Log.d("VK_Enterprises", "Downloading result is null or empty")
                                nextDownloadStep(database, retrofit, notification, tableNo, type)
                            }
                        } else {
                            Log.d("VK_Enterprises", "Downloading response code: ${response.code()}")
                            nextDownloadStep(database, retrofit, notification, tableNo, type)
                        }
                    }
                }

                override fun onFailure(call: Call<DownloadResponse>, t: Throwable) {
                    if (t is SocketTimeoutException) {
                        scope.launch { downloadNewData(database, retrofit, notification, tableNo, type) }
                    }else{
                        Log.d("VK_Enterprises", "Downloading Failed: ${t.localizedMessage}")
                        scope.launch {
                            nextDownloadStep(database, retrofit, notification, tableNo, type)
                        }
                    }
                }
            })
        }


    }

    private suspend fun nextDeleteStep(database: MyDatabase, retrofit: Server, notification: NotificationCompat.Builder, tableNo: Int, type: Int){
        if(tableNo == 99 && type == 2){
            Log.d("VK_Enterprises", "Clearing Garbage Complete")

            notification.setContentTitle("Downloading")
            startForeground(1, notification.build())

            downloadNewData(database, retrofit, notification, 0, 1)
        }else{
            if(tableNo == 99 && type == 1 ){
                clearOfflineGarbage(database, retrofit, notification, 0, 2)
            }else{
                clearOfflineGarbage(database, retrofit, notification, tableNo + 1, type)
            }
        }
    }

    private suspend fun nextDownloadStep(database: MyDatabase, retrofit: Server, notification: NotificationCompat.Builder, tableNo: Int, type: Int){
        if(tableNo == 99 && type == 2){
            Log.d("VK_Enterprises", "Downloading Complete")
            notification.setContentTitle("Download Completed")
            notification.setProgress(0, 0, false)
            notification.setOngoing(false)
            notification.setAutoCancel(true)
            startForeground(1, notification.build())
            job.cancel()
            stopSelf()
        }else{
            if(tableNo == 99 && type == 1 ){
                downloadNewData(database, retrofit, notification, 0, 2)
            }else{
                downloadNewData(database, retrofit, notification, tableNo + 1, type)
            }
        }
    }


}