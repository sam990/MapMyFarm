package com.mapmyfarm.mapmyfarm

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.mapmyfarm.mapmyfarm.FarmsDataStore.farmMap
import com.mapmyfarm.mapmyfarm.FarmsDataStore.harvestMap
import java.lang.RuntimeException
import java.util.*

@Entity(tableName = "farmHarvest",
    foreignKeys = [
        ForeignKey(
            entity = Farm::class,
            parentColumns = ["id"],
            childColumns = ["farmID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FarmHarvest(
    @PrimaryKey val farmID: String,
    val harvestIDs: List<String>
) {

    @Ignore var farm = farmMap[farmID] ?: run {
        Log.e("FarmHarvest", "FarmID: $farmID not found" )
        Farm()
    }

    @Ignore var harvests = harvestIDs.mapNotNull { harvestMap[it] }


    fun refreshFarm() {
        farm = farmMap[farmID] ?: run {
            Log.e("FarmHarvest", "FarmID: $farmID not found" )
            farm
        }
    }

    fun refreshHarvests() {
        harvests = harvestIDs.mapNotNull { harvestMap[it] }
    }

    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FarmHarvest>() {

            override fun areItemsTheSame(oldItem: FarmHarvest, newItem: FarmHarvest): Boolean {
                return oldItem.farmID == newItem.farmID
            }

            override fun areContentsTheSame(oldItem: FarmHarvest, newItem: FarmHarvest): Boolean {
                if (oldItem.farm != newItem.farm) return false
                if (oldItem.harvests.size != newItem.harvests.size) return false
                for (i in oldItem.harvests.indices) {
                    if (oldItem.harvests[i].crop != newItem.harvests[i].crop
                        || oldItem.harvests[i].sowingDate != newItem.harvests[i].sowingDate
                    ) return false
                }
                return true
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FarmHarvest

        if (farmID != other.farmID) return false
        if (harvestIDs.size != other.harvestIDs.size) return false

        for (i in harvestIDs.indices) {
            if (harvestIDs[i] != other.harvestIDs[i]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = farmID.hashCode()
        result = 31 * result + harvestIDs.hashCode()
        return result
    }

}