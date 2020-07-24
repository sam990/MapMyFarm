package com.mapmyfarm.mapmyfarm

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "harvest")
data class Harvest(
    @PrimaryKey val id: String,
    val crop: String?,
    val sowingDate: Date,
    val seedBrand: String?,
    val plantingMode: String?,
    val weedingMode: String?,
    val harvestCuttingMode: String?,
    val fertilizer: String?,
    val pesticide: String?,
    val prevSeedPackets: Int?,
    val prevSeedPrice: Int?,
    val prevLabourNum: Int?,
    val prevLabourDays: Int?,
    val prevLabourCharge: Int?,
    val prevMachineryCharge: Int?,
    val prevFertilizerPackets: Int?,
    val prevFertilizerPrice: Int?,
    val prevPesticidePackets: Int?,
    val prevPesticidePrice: Int?,
    val comment: String?
)