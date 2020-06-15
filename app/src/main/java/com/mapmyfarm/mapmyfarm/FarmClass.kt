package com.mapmyfarm.mapmyfarm

import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList

data class FarmClass(
    val id: String?,
    val area: Double?,
    val farmID: String?,
    val sowingDate: Date?,
    val latList: ArrayList<Double>?,
    val lngList: ArrayList<Double>?,
    val crop: String?,
    val plantingMode: String?,
    val weedingMode: String?,
    val harvestCuttingMode: String?,
    val fertilizer: String?,
    val pesticide: String?,
    val landType: String?,
    val locale: String?,
    val prevSeedPieces: Int?,
    val prevSeedPrice: Int?,
    val prevLabourNum: Int?,
    val prevLabourDays: Int?,
    val prevLabourCharge: Int?,
    val prevMachineryCharge: Int?,
    val prevFertilizerPieces: Int?,
    val prevFertilizerPrice: Int?,
    val prevPesticidePieces: Int?,
    val prevPesticidePrice: Int?,
    val comment: String?
)  {

    val coordinates : ArrayList<LatLng>

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FarmClass>(){
            override fun areItemsTheSame(oldItem: FarmClass, newItem: FarmClass): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FarmClass, newItem: FarmClass): Boolean {
                return oldItem == newItem
            }

        }
    }

    init {
        val count: Int = latList?.size ?: 0
        coordinates = ArrayList()
        for (i in 0 until count) {
            coordinates.add(LatLng(latList!![i], lngList!![i]))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FarmClass

        if (id != other.id) return false
        if (area != other.area) return false
        if (farmID != other.farmID) return false
        if (sowingDate != other.sowingDate) return false
        if (crop != other.crop) return false
        if (plantingMode != other.plantingMode) return false
        if (weedingMode != other.weedingMode) return false
        if (harvestCuttingMode != other.harvestCuttingMode) return false
        if (fertilizer != other.fertilizer) return false
        if (pesticide != other.pesticide) return false
        if (landType != other.landType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + area.hashCode()
        result = 31 * result + farmID.hashCode()
        result = 31 * result + sowingDate.hashCode()
        result = 31 * result + crop.hashCode()
        result = 31 * result + plantingMode.hashCode()
        result = 31 * result + weedingMode.hashCode()
        result = 31 * result + harvestCuttingMode.hashCode()
        result = 31 * result + fertilizer.hashCode()
        result = 31 * result + pesticide.hashCode()
        result = 31 * result + landType.hashCode()
        return result
    }


}