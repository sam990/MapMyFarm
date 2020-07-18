package com.mapmyfarm.mapmyfarm

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mapmyfarm.mapmyfarm.FarmsDataStore.farmMap
import com.mapmyfarm.mapmyfarm.FarmsDataStore.harvestMap

@Entity(tableName = "farmHarvest")
data class FarmHarvest(
    @PrimaryKey val farmID: String,
    var harvestIDs: ArrayList<String>
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FarmHarvest>(){
            override fun areItemsTheSame(oldItem: FarmHarvest, newItem: FarmHarvest): Boolean {
                return oldItem.farmID == newItem.farmID
            }

            override fun areContentsTheSame(oldItem: FarmHarvest, newItem: FarmHarvest): Boolean {
                if (oldItem.farmID != newItem.farmID) return false
                if (farmMap[oldItem.farmID]?.timestamp != farmMap[newItem.farmID]?.timestamp) return false
                if (oldItem.harvestIDs.size != newItem.harvestIDs.size) return false
                for (i in oldItem.harvestIDs.indices) {
                    if (harvestMap[oldItem.harvestIDs[i]]?.timestamp != harvestMap[newItem.harvestIDs[i]]?.timestamp) return false
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