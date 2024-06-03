package com.ftg.carrepo.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ftg.carrepo.Models.VehicleDetails

@Database(entities = [VehicleDetails::class], version = 1, exportSchema = false)
abstract class MyDatabase: RoomDatabase() {
    abstract fun getDao(): RoomDao
    companion object{
        private var INSTANCE: MyDatabase? = null
        fun getDatabase(context: Context): MyDatabase{
            if (INSTANCE==null){
                INSTANCE = Room.databaseBuilder(
                    context,
                    MyDatabase::class.java,
                    "MyOfflineDatabase"
                ).fallbackToDestructiveMigration().build()
            }
            return INSTANCE!!
        }
    }
}