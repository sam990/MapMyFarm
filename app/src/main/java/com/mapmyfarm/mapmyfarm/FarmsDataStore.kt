package com.mapmyfarm.mapmyfarm


import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.amazonaws.amplify.generated.graphql.CreateFarmDetailsMutation
import com.amazonaws.amplify.generated.graphql.DeleteFarmDetailsMutation
import com.amazonaws.amplify.generated.graphql.ListFarmDetailsQuery
import com.amazonaws.amplify.generated.graphql.UpdateFarmDetailsMutation
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.android.gms.maps.model.LatLng
import com.mapmyfarm.mapmyfarm.AppSyncClientFactory.appSyncClient
import type.CreateFarmDetailsInput
import type.DeleteFarmDetailsInput
import type.UpdateFarmDetailsInput
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.Nonnull
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread

//interface DataStoreOperationCallback{
//    fun onSuccess()
//    fun onError()
//}

typealias DataStoreOperationCallback = (Boolean) -> Unit

object FarmsDataStore {

    @Volatile var farmsHarvests: MutableList<FarmHarvest> = ArrayList()
    @Volatile var farmMap = HashMap<String, Farm>()
    @Volatile var harvestMap = HashMap<String, Harvest>()

    private val sdf = SimpleDateFormat("yyyy-MM-ddZZZZZ", Locale.ENGLISH)
//    private const val TAG = "FarmsDataStore"

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

            val farms = db.farmDao().getAll()
            for (farm in farms) {
                farmMap[farm.id] = farm
            }

            val harvests = db.harvestDao().getAll()
            for (harvest in harvests) {
                harvestMap[harvest.id] = harvest
            }

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



    fun fetchItems(callback: DataStoreOperationCallback) {

        appSyncClient()!!.query(ListFarmDetailsQuery.builder().build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(object : GraphQLCall.Callback<ListFarmDetailsQuery.Data?>() {

                @Synchronized override fun onResponse(@Nonnull response: Response<ListFarmDetailsQuery.Data?>) {
                    val items: MutableList<ListFarmDetailsQuery.Item>? = response.data()?.listFarmDetails()?.items()
                    farmsList.clear()
                    if (items != null) {
                        for (item in items) {
                            farmsList.add(
                                parseFarm(item)
                            )
                        }
                    }
                    db.farmDao().insertAll(farmsList)
                    callback(true)
                }


                override fun onFailure(e: ApolloException) {
                    callback(false)
                }
            })
    }

    fun addFarm(
        area: Double?,
        farmID: String,
        sowingDate: Date,
        points: ArrayList<LatLng>?,
        crop: String,
        seedBrand: String?,
        plantingMode: String?,
        weedingMode: String?,
        harvestCuttingMode: String?,
        fertilizer: String?,
        pesticide: String?,
        landType: String?,
        locale: String?,
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
        userCallback: DataStoreOperationCallback
    ) {
        val (first, second) = getDoubleLatLngList(points)
        val userMobile = AWSMobileClient.getInstance().username
        val callback: GraphQLCall.Callback<CreateFarmDetailsMutation.Data> =
            object : GraphQLCall.Callback<CreateFarmDetailsMutation.Data>() {

                override fun onResponse(@Nonnull response: Response<CreateFarmDetailsMutation.Data>) {
                    fetchItems(userCallback)
                }

                override fun onFailure(@Nonnull e: ApolloException) {
                    userCallback.onError()
                }
            }
        appSyncClient()!!.mutate(
            CreateFarmDetailsMutation.builder()
                .input(
                    CreateFarmDetailsInput.builder()
                        .area(area ?: 0.0)
                        .crop(crop)
                        .seed_brand(seedBrand)
                        .farm_id(farmID)
                        .sowing_date(sdf.format(sowingDate))
                        .planting_mode(plantingMode)
                        .weeding_mode(weedingMode)
                        .harvest_cutting_mode(harvestCuttingMode)
                        .fertilizer(fertilizer)
                        .pesticide(pesticide)
                        .land_type(landType)
                        .coordinatesLat(first)
                        .coordinatesLon(second)
                        .locale(locale)
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
                        .usermobile(userMobile)
                        .build()
                )
                .build()
        ).enqueue(callback)
    }

    fun deleteFarm(id: String, userCallback: DataStoreOperationCallback) {
        val callback: GraphQLCall.Callback<DeleteFarmDetailsMutation.Data> =
            object : GraphQLCall.Callback<DeleteFarmDetailsMutation.Data>() {
                override fun onResponse(@Nonnull response: Response<DeleteFarmDetailsMutation.Data>) {

                    db.farmDao().deleteByID(id)
                    refreshRoomDB(userCallback)
                }

                override fun onFailure(@Nonnull e: ApolloException) {
                    userCallback.onError()
                }
            }
        appSyncClient()!!.mutate(
            DeleteFarmDetailsMutation.builder()
                .input(DeleteFarmDetailsInput.builder().id(id).build())
                .build()
        ).enqueue(callback)
    }

    fun updateFarm(
        id: String,
        farmID: String?,
        sowingDate: Date?,
        crop: String?,
        seedBrand: String?,
        plantingMode: String?,
        weedingMode: String?,
        harvestCuttingMode: String?,
        fertilizer: String?,
        pesticide: String?,
        landType: String?,
        locale: String?,
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
        userCallback: DataStoreOperationCallback
    ) {
        val callback: GraphQLCall.Callback<UpdateFarmDetailsMutation.Data> =
            object : GraphQLCall.Callback<UpdateFarmDetailsMutation.Data>() {
                override fun onResponse(@Nonnull response: Response<UpdateFarmDetailsMutation.Data>) {
                    fetchItems(userCallback)
                }

                override fun onFailure(@Nonnull e: ApolloException) {
                    userCallback.onError()
                }
            }
        val builder: UpdateFarmDetailsInput.Builder = UpdateFarmDetailsInput.builder()
        builder.id(id)
        if (farmID != null) {
            builder.farm_id(farmID)
        }
        if (sowingDate != null) {
            builder.sowing_date(sdf.format(sowingDate))
        }
        if(crop != null) {
            builder.crop(crop)
        }
        if(seedBrand != null) {
            builder.seed_brand(seedBrand)
        }
        if(plantingMode != null) {
            builder.planting_mode(plantingMode)
        }
        if(weedingMode != null) {
            builder.weeding_mode(weedingMode)
        }
        if(harvestCuttingMode != null) {
            builder.harvest_cutting_mode(harvestCuttingMode)
        }
        if(fertilizer != null) {
            builder.harvest_cutting_mode(harvestCuttingMode)
        }
        if(pesticide != null) {
            builder.pesticide(pesticide)
        }
        if(landType != null) {
            builder.land_type(landType)
        }
        if(locale != null) {
            builder.locale(locale)
        }
        if(prevSeedPieces != null) {
            builder.prev_seed_packets(prevSeedPieces)
        }
        if(prevSeedPrice != null) {
            builder.prev_seed_price(prevSeedPrice)
        }
        if(prevLabourNum != null) {
            builder.prev_num_labour(prevLabourNum)
        }
        if(prevLabourDays != null) {
            builder.prev_labour_days(prevLabourDays)
        }
        if(prevLabourCharge != null) {
            builder.prev_labour_charge(prevLabourCharge)
        }
        if(prevMachineryCharge != null) {
            builder.prev_machine_charge(prevMachineryCharge)
        }
        if(prevFertilizerPieces != null) {
            builder.prev_fertilizer_packets(prevFertilizerPieces)
        }
        if(prevFertilizerPrice != null) {
            builder.prev_fertilizer_charge(prevFertilizerPrice)
        }
        if(prevPesticidePieces != null) {
            builder.prev_pesticide_packets(prevPesticidePieces)
        }
        if(prevPesticidePrice != null) {
            builder.prev_pesticide_charge(prevPesticidePrice)
        }
        if(comment != null) {
            builder.comments(comment)
        }

        appSyncClient()!!.mutate(
            UpdateFarmDetailsMutation.builder()
                .input(builder.build())
                .build()
        ).enqueue(callback)
    }

    fun parseFarm(item: ListFarmDetailsQuery.Item): FarmClass {
        val coors = doubleToLatLngList(item.coordinatesLat(), item.coordinatesLon())
        return FarmClass(
            item.id(),
            item.area(),
            item.farm_id(),
            sdf.parse(item.sowing_date()) ?: Date(),
            coors,
            item.crop(),
            item.seed_brand(),
            item.planting_mode(),
            item.weeding_mode(),
            item.harvest_cutting_mode(),
            item.fertilizer(),
            item.pesticide(),
            item.land_type(),
            item.locale(),
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

    fun getDoubleLatLngList(points: ArrayList<LatLng>?) : Pair<ArrayList<Double>, ArrayList<Double>> {
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
}
