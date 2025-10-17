package com.sap.codelab.view.create

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.utils.extensions.empty
import com.sap.codelab.view.locationpicker.LocationPicker

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private val mapPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getLocationData()?.let {
                location = it
                updateLocationInfo(it)
            }
        } else {
            Toast.makeText(this, "No location selected", Toast.LENGTH_SHORT).show()
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
                memoTitleContainer.error = getErrorMessage(model.hasTitleError(), R.string.memo_title_empty_error)
                memoDescription.error = getErrorMessage(model.hasTextError(), R.string.memo_text_empty_error)
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
        val intent = Intent(this, LocationPicker::class.java)
        mapPickerLauncher.launch(intent)
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
}
