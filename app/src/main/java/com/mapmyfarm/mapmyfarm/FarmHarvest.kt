package com.mapmyfarm.mapmyfarm

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FarmHarvest(
    @PrimaryKey val farm: Farm,
    val harvests: List<Harvest>
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FarmHarvest>(){
            override fun areItemsTheSame(oldItem: FarmHarvest, newItem: FarmHarvest): Boolean {
                return oldItem.farm.id == newItem.farm.id
            }

            override fun areContentsTheSame(oldItem: FarmHarvest, newItem: FarmHarvest): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FarmHarvest

        if (farm != other.farm) return false
        if (harvests.size != other.harvests.size) return false

        for (i in harvests.indices) {
            if (harvests[i] != other.harvests[i]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = farm.hashCode()
        result = 31 * result + harvests.hashCode()
        return result
    }

}