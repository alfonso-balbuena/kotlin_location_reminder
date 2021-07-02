package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient : GeofencingClient
    private val CODE_RESULT_PERMISSION = 1

    private val geofencePendingIntent : PendingIntent by lazy {
        val intent = Intent(context,GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        context?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPermissionFineLocation()
            }
            geofencingClient = LocationServices.getGeofencingClient(it)
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPermissionFineLocation() {
        val permissions = mutableListOf<String>()
        if(context?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(context?.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
        if(permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(),CODE_RESULT_PERMISSION)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkLocationEnabled(resolve : Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(activity!!)
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnSuccessListener {
            addGeofenceAndSave()
        }
        locationSettingsResponseTask.addOnFailureListener {exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    startIntentSenderForResult(exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,null,0,0,0,null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("SAVEREMINDER", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkLocationEnabled()
                }.show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkLocationEnabled(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == CODE_RESULT_PERMISSION) {
            if(permissions.isEmpty() || grantResults.any { result -> result == PackageManager.PERMISSION_DENIED }) {
                val alertDialog = AlertDialog.Builder(context).setMessage(getString(R.string.permission_denied_explanation))
                    .setTitle(getString(R.string.location_required_error)).setNeutralButton("Ok", DialogInterface.OnClickListener{
                            dialog, _ -> dialog.cancel() }).create()
                alertDialog.show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val reminderData = getReminderDataFromViewModel()
            if(_viewModel.validateEnteredData(reminderData)) {
                addGeofence()
            }
        }
    }

    private fun getReminderDataFromViewModel(): ReminderDataItem {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.selectedPOI.value?.latLng?.latitude
        val longitude = _viewModel.selectedPOI.value?.latLng?.longitude
        return ReminderDataItem(title, description = description, location = location, latitude = latitude, longitude = longitude)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addGeofenceAndSave() {
        val reminderData = getReminderDataFromViewModel()
        val geofence = Geofence.Builder().
        setRequestId(reminderData.id).
        setCircularRegion(reminderData.latitude!!,reminderData.longitude!!, 100f).
        setExpirationDuration(1000 * 60 * 60).
        setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).
        build()
        val geofencingRequest = GeofencingRequest.Builder().
        setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).
        addGeofence(geofence).
        build()
        if(context?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent).run {
                addOnSuccessListener {
                    _viewModel.validateAndSaveReminder(reminderData)
                }
                addOnFailureListener {
                    Log.d("AddGeofencing","${it.message}")
                    val snackbar = Snackbar.make(binding.root,getString(R.string.geofences_not_added),Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addGeofence() {
        if(context?.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            context?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getPermissionFineLocation()
        } else {
            checkLocationEnabled()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.action.ACTION_GEOFENCE_EVENT"
    }
}

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29