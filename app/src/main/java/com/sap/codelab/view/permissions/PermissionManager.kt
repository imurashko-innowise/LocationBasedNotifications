package com.sap.codelab.view.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

internal class PermissionManager(
    private val context: Context,
    registry: ActivityResultRegistry,
    lifecycleOwner: LifecycleOwner,
) {

    private var requiredPermissions = arrayOf<Permission>()
    private var callback: (Boolean) -> Unit = {}
    // In case we'll need detailed breakdown of which permissions were granted/denied
    private var detailedCallback: (Map<Permission,Boolean>) -> Unit = {}

    private val permissionCheck = registry.register(
        PERMISSION_LAUNCHER_KEY,
        lifecycleOwner,
        RequestMultiplePermissions(),
    ) { grantResults ->
        sendResultAndCleanUp(grantResults)
    }

    fun havePermissions(vararg permission: Permission): Boolean {
        return permission.areAllPermissionsGranted()
    }

    fun setPermissions(vararg permission: Permission): PermissionManager {
        requiredPermissions = arrayOf(*permission)
        return this
    }

    fun requestPermissions(onResult: (Boolean) -> Unit) {
        this.callback = onResult
        handlePermissionRequest()
    }

    fun requestPermissionsDetailed(onResult: (Map<Permission,Boolean>) -> Unit) {
        this.detailedCallback = onResult
        handlePermissionRequest()
    }

    private fun handlePermissionRequest() = when {
        requiredPermissions.areAllPermissionsGranted() -> sendPositiveResult()
        else -> requestPermissions()
    }

    private fun sendPositiveResult() {
        sendResultAndCleanUp(getPermissionList().associateWith { true })
    }

    private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
        callback(grantResults.all { it.value })
        detailedCallback(grantResults.mapKeys { Permission.from(it.key) })
        cleanUp()
    }

    private fun cleanUp() {
        requiredPermissions = arrayOf()
        callback = {}
        detailedCallback = {}
    }

    private fun requestPermissions() {
        permissionCheck.launch(getPermissionList())
    }

    private fun Array<out Permission>.areAllPermissionsGranted(): Boolean {
        return all { it.isGranted() }
    }

    private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()

    private fun Permission.isGranted() = permissions.all { hasPermission(it) }

    private fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val PERMISSION_LAUNCHER_KEY = "permissionLauncherKey"
    }
}