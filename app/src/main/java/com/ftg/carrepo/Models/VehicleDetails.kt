package com.ftg.carrepo.Models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity("MyOfflineDatabase")
data class VehicleDetails(
    @PrimaryKey(autoGenerate = true)
    val uniqueId: Int?,

    val area: String?,
    val bkt: String?,
    val branch: String?,
    val chassis_no: String?,
    val confirm: Int?,
    val contract_no: String?,
    val createDate: String?,
    val customer_name: String?,
    val engine_no: String?,
    val ex_name: String?,
    val financeCode: Int?,
    val financeName: String?,
    val financer: String?,
    val gv: String?,
    val headOfficeCode: Int?,
    val headOfficeId: Int?,
    val id: Int?,
    val level1: String?,
    val level1con: String?,
    val level2: String?,
    val level2con: String?,
    val level3: String?,
    val level3con: String?,
    val level4: String?,
    val level4con: String?,
    val mek_and_model: String?,
    val od: String?,
    val poss: String?,
    val rc_no: String?,
    val region: String?,
    val ses17: String?,
    val ses9: String?,
    val table_no: Int?,
    val tbr: String?,
    val total_records: Int?,
    val type: Int?,
    val updateDate: String?,
    val upload_time: String?,
    val user1: String?,
    val user1_mobile: String?,
    val user2: String?,
    val user2_mobile: String?,
    val user3: String?,
    val user3_mobile: String?,
    val vehicleId: Int?,

    val _id: String,
    val updatedAt: String,
    val is_released: String,
    val branch_id: String,

)