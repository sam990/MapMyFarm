package com.mapmyfarm.mapmyfarm


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.client.AWSMobileClient
import com.eyalbira.loadingdots.LoadingDots
import com.google.android.material.button.MaterialButton


private const val LOCATION_PERMISSION_REQUEST_CODE = 990
const val TAG = "Dashboard"

class Dashboard : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var farmAdapter: FarmAdapter
    private var accessToFetch = false
    lateinit var loadingDots: LoadingDots
    lateinit var emptyView: TextView
    var locationPermissionEnabled = false
    lateinit var aboutDialog: AlertDialog
    var fetchFromNetwork = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        title = "Dashboard"

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.drawable.actionbar_logo)
        supportActionBar?.setDisplayUseLogoEnabled(true)



        recyclerView = findViewById(R.id.farm_list_recycler_view)
        recyclerView.itemAnimator = DefaultItemAnimator()
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
//        recyclerView.setHasFixedSize(true)
        farmAdapter = FarmAdapter( {farmID, harvestID -> onHarvestClick(farmID, harvestID)}) {
            addHarvest(it)
        }
        recyclerView.adapter = farmAdapter
        loadingDots = findViewById(R.id.recyclerview_loading_dots)
        emptyView = findViewById(R.id.recyclerview_empty)

        loadUserDetailsLocal(this, object : InitialiseUserDetailsCallback{
            override fun onResult(result: Boolean) {
                if (!result){
                    initialiseUserDetails(this@Dashboard, object : InitialiseUserDetailsCallback{
                        override fun onResult(result: Boolean) {
                            if(!result) {
                                runOnUiThread { getUserDetails() }
                            }
                        }
                    })
                }
            }
        })


        AppSyncClientFactory.init(this)

        checkLocationPermission()

        findViewById<MaterialButton>(R.id.add_farm_button).setOnClickListener {
            initialiseAddFarm()
        }
        /*findViewById<MaterialButton>(R.id.dashboard_contact).setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.mapmyfarm_contact)))
            startActivity(callIntent)
        }

        findViewById<MaterialButton>(R.id.dashboard_about).setOnClickListener {
            showAbout()
        }*/
        initialiseAbout()

        fetchFromNetwork = intent.getBooleanExtra("LOAD_FROM_SERVER", false)
        FarmsDataStore.initialiseRoomDB(this)
        startFetchingList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                return true
            }

            R.id.action_refresh -> {
                fetchFromNetwork = true
                startFetchingList()
                return true
            }

            R.id.action_contact -> {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.mapmyfarm_contact)))
                startActivity(callIntent)
                return true
            }

            R.id.action_about -> {
                showAbout()
                return true
            }

            R.id.action_edit_details -> {
                val myIntent = Intent(this, UpdateUserDetails::class.java)
                startActivity(myIntent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initialiseAbout() {
        aboutDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.about)
                setMessage(R.string.about_content)
                setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }
            }
            builder.create()
        }
    }

    private fun showAbout() {
        aboutDialog.show()
    }



    private fun startFetchingList() {
        accessToFetch = true
        val callback: DataStoreOperationCallback = {
                if(it) {
                    //populate
                   runOnUiThread {
                        fetchFromNetwork = false
                        loadingDots.stopAnimation()
                        loadingDots.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }

                    if (FarmsDataStore.farmsHarvests.size == 0) {
                        runOnUiThread {
                            emptyView.setText(R.string.empty_message)
                            emptyView.visibility = View.VISIBLE
                        }
                    } else if (emptyView.visibility == View.VISIBLE) {
                        runOnUiThread { emptyView.visibility = View.GONE }
                    }
                    val newList = FarmsDataStore.farmsHarvests.toList()
                    runOnUiThread { farmAdapter.submitList(newList) }
                }

                else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Unable to get data", Toast.LENGTH_LONG).show()
                    }
                }
            }
        if(fetchFromNetwork){
            FarmsDataStore.refreshDataNetwork(callback)
        } else {
            FarmsDataStore.refreshRoomDB(callback)
        }
    }

    private fun onHarvestClick(farmID: String, harvestID: String) {
        val myIntent = Intent(this, HarvestEdit::class.java)
        myIntent.putExtra("FARM_ID", farmID)
        myIntent.putExtra("HARVEST_ID", harvestID)
        startActivity(myIntent)
    }

    private fun addHarvest(farmID: String) {
        val myIntent = Intent(this, HarvestDetailsInput::class.java)
        myIntent.putExtra("FARM_ID", farmID)
        startActivity(myIntent)
    }

    private fun initialiseAddFarm() {
        if(!locationPermissionEnabled) {
            Toast.makeText(applicationContext,
                "Enable location permission to add Farm",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val myIntent = Intent(this, TraceFarm::class.java)
        startActivity(myIntent)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        locationPermissionEnabled = if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Enable the my location layer if the permission has been granted.
            true
        } else {
            // Permission was denied.
            Toast.makeText(applicationContext,
                "Enable location permission to add Farm",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    override fun onResume() {
        super.onResume()
        startFetchingList()
    }

    private fun logoutUser() {
        FarmsDataStore.clearDB {
            if(it) {
                runOnUiThread {
                    AWSMobileClient.getInstance().signOut()
                    //go to the login page
                    val myIntent = Intent(this@Dashboard, SignIn::class.java)
                    startActivity(myIntent)
                    finish()
                }
            }
            else {
                runOnUiThread {
                    Toast.makeText(applicationContext,
                        "Error Logout",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }
    }

    private fun getUserDetails() {
        val myIntent = Intent(this, GetUserDetails::class.java)
        startActivity(myIntent)
        finish()
    }

}
