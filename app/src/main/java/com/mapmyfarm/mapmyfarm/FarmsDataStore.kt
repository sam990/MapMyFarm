package com.mapmyfarm.mapmyfarm


import android.content.Context
import android.util.Log
import androidx.room.Room
import com.amazonaws.amplify.generated.graphql.*
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.android.gms.maps.model.LatLng
import com.mapmyfarm.mapmyfarm.AppSyncClientFactory.appSyncClient
import type.*
import type.FarmInput
import type.HarvestInput
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.Nonnull
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread


typealias DataStoreOperationCallback = (Boolean) -> Unit
typealias CreateEntityCallback = (String?) -> Unit


object FarmsDataStore {

    @Volatile var farmsHarvests: MutableList<FarmHarvest> = ArrayList()
    @Volatile var farmMap = HashMap<String, Farm>()
    @Volatile var harvestMap = HashMap<String, Harvest>()
    @Volatile var farmMappings = ArrayList<FarmMapping>()
//    @Volatile var farms = ArrayList<Farm>()
//    @Volatile var harvests = ArrayList<Harvest>()

    private val sdf = SimpleDateFormat("yyyy-MM-ddZZZZZ", Locale.ENGLISH)
    private const val TAG = "FarmsDataStore"

    lateinit var db : FarmDatabase

    fun initialiseRoomDB(context: Context) {
        db = Room.databaseBuilder(context.applicationContext, FarmDatabase::class.java, "Farms.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Synchronized fun refreshRoomDB(callback: DataStoreOperationCallback) {
        thread {
            farmMap.clear()
            farmsHarvests.clear()
            harvestMap.clear()

            val farms = ArrayList(db.farmDao().getAll())
            for (farm in farms) {
                farmMap[farm.id] = farm
            }

            val harvests = ArrayList(db.harvestDao().getAll())
            for (harvest in harvests) {
                harvestMap[harvest.id] = harvest
            }

            farmMappings = ArrayList(db.farmMappingDao().getAll())

            farmsHarvests = ArrayList(db.farmHarvestDao().getAll())

            callback(true)
        }
    }

    fun clearDB(callback: DataStoreOperationCallback) {
        thread {
            db.clearAllTables()
            callback(true)
        }
    }


    fun refreshDataNetwork(callback: DataStoreOperationCallback) {
        networkFetch { res ->
            if(!res) {
                callback(false)
                return@networkFetch
            }

            buildFarmHarvests { callback(it) }
        }
    }

    fun networkFetch(callback: DataStoreOperationCallback) {

        networkFetchFarms {resFarm->
            if(!resFarm) {
                callback(false)
            } else {
                networkFetchHarvests {resHarvest->
                    if(!resHarvest) {
                        callback(false)
                    } else {
                        networkFetchMappings {resMapping->
                            if(!resMapping) {
                                callback(false)
                            } else {
                                callback(true)
                            }
                        }
                    }

                }
            }
        }

    }

    fun networkFetchMappings(callback: DataStoreOperationCallback) {
        appSyncClient()?.query(ListUserFarmMappingsQuery.builder().build())?.responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
            ?.enqueue(
                object : GraphQLCall.Callback<ListUserFarmMappingsQuery.Data?>() {
                    override fun onFailure(e: ApolloException) {
                        Log.e(TAG, e.toString())
                        callback(false)
                    }

                    override fun onResponse(response: Response<ListUserFarmMappingsQuery.Data?>) {
                        val items: List<ListUserFarmMappingsQuery.Item>? = response.data()?.listUserFarmMappings()?.items()
                        farmMappings.clear()
                        if (items != null) {
                            for (item in items) {
                                farmMappings.add(parseFarmMapping(item))
                            }
                        }
                        db.farmMappingDao().deleteAll()
                        db.farmMappingDao().insertAll(farmMappings)
                        callback(true)
                    }

                }
            ) ?: callback(false)
    }

    fun networkFetchFarms(callback: DataStoreOperationCallback) {
        appSyncClient()?.query(ListUserFarmsQuery.builder().build())?.responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
            ?.enqueue(
                object : GraphQLCall.Callback<ListUserFarmsQuery.Data?>() {
                    override fun onFailure(e: ApolloException) {
                        Log.e(TAG, e.toString())
                        callback(false)
                    }

                    override fun onResponse(response: Response<ListUserFarmsQuery.Data?>) {
                        val items: List<ListUserFarmsQuery.Item>? = response.data()?.listUserFarms()?.items()
                        val farms = ArrayList<Farm>()
                        if (items != null) {
                            for (item in items) {
                                farms.add(parseFarm(item))
                            }
                        }
                        db.farmDao().deleteAll()
                        db.farmDao().insertAll(farms)
                        farmMap.clear()
                        for (farm in farms) {
                            farmMap[farm.id] = farm
                        }
                        callback(true)
                    }

                }
            ) ?: callback(false)
    }

    fun networkFetchHarvests(callback: DataStoreOperationCallback) {
        appSyncClient()?.query(ListUserHarvestsQuery.builder().build())?.responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
            ?.enqueue(
                object : GraphQLCall.Callback<ListUserHarvestsQuery.Data?>() {
                    override fun onFailure(e: ApolloException) {
                        Log.e(TAG, e.toString())
                        callback(false)
                    }

                    override fun onResponse(response: Response<ListUserHarvestsQuery.Data?>) {
                        val items: List<ListUserHarvestsQuery.Item>? = response.data()?.listUserHarvests()?.items()
                        val harvests = ArrayList<Harvest>()
                        if (items != null) {
                            for (item in items) {
                                harvests.add(parseHarvest(item))
                            }
                        }
                        db.harvestDao().deleteAll()
                        db.harvestDao().insertAll(harvests)
                        harvestMap.clear()
                        for (harvest in harvests) {
                            harvestMap[harvest.id] = harvest
                        }
                        callback(true)
                    }

                }
            ) ?: callback(false)
    }


    fun addFarm(
        area: Double,
        locale: String?,
        coordinates: List<LatLng>,
        landType: String?,
        callback: CreateEntityCallback
    ) {
        val (lat, lng) = getDoubleLatLngList(coordinates)
        appSyncClient()?.mutate(
                CreateFarmMutation.builder()
                    .input(
                        FarmInput.builder()
                            .area(area)
                            .locale(locale)
                            .coordinatesLat(lat)
                            .coordinatesLng(lng)
                            .land_type(landType)
                            .build()
                    ).build()
            )?.enqueue(
                object: GraphQLCall.Callback<CreateFarmMutation.Data?>() {
                    override fun onFailure(e: ApolloException) {
                        Log.e(TAG, e.toString())
                        callback(null)
                    }

                    override fun onResponse(response: Response<CreateFarmMutation.Data?>) {
                        response.data()?.createFarm()?.let { res ->
                            val farm = parseFarm(res)
                            db.farmDao().insert(farm)
                            farmMap[farm.id] = farm
                        }
                        callback(response.data()?.createFarm()?.id())
                    }

                }
            ) ?: callback(null)
    }


    fun addHarvest(
        crop: String,
        sowingDate: Date,
        seedBrand: String?,
        plantingMode: String?,
        weedingMode: String?,
        harvestCuttingMode: String?,
        fertilizer: String?,
        pesticide: String?,
        prevSeedPieces: Int?,
        prevSeedPrice: Int?,
        prevLabourNum: Int?,
        prevLabourDays: Int?,
        prevLabourCharge: Int?,
        prevMachineryCharge: Int?,
        prevFertilizerPieces: Int?,
        prevFertilizerPrice: Int?,
        prevPesticidePieces: Int?,
        prevPesticidePrice: Int?,
        comment: String?,
        callback: CreateEntityCallback
    ) {
        val qCallback = object : GraphQLCall.Callback<CreateHarvestMutation.Data?>() {

                override fun onResponse(@Nonnull response: Response<CreateHarvestMutation.Data?>) {
                    response.data()?.createHarvest()?.let {
                        val harvest = parseHarvest(it)
                        harvestMap[harvest.id] = harvest
                        db.harvestDao().insert(harvest)
                    }
                    callback(response.data()?.createHarvest()?.id())
                }

                override fun onFailure(@Nonnull e: ApolloException) {
                    Log.e(TAG, e.toString())
                    callback(null)
                }
            }
        appSyncClient()?.mutate(
            CreateHarvestMutation.builder()
                .input(
                    HarvestInput.builder()
                        .crop(crop)
                        .seed_brand(seedBrand)
                        .sowing_date(sdf.format(sowingDate))
                        .planting_mode(plantingMode)
                        .weeding_mode(weedingMode)
                        .harvest_cutting_mode(harvestCuttingMode)
                        .fertilizer(fertilizer)
                        .pesticide(pesticide)
                        .prev_seed_packets(prevSeedPieces)
                        .prev_seed_price(prevSeedPrice)
                        .prev_num_labour(prevLabourNum)
                        .prev_labour_days(prevLabourDays)
                        .prev_labour_charge(prevLabourCharge)
                        .prev_machine_charge(prevMachineryCharge)
                        .prev_fertilizer_packets(prevFertilizerPieces)
                        .prev_fertilizer_charge(prevFertilizerPrice)
                        .prev_pesticide_packets(prevPesticidePieces)
                        .prev_pesticide_charge(prevPesticidePrice)
                        .comments(comment)
                        .build()
                )
                .build()
        )?.enqueue(qCallback) ?: callback(null)
    }

    fun addFarmMapping(farmID: String, harvestID: String, callback: DataStoreOperationCallback) {
        appSyncClient()?.mutate(
            CreateFarmMappingMutation.builder()
                .input(
                    FarmMappingInput.builder()
                        .farmID(farmID)
                        .harvestID(harvestID)
                        .build()
                ).build()
        )?.enqueue(
            object: GraphQLCall.Callback<CreateFarmMappingMutation.Data?>() {
                override fun onFailure(e: ApolloException) {
                    Log.e(TAG, e.toString())
                    callback(false)
                }

                override fun onResponse(response: Response<CreateFarmMappingMutation.Data?>) {
                    val mapping = FarmMapping(farmID, harvestID)
                    // add to database
                    db.farmMappingDao().insert(mapping)
                    // add to harvest
                    farmMappings.add(mapping)

                    var found = false

                    farmsHarvests = ArrayList(
                        farmsHarvests.map {
                            if (it.farmID == farmID) {
                                found = true
                                val harvest = FarmHarvest(it.farmID, it.harvestIDs + listOf(harvestID))
                                // update
                                db.farmHarvestDao().updateFarmHarvests(harvest)
                                harvest
                            } else {
                                it
                            }
                        }
                    )

                    // if first item then add it
                    if (!found) {
                        val harvest = FarmHarvest(farmID, listOf(harvestID))
                        db.farmHarvestDao().insert(harvest)
                        farmsHarvests.add(harvest)
                    }

                    callback(true)
                }

            }
        ) ?: callback(false)
    }


    fun updateFarm(
        id: String,
        farmID: Int?,
        area: Double?,
        locale: String?,
        landType: String?,
        callback: DataStoreOperationCallback
    ) {
        val builder = UpdateFarmInput.builder()
        builder.id(id)

        farmID?.let { builder.farmID(it) }
        area?.let { builder.area(it) }
        locale?.let { builder.locale(it) }
        landType?.let { builder.land_type(it) }

        appSyncClient()?.let {
            it.mutate(
                UpdateFarmMutation.builder()
                    .input(
                        builder.build()
                    )
                    .build()
            ).enqueue(
                object: GraphQLCall.Callback<UpdateFarmMutation.Data?>() {
                    override fun onFailure(e: ApolloException) {
                        Log.e(TAG, e.toString())
                        callback(false)
                    }

                    override fun onResponse(response: Response<UpdateFarmMutation.Data?>) {
                        response.data()?.updateFarm()?.let { res ->
                            val farm = parseFarm(res)
                            db.farmDao().updateFarms(farm)
                            farmMap[farm.id] = farm
                            farmsHarvests.forEach { item-> item.refreshFarm() }
                        }
                        callback(response.data() != null)
                    }

                }
            )
        } ?: callback(false)
    }

    fun updateHarvest(
        id: String,
        crop: String?,
        sowingDate: Date?,
        seedBrand: String?,
        plantingMode: String?,
        weedingMode: String?,
        harvestCuttingMode: String?,
        fertilizer: String?,
        pesticide: String?,
        prevSeedPieces: Int?,
        prevSeedPrice: Int?,
        prevLabourNum: Int?,
        prevLabourDays: Int?,
        prevLabourCharge: Int?,
        prevMachineryCharge: Int?,
        prevFertilizerPieces: Int?,
        prevFertilizerPrice: Int?,
        prevPesticidePieces: Int?,
        prevPesticidePrice: Int?,
        comment: String?,
        callback: DataStoreOperationCallback
    ) {
        val builder = UpdateHarvestInput.builder()
        builder.id(id)

        with(builder) {
            crop?.let { crop(it) }
            sowingDate?.let { sowing_date(sdf.format(it)) }
            seedBrand?.let { seed_brand(it) }
            plantingMode?.let { planting_mode(it) }
            weedingMode?.let { weeding_mode(it) }
            harvestCuttingMode?.let { harvest_cutting_mode(it) }
            fertilizer?.let { fertilizer(it) }
            pesticide?.let { pesticide(it) }
            prevSeedPieces?.let { prev_seed_packets(it) }
            prevSeedPrice?.let { prev_seed_price(it) }
            prevLabourNum?.let { prev_num_labour(it) }
            prevLabourDays?.let { prev_labour_days(it) }
            prevLabourCharge?.let { prev_labour_charge(it) }
            prevMachineryCharge?.let { prev_machine_charge(it) }
            prevFertilizerPieces?.let { prev_fertilizer_packets(it) }
            prevFertilizerPrice?.let { prev_fertilizer_charge(it) }
            prevPesticidePieces?.let { prev_pesticide_packets(it) }
            prevPesticidePrice?.let { prev_pesticide_charge(it) }
            comment?.let { comments(it) }
        }

        // update
        appSyncClient()?.mutate(
            UpdateHarvestMutation.builder()
                .input(builder.build())
                .build()
        )?.enqueue(
            object: GraphQLCall.Callback<UpdateHarvestMutation.Data?>() {
                override fun onFailure(e: ApolloException) {
                    Log.e(TAG, e.toString())
                    callback(false)
                }

                override fun onResponse(response: Response<UpdateHarvestMutation.Data?>) {
                    response.data()?.updateHarvest()?.let {
                        val harvest = parseHarvest(it)
                        db.harvestDao().updateHarvests(harvest)
                        harvestMap[harvest.id] = harvest
                        // refresh all
                        farmsHarvests.forEach { item -> item.refreshHarvests() }
                    }
                    callback(response.data() != null)
                }

            }
        ) ?: callback(false)
    }


    fun deleteFarm(id: String, callback: DataStoreOperationCallback) {
        // delete all
        deleteMappingsWithFarm(id) {
            if (!it) {
                callback(false)
            }
            else {
                // delete the farm
                appSyncClient()?.mutate(
                    DeleteFarmMutation.builder()
                        .input(DeleteFarmInput.builder()
                            .id(id)
                            .build()
                        ).build()
                )?.enqueue(
                    object: GraphQLCall.Callback<DeleteFarmMutation.Data?>() {
                        override fun onFailure(e: ApolloException) {
                            Log.e(TAG, e.toString())
                            callback(false)
                        }

                        override fun onResponse(response: Response<DeleteFarmMutation.Data?>) {
                            // delete local
                            db.farmDao().deleteByID(id)
                            farmMap.remove(id)
                            // load mappings
                            farmMappings = ArrayList(db.farmMappingDao().getAll())
                            // delete farmHarvests
                            farmsHarvests = ArrayList(farmsHarvests.filter { item -> item.farmID != id })
//                        db.farmHarvestDao().deleteByID(id)
                            callback(true)
                        }
                    }
                ) ?: callback(false)
            }

        }
    }


    fun deleteHarvest(id: String, callback: DataStoreOperationCallback) {
        // delete mappings
        deleteMappingsWithHarvest(id) {
            if (!it) {
                callback(false)
                return@deleteMappingsWithHarvest
            }
            else {
                appSyncClient()?.mutate(
                    DeleteHarvestMutation.builder()
                        .input(
                            DeleteHarvestInput.builder()
                                .id(id)
                                .build()
                        )
                        .build()
                )?.enqueue(
                    object: GraphQLCall.Callback<DeleteHarvestMutation.Data?>() {
                        override fun onFailure(e: ApolloException) {
                            // log
                            Log.e(TAG, e.toString())
                            callback(false)
                        }

                        override fun onResponse(response: Response<DeleteHarvestMutation.Data?>) {
                            // delete from local database
                            db.harvestDao().deleteByID(id)
                            // delete from Map
                            harvestMap.remove(id)
                            farmMappings = ArrayList(db.farmMappingDao().getAll())
                            // delete from FarmHarvests
                            farmsHarvests = ArrayList(farmsHarvests.map { fh->
                                if (fh.harvestIDs.contains(id)) {
                                    val nFH = FarmHarvest(fh.farmID, fh.harvestIDs.filter { hID-> hID != id })
                                    db.farmHarvestDao().updateFarmHarvests(nFH)
                                    nFH
                                } else {
                                    fh
                                }
                            })

                            callback(true)
                        }

                    }
                ) ?: callback(false)
            }
        }
    }

    fun deleteFarmMapping(farmID: String, harvestID: String, callback: DataStoreOperationCallback) {
        appSyncClient()?.mutate(
            DeleteFarmMappingMutation.builder()
                .input(
                    DeleteFarmMappingInput.builder()
                        .farmID(farmID)
                        .harvestID(harvestID)
                        .build()
                ).build()
        )?.enqueue(
            object: GraphQLCall.Callback<DeleteFarmMappingMutation.Data?>() {
                override fun onFailure(e: ApolloException) {
                    Log.e(TAG, e.toString())
                    callback(false)
                }

                override fun onResponse(response: Response<DeleteFarmMappingMutation.Data?>) {
                    // delete local
                    db.farmMappingDao().deleteByID(farmID, harvestID)
                    // refresh farmHarvest

                    farmsHarvests = ArrayList(farmsHarvests.map {
                        if (it.farmID == farmID) {
                            FarmHarvest(it.farmID, it.harvestIDs.filter { id -> id != harvestID })
                        } else {
                            it
                        }
                    })

                    callback(true)
                }

            }
        ) ?: callback(false)
    }

    fun deleteMappingsWithFarm(id: String, callback: DataStoreOperationCallback) {
        // get all  mappings with given id
        thread {
            val ids = ArrayList<Pair<String, String>>()
            for (mapping in farmMappings) {
                if(mapping.farmID == id) {
                    ids.add(Pair(mapping.farmID, mapping.harvestID))
                }
            }

            // delete mappings
            val chunks = ids.chunked(25)
            var res = true
            var count = 0
            for (chunk in chunks) {
                // build input
                val delIds = chunk.map { DeleteFarmMappingInput.builder().farmID(it.first).harvestID(it.second).build() }
                batchDeleteFarmMappings(delIds) {
                    res = res && it
                    count++
                    if (count == chunks.size) {
                        callback(res)
                    }
                }
            }
        }
    }

    fun deleteMappingsWithHarvest(id: String, callback: DataStoreOperationCallback) {
        // get all  mappings with given id
        thread {
            val ids = ArrayList<Pair<String, String>>()
            for (mapping in farmMappings) {
                if(mapping.harvestID == id) {
                    ids.add(Pair(mapping.farmID, mapping.harvestID))
                }
            }

            // delete mappings
            val chunks = ids.chunked(25)
            var res = true
            var count = 0
            for (chunk in chunks) {
                // build input
                val delIds = chunk.map { DeleteFarmMappingInput.builder().farmID(it.first).harvestID(it.second).build() }
                batchDeleteFarmMappings(delIds) {
                    res = res && it
                    count++
                    if (count == chunks.size) {
                        callback(res)
                    }
                }
            }
        }
    }


    fun batchDeleteFarmMappings(ids: List<DeleteFarmMappingInput>, callback: DataStoreOperationCallback) {
        appSyncClient()?.mutate(
            BatchDeleteFarmMappingsMutation.builder()
                .input(ids)
                .build()
        )?.enqueue(
            object: GraphQLCall.Callback<BatchDeleteFarmMappingsMutation.Data?>() {
                override fun onFailure(e: ApolloException) {
                    Log.e(TAG, e.toString())
                    callback(false)
                }

                override fun onResponse(response: Response<BatchDeleteFarmMappingsMutation.Data?>) {
                    if (response.data()?.batchDeleteFarmMappings()?.size != ids.size) {
                        // not deleted some values
                        Log.e(TAG, "Not Deleted: ${response.errors().joinToString()}")
                    }
                    callback(true)
                }

            }

        ) ?: callback(false)
    }


    fun getDoubleLatLngList(points: List<LatLng>?): Pair<ArrayList<Double>, ArrayList<Double>> {
        val lat: ArrayList<Double> = ArrayList()
        val lng: ArrayList<Double> = ArrayList()
        if(points == null){
            return Pair(lat, lng)
        }
        for (latLng in points) {
            lat.add(latLng.latitude)
            lng.add(latLng.longitude)
        }
        return Pair(lat, lng)
    }

    fun doubleToLatLngList(latList: List<Double>?, lngList: List<Double>?): ArrayList<LatLng> {
        val count: Int = latList?.size ?: 0
        val coordinates = ArrayList<LatLng>()
        for (i in 0 until count) {
            coordinates.add(LatLng(latList!![i], lngList!![i]))
        }
        return coordinates
    }



    fun parseFarmMapping(item: ListUserFarmMappingsQuery.Item): FarmMapping {
        return FarmMapping(
            item.farmID(),
            item.harvestID()
        )
    }

    fun parseFarm(item: ListUserFarmsQuery.Item): Farm {
        val coors = doubleToLatLngList(item.coordinatesLat(), item.coordinatesLng())
        return Farm(
            item.id(),
            item.farmID(),
            item.area(),
            item.locale(),
            coors,
            item.land_type()
        )
    }

    fun parseFarm(item: CreateFarmMutation.CreateFarm): Farm {
        val coors = doubleToLatLngList(item.coordinatesLat(), item.coordinatesLng())
        return Farm(
            item.id(),
            item.farmID(),
            item.area(),
            item.locale(),
            coors,
            item.land_type()
        )
    }

    fun parseFarm(item: UpdateFarmMutation.UpdateFarm): Farm {
        val coors = doubleToLatLngList(item.coordinatesLat(), item.coordinatesLng())
        return Farm(
            item.id(),
            item.farmID(),
            item.area(),
            item.locale(),
            coors,
            item.land_type()
        )
    }



    fun parseHarvest(item: ListUserHarvestsQuery.Item): Harvest {
        return Harvest(
            item.id(),
            item.crop(),
            sdf.parse(item.sowing_date()) ?: Date(),
            item.seed_brand(),
            item.planting_mode(),
            item.weeding_mode(),
            item.harvest_cutting_mode(),
            item.fertilizer(),
            item.pesticide(),
            item.prev_seed_packets(),
            item.prev_seed_price(),
            item.prev_num_labour(),
            item.prev_labour_days(),
            item.prev_labour_charge(),
            item.prev_machine_charge(),
            item.prev_fertilizer_packets(),
            item.prev_fertilizer_charge(),
            item.prev_pesticide_packets(),
            item.prev_pesticide_charge(),
            item.comments()
        )
    }

    fun parseHarvest(item: CreateHarvestMutation.CreateHarvest): Harvest {
        return Harvest(
            item.id(),
            item.crop(),
            sdf.parse(item.sowing_date()) ?: Date(),
            item.seed_brand(),
            item.planting_mode(),
            item.weeding_mode(),
            item.harvest_cutting_mode(),
            item.fertilizer(),
            item.pesticide(),
            item.prev_seed_packets(),
            item.prev_seed_price(),
            item.prev_num_labour(),
            item.prev_labour_days(),
            item.prev_labour_charge(),
            item.prev_machine_charge(),
            item.prev_fertilizer_packets(),
            item.prev_fertilizer_charge(),
            item.prev_pesticide_packets(),
            item.prev_pesticide_charge(),
            item.comments()
        )
    }

    fun parseHarvest(item: UpdateHarvestMutation.UpdateHarvest): Harvest {
        return Harvest(
            item.id(),
            item.crop(),
            sdf.parse(item.sowing_date()) ?: Date(),
            item.seed_brand(),
            item.planting_mode(),
            item.weeding_mode(),
            item.harvest_cutting_mode(),
            item.fertilizer(),
            item.pesticide(),
            item.prev_seed_packets(),
            item.prev_seed_price(),
            item.prev_num_labour(),
            item.prev_labour_days(),
            item.prev_labour_charge(),
            item.prev_machine_charge(),
            item.prev_fertilizer_packets(),
            item.prev_fertilizer_charge(),
            item.prev_pesticide_packets(),
            item.prev_pesticide_charge(),
            item.comments()
        )
    }

    fun buildFarmHarvests(callback: DataStoreOperationCallback) {

        thread {
            val map = HashMap<String, ArrayList<String>>()
            for (farmID in farmMap.keys) {
                map[farmID] = ArrayList()
            }

            for (mapping in farmMappings) {
                if (mapping.farmID !in map) {
                    Log.e(TAG, "Farm with ${mapping.farmID} not found")
                } else {
                    map[mapping.farmID]?.add(mapping.harvestID)
                }
            }

            farmsHarvests.clear()

            for (entry in map) {
                farmsHarvests.add(FarmHarvest(entry.key, entry.value))
            }

            db.farmHarvestDao().deleteAll()
            db.farmHarvestDao().insertAll(farmsHarvests)

            callback(true)
        }
    }
}
