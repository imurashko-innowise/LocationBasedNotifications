package com.sap.codelab.view.permissions

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi

internal sealed class Permission(vararg val permissions: String) {

    object Location : Permission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    object Wifi : Permission(ACCESS_WIFI_STATE)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    object Notifications : Permission(POST_NOTIFICATIONS)

    companion object {
        @SuppressLint("NewApi")
        fun from(permission: String) = when (permission) {
            ACCESS_WIFI_STATE -> Wifi
            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> Location
            POST_NOTIFICATIONS -> Notifications
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}