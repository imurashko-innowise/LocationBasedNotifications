package com.sap.codelab.view.geofencing

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sap.codelab.model.Memo

internal object GeofenceHelper {

    private lateinit var geofencingClient: GeofencingClient

    fun initialize(context: Context) {
        geofencingClient = LocationServices.getGeofencingClient(context)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun registerGeofence(context: Context, memo: Memo) {
        val geofence = Geofence.Builder()
            .setRequestId(memo.id.toString())
            .setCircularRegion(memo.reminderLatitude, memo.reminderLongitude, DEFAULT_GEOFENCE_RADIUS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER
            )
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(
                Geofence.GEOFENCE_TRANSITION_ENTER
            )
            .addGeofence(geofence)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
    }

    fun removeGeofenceForMemo(memo: Memo) {
        geofencingClient.removeGeofences(listOf(memo.id.toString()))
    }

    private const val DEFAULT_GEOFENCE_RADIUS = 200f
}