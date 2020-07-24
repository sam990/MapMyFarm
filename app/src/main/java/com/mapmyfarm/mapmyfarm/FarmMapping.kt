package com.mapmyfarm.mapmyfarm

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "farmMapping", primaryKeys = ["farmID", "harvestID"],

foreignKeys = [
    ForeignKey(entity = Farm::class,
        parentColumns = ["id"],
        childColumns = ["farmID"],
        onDelete = ForeignKey.CASCADE),

    ForeignKey(entity = Harvest::class,
        parentColumns = ["id"],
        childColumns = ["harvestID"],
        onDelete = ForeignKey.CASCADE)
],
    indices = [Index(value = ["farmID"]), Index(value = ["harvestID"])]

)
data class FarmMapping (
    val farmID: String,
    val harvestID: String
)