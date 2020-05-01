package com.redev.mapdemo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class MainActivity : AppCompatActivity() , OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var mMap:GoogleMap

    //map
    private lateinit var mGoogleApiClient:GoogleApiClient
    private lateinit var mLocationRequest:LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //latitude and longitude
    private var latitude = 13.736717
    private var longitude = 100.523186

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //
        settingAppBar()

        val supportMap  = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMap.getMapAsync(this)


    }

    private fun settingAppBar() {
        toolbar = findViewById(R.id.app_bar_search)
        toolbar.setTitle("Map Demo")
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_setting , menu)

        Log.i("create menu","ok")
        val menuItem = menu!!.findItem(R.id.menu_search_map) as MenuItem
        var searchMap = menuItem.actionView as SearchView

        searchMap.setOnQueryTextListener(object :android.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextChange(data: String?): Boolean {
                return false
            }

            override fun onQueryTextSubmit(data: String?): Boolean {

                if (!data.isNullOrEmpty())
                {
                    //
                    searchLocation(data)
                }

                return false
            }
        })
        return true
    }


    override fun onMapReady(map: GoogleMap?) {
        if (map != null)
        {
            mMap = map
            mMap.mapType =  (GoogleMap.MAP_TYPE_NORMAL)
            mMap.uiSettings.setZoomControlsEnabled(true);
            mMap.uiSettings.setZoomGesturesEnabled(true);
            mMap.uiSettings.setCompassEnabled(true);
//            mMap.setMyLocationEnabled(true);

            //maker location on map
            //mMap.addMarker(MarkerOptions().position(LatLng(latitude,longitude))).title = "Current Location"
            //var location = LatLng(latitude,longitude)
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        }
    }

    private fun searchLocation(data: String)
    {
        Log.i("map search" , data)


        val geocoder = Geocoder(this.applicationContext)

        val addressList = geocoder.getFromLocationName(data , 1)
        // val  address:MutableList<Address> = geocoder.getFromLocationName(data , 1)

        if (addressList != null)
        {
            Log.i("map search result" , addressList.size.toString())
            var ad = addressList.get(0)
            val latLng = LatLng(ad.latitude , ad.longitude)

            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap.uiSettings.isCompassEnabled = true
            mMap.uiSettings.isScrollGesturesEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isZoomGesturesEnabled = true

            mMap.addMarker(MarkerOptions().position(latLng).title(data)).title = data
           // mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16f))
        }
    }


    //

    override fun onStart() {
        super.onStart()

        //
        startLocationUpdate()
    }

    private fun startLocationUpdate() {

        Log.i("start location","working")

        //initial request location
        mLocationRequest = LocationRequest()
        mLocationRequest.run {
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            setInterval(0)
            setFastestInterval(0)
        }

        // initialize location setting request builder object
        val build = LocationSettingsRequest.Builder()
        build.addLocationRequest(mLocationRequest)

        val locationSettingRequest = build.build()

        // initialize location service object
        val settingClient = LocationServices.getSettingsClient(this)
        settingClient.checkLocationSettings(locationSettingRequest)

        // call register location listene
        registerLocationListner()
    }

    private fun registerLocationListner() {

        Log.i("register location","working")

        // initialize location callback object
        val locationCallback = object :LocationCallback()
        {
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationChange(locationResult!!.lastLocation)
            }
        }

        // 4. add permission if android version is greater then 23
        if(Build.VERSION.SDK_INT > 23 && checkPermission())
        {
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest,locationCallback,Looper.getMainLooper())

            //
           // buildGoogleApiClient()
            //mMap.isMyLocationEnabled = true
        }
    }

    private fun buildGoogleApiClient() {

       mGoogleApiClient = GoogleApiClient.Builder(this)
           .addConnectionCallbacks(this)
           .addOnConnectionFailedListener(this)
           .addApi(LocationServices.API).build()
        mGoogleApiClient.connect()
    }

    //
    private fun onLocationChange(lastLocation: Location?) {
        Log.i("start location","working")

        // create message for toast with updated latitude and longitudefa
        val message = "Update Location "+ lastLocation?.latitude +","+ lastLocation?.longitude

        // show toast message with updated location
        //Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
       // Toast.makeText(this , message ,Toast.LENGTH_LONG).show()


        val location = LatLng(lastLocation!!.latitude , lastLocation!!.longitude)
        //clear map
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(location)).title = "current locations"
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }

    //
    fun checkPermission():Boolean
    {
       if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
       {
           return true
       }
        else
       {
           requestPermissions()
           return false
       }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this , arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //
        if (requestCode == 1)
        {
            permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION
            registerLocationListner()
        }
    }

    override fun onConnected(p0: Bundle?) {
       mLocationRequest = LocationRequest()
        mLocationRequest.run {
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            setFastestInterval(100)
            setInterval(100)
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
           // LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }
}
