package com.ftg.carrepo.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ftg.carrepo.Models.SearchedVehicleDetails
import com.ftg.carrepo.Models.VehicleDetails

@Dao
interface RoomDao {
    @Query("DELETE FROM MyOfflineDatabase")
    suspend fun deleteFullRecord()

    @Query("SELECT * FROM MyOfflineDatabase WHERE table_no = :tableNo ORDER BY id LIMIT 1")
    suspend fun getStartVehicleFromTable(tableNo: Int): VehicleDetails?

    @Query("SELECT * FROM MyOfflineDatabase WHERE table_no = :tableNo ORDER BY id DESC LIMIT 1")
    suspend fun getEndVehicleFromTable(tableNo: Int): VehicleDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewVehicles(list: List<VehicleDetails?>)

    @Query("SELECT COUNT(uniqueId) FROM MyOfflineDatabase")
    suspend fun getOfflineRecordCount() : Int

    @Query("SELECT * FROM MyOfflineDatabase WHERE table_no = :tableNo AND id = :vehicleId")
    suspend fun getVehicleDetails(tableNo: Int, vehicleId: Int): VehicleDetails?


    @Query("Delete FROM MyOfflineDatabase WHERE table_no = :tableNo AND id = :vehicleId")
    suspend fun deleteVehicle(tableNo: Int,vehicleId: Int)

    @Query("SELECT * FROM MyOfflineDatabase")
    suspend fun getAllVehicles(): List<VehicleDetails>

    @Query("SELECT chassis_no, engine_no, id, mek_and_model, rc_no, table_no, type FROM MyOfflineDatabase WHERE type = 1 AND rc_no LIKE :rcNumber")
    suspend fun searchWithRc(rcNumber: String): List<SearchedVehicleDetails>?

    @Query("SELECT chassis_no, engine_no, id, mek_and_model, rc_no, table_no, type FROM MyOfflineDatabase WHERE type = 2 AND chassis_no LIKE :chassisNo")
    suspend fun searchWithChassis(chassisNo: String): List<SearchedVehicleDetails>?

    @Query("SELECT * FROM MyOfflineDatabase WHERE table_no = :tableNo")
    suspend fun getVehiclesOfTable(tableNo: Int): List<VehicleDetails>
}