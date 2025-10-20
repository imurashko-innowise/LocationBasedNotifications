package com.sap.codelab.view.geofencing

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.view.notifications.NotificationsHelper
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import kotlinx.coroutines.launch

internal class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        val memoId = geofencingEvent?.triggeringGeofences?.firstOrNull()?.requestId?.toLongOrNull() ?: return

        ScopeProvider.application.launch {
            val memo = Repository.getMemoById(memoId)
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) NotificationsHelper.showNotification(context, memo)
        }

    }
}