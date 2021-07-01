package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private val CODE_RESULT_PERMISSION = 1
    private var _map : GoogleMap? = null
    private var _marker : Marker? = null
    private var name : String = ""
    private var locationProviderClient : FusedLocationProviderClient? = null


    @RequiresApi(Build.VERSION_CODES.M)
    private val callback = OnMapReadyCallback {
        _map = it
        _map?.setMinZoomPreference(15.0f)
        _map?.setMaxZoomPreference(18.0f)
        _map?.setOnPoiClickListener { poi -> addMarker(poi.name,poi.latLng) }
        _map?.setOnMapLongClickListener { place -> addMarker("Custom place",place) }
        _map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(context,R.raw.map_style))
        initMap()
    }

    private fun addMarker(title : String, latLng : LatLng) {
        _marker?.remove()
        name = title
        _marker = _map?.addMarker(MarkerOptions().position(latLng).title(title))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermission()
        }

        binding.btnSave.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun initLocationRequest() {
        context?.let {
            locationProviderClient = LocationServices.getFusedLocationProviderClient(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFr = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFr.getMapAsync(callback)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPermission() {
        if(context?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),CODE_RESULT_PERMISSION)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == CODE_RESULT_PERMISSION) {
            if(permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap()
            } else {
                val alertDialog = AlertDialog.Builder(context).setMessage(getString(R.string.permission_denied_explanation))
                    .setTitle(getString(R.string.location_required_error)).setNeutralButton("Ok", DialogInterface.OnClickListener{
                            dialog, _ -> dialog.cancel() }).create()
                alertDialog.show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initMap() {
        if(context?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            _map?.isMyLocationEnabled = true
            initLocationRequest()
            locationProviderClient?.let {
                it.lastLocation.addOnSuccessListener { location ->
                    _map?.let { _mapLet ->
                        location?.let { loc ->
                            _mapLet.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude,loc.longitude),18.0f))
                        }

                    }
                }
            }
        }
    }

    private fun onLocationSelected() {
        val poi = PointOfInterest(_marker?.position,"",name)
        _viewModel.selectedPOI.value = poi
        _viewModel.reminderSelectedLocationStr.value = name
        NavHostFragment.findNavController(this).navigateUp()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            _map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            _map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            _map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            _map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
