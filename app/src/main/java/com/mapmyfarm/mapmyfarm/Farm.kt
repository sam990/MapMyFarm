package com.mapmyfarm.mapmyfarm

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "farm")
data class Farm(
    @PrimaryKey val id: String,
    val farmID: Int,
    var area: Double,
    val locale: String?,
    val coordinates: List<LatLng>,
    var landType: String?
) {
    constructor(): this (
        "",
        0,
        0.0,
        "",
        emptyList<LatLng>(),
        ""
    )

//    val timestamp = Date()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Farm

        if (id != other.id) return false
        if (farmID != other.farmID) return false
        if (area != other.area) return false
        if (locale != other.locale) return false
        if (landType != other.landType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + farmID.hashCode()
        result = 31 * result + area.hashCode()
        result = 31 * result + (locale?.hashCode() ?: 0)
        result = 31 * result + (landType?.hashCode() ?: 0)
        return result
    }
}