package com.sap.codelab.view.create

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.sap.codelab.view.geofencing.GeofenceHelper
import com.sap.codelab.view.permissions.Permission
import com.sap.codelab.view.permissions.PermissionManager
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.model.Memo
import com.sap.codelab.utils.extensions.empty
import com.sap.codelab.utils.extensions.showToast
import com.sap.codelab.view.locationpicker.LocationPicker
import kotlinx.coroutines.launch
import kotlin.jvm.java

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private lateinit var permissionManager: PermissionManager
    private val mapPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getLocationData()?.let {
                location = it
                updateLocationInfo(it)
            }
        } else {
            showToast(R.string.create_memo_no_location_selected)
        }
    }
    private var location: LatLng = LatLng(0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        binding.contentCreateMemo.locationPicker.setOnClickListener {
            openLocationPicker()
        }
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        model = ViewModelProvider(this)[CreateMemoViewModel::class.java]
        permissionManager = PermissionManager(
            context = this,
            registry = activityResultRegistry,
            lifecycleOwner = this,
        )

        registerCollectors()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }

            else             -> super.onOptionsItemSelected(item)
        }
    }

    private fun registerCollectors() {
        lifecycleScope.launch {
            model.onMemoSaved.collect { savedMemo ->
                permissionManager.setPermissions(Permission.Location)
                    .requestPermissions { isGranted ->
                        if (isGranted) {
                            registerGeofenceWithPermission(savedMemo)
                        } else {
                            showMissingPermissionsToast()
                        }
                    }
            }
        }
    }

    /**
     * Saves the memo if the input is valid; otherwise shows the corresponding error messages.
     */
    private fun saveMemo() {
        binding.contentCreateMemo.run {
            model.updateMemo(
                title = memoTitle.text.toString(),
                description = memoDescription.text.toString(),
                location = location,
            )
            if (model.isMemoValid()) {
                model.saveMemo()
                setResult(RESULT_OK)
                finish()
            } else {
                memoTitleContainer.error = getErrorMessage(model.hasTitleError(), R.string.create_memo_title_empty_error)
                memoDescription.error = getErrorMessage(model.hasTextError(), R.string.create_memo_text_empty_error)
            }
        }
    }

    /**
     * Returns the error message if there is an error, or an empty string otherwise.
     *
     * @param hasError          - whether there is an error.
     * @param errorMessageResId - the resource id of the error message to show.
     * @return the error message if there is an error, or an empty string otherwise.
     */
    private fun getErrorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String {
        return if (hasError) {
            getString(errorMessageResId)
        } else {
            String.empty()
        }
    }

    private fun openLocationPicker() {
        permissionManager
            .setPermissions(Permission.Location)
            .requestPermissions { isGranted ->
                if (isGranted) {
                    val intent = Intent(this, LocationPicker::class.java)
                    mapPickerLauncher.launch(intent)
                } else {
                    showMissingPermissionsToast()
                }
            }
    }

    private fun registerGeofenceWithPermission(memo: Memo) {
        val hasPermission = checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            GeofenceHelper.registerGeofence(
                context = this,
                memo = memo,
            )
        } else {
            showMissingPermissionsToast()
        }
    }

    private fun Intent.getLocationData(): LatLng? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(LocationPicker.RESULT_LOCATION_NAME, LatLng::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(LocationPicker.RESULT_LOCATION_NAME)
        }
    }

    private fun updateLocationInfo(location: LatLng) {
        binding.contentCreateMemo.apply {
            locationLatitude.text = location.latitude.toString()
            locationLongitude.text = location.longitude.toString()
        }
    }

    private fun showMissingPermissionsToast() =
        showToast(R.string.create_memo_geofence_register_error)
}
