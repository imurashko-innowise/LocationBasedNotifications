package com.sap.codelab.repository

import android.app.Application
import com.sap.codelab.view.geofencing.GeofenceHelper
import com.sap.codelab.view.notifications.NotificationsHelper

/**
 * Extension of the Android Application class.
 */
internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
        NotificationsHelper.createNotificationChannel(this)
        GeofenceHelper.initialize(this)
    }
}