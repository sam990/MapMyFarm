package com.mapmyfarm.mapmyfarm

import androidx.room.*

@Dao
interface FarmDao {
    @Query("SELECT * FROM farm")
    fun getAll(): List<Farm>

    @Query("SELECT * FROM farm WHERE id = :userID")
    fun loadById(userID: String): Farm

    @Query("SELECT * FROM farm WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: List<String>): List<Farm>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg farm: Farm)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(farms: List<Farm>)

    @Update
    fun updateFarms(vararg farm: Farm)

    @Delete
    fun delete(farm: Farm)

    @Query("DELETE FROM farm")
    fun deleteAll()

    @Query("DELETE FROM farm WHERE id = :delID")
    fun deleteByID(delID: String)

}


@Dao
interface HarvestDao {
    @Query("SELECT * FROM harvest")
    fun getAll(): List<Harvest>

    @Query("SELECT * FROM harvest WHERE id = :userID")
    fun loadById(userID: String): Harvest

    @Query("SELECT * FROM harvest WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: List<String>): List<Harvest>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg harvest: Harvest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(harvests: List<Harvest>)

    @Update
    fun updateHarvests(vararg harvest: Harvest)

    @Delete
    fun delete(harvest: Harvest)

    @Query("DELETE FROM harvest")
    fun deleteAll()

    @Query("DELETE FROM harvest WHERE id = :delID")
    fun deleteByID(delID: String)
}

@Dao
interface FarmMappingDao {
    @Query("SELECT * FROM farmMapping")
    fun getAll(): List<FarmMapping>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg farmMapping: FarmMapping)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(farmMappings: List<FarmMapping>)

    @Update
    fun updateFarmMappings(vararg farmMapping: FarmMapping)

    @Delete
    fun delete(farmMapping: FarmMapping)

    @Query("DELETE FROM farmMapping")
    fun deleteAll()

    @Query("DELETE FROM farmMapping WHERE farmID = :farmID AND harvestID = :harvestID")
    fun deleteByID(farmID: String, harvestID: String)
}

@Dao
interface FarmHarvestDao {
    @Query("SELECT * FROM farmHarvest")
    fun getAll(): List<FarmHarvest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg farmHarvest: FarmHarvest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(farmHarvests: List<FarmHarvest>)

    @Update
    fun updateFarmHarvests(vararg farmHarvest: FarmHarvest)

    @Delete
    fun delete(farmHarvest: FarmHarvest)

    @Query("DELETE FROM farmHarvest")
    fun deleteAll()

    @Query("DELETE FROM farmHarvest WHERE farmID = :farmID")
    fun deleteByID(farmID: String)
}

