package com.sap.codelab.view.home

import android.Manifest
import com.sap.codelab.view.permissions.PermissionManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import com.sap.codelab.view.geofencing.GeofenceHelper
import com.sap.codelab.view.permissions.Permission
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityHomeBinding
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import com.sap.codelab.utils.extensions.showToast
import com.sap.codelab.view.create.CreateMemo
import com.sap.codelab.view.detail.BUNDLE_MEMO_ID
import com.sap.codelab.view.detail.ViewMemo
import kotlinx.coroutines.launch

/**
 * The main activity of the app. Shows a list of recorded memos and lets the user add new memos.
 */
internal class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var model: HomeViewModel
    private lateinit var menuItemShowAll: MenuItem
    private lateinit var menuItemShowOpen: MenuItem
    private lateinit var permissionManager: PermissionManager
    private val createMemoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                model.refreshMemos()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        model = ViewModelProvider(this)[HomeViewModel::class.java]

        // Setup the adapter and the recycler view
        setupRecyclerView(initializeAdapter())

        binding.fab.setOnClickListener {
            // Handles clicks on the FAB button > creates a new Memo
            createMemoLauncher.launch(Intent(this@Home, CreateMemo::class.java))
        }
        model.loadOpenMemos()

        permissionManager = PermissionManager(
            context = this,
            registry = activityResultRegistry,
            lifecycleOwner = this,
        )
    }

    override fun onStart() {
        if (model.isFirstLaunch) {
            model.onFirstLaunch()
            requestPermissions()
            registerGeofencesForActiveMemos()
        }
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menuItemShowAll = menu.findItem(R.id.action_show_all)
        menuItemShowOpen = menu.findItem(R.id.action_show_open)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_all -> {
                model.loadAllMemos()
                //Switch available menu options
                menuItemShowAll.isVisible = false
                menuItemShowOpen.isVisible = true
                true
            }
            R.id.action_show_open -> {
                model.loadOpenMemos()
                //Switch available menu options
                menuItemShowOpen.isVisible = false
                menuItemShowAll.isVisible = true
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Opens the Memo detail view for the given memoId.
     *
     * @param memoId    - the id of the memo to be shown.
     */
    private fun openMemoDetails(memoId: Long) {
        val intent = Intent(this@Home, ViewMemo::class.java)
        intent.putExtra(BUNDLE_MEMO_ID, memoId)
        startActivity(intent)
    }

    /**
     * Initializes the recycler view to display the list of memos.
     */
    private fun setupRecyclerView(adapter: MemoAdapter) {
        binding.contentHome.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            addItemDecoration(
                DividerItemDecoration(
                    this@Home,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }
    }

    /**
     * Initializes the adapter and sets the needed callbacks.
     */
    private fun initializeAdapter(): MemoAdapter {
        val adapter = MemoAdapter(
            items = mutableListOf(),
            onItemClick = { view ->
                openMemoDetails((view.tag as Memo).id)
            },
            onCheckboxChanged = { checkbox, isChecked ->
                val memo = checkbox.tag as Memo
                GeofenceHelper.removeGeofenceForMemo(memo)
                model.updateMemo(memo, isChecked)
                model.refreshMemos()
            },
        )
        lifecycle.coroutineScope.launch {
            model.memos.collect { memos ->
                adapter.setItems(memos)
            }
        }
        return adapter
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Permission.Location, Permission.Notifications)
        } else {
            arrayOf(Permission.Location)
        }
        if (permissionManager.havePermissions(*permissions).not()) {
            permissionManager
                .setPermissions(*permissions)
                .requestPermissions { granted ->
                    val messageRes = if (granted) {
                        R.string.home_permissions_granted
                    } else {
                        R.string.home_missing_permissions
                    }
                    showToast(messageRes)
                }
        }
    }

    private fun registerGeofencesForActiveMemos() {
        ScopeProvider.application.launch {
            val activeMemos = Repository.getAll().filter { it.isDone.not() }

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                GeofenceHelper.registerAllGeofences(this@Home, activeMemos)
            }
        }
    }
}
