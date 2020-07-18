package com.mapmyfarm.mapmyfarm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmMapping")
data class FarmMapping (
    @PrimaryKey val farmID: String,
    val harvestID: String
)