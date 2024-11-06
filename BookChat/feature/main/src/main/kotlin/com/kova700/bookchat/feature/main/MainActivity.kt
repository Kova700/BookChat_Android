package com.kova700.bookchat.feature.main

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.navigation.MainNavigationViewModel
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.feature.main.databinding.ActivityMainBinding
import com.kova700.bookchat.feature.main.navigation.getResId
import com.kova700.bookchat.util.permissions.getPermissionsLauncher
import com.kova700.bookchat.util.permissions.notificationPermission
import com.kova700.bookchat.util.toast.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.kova700.bookchat.feature.main.R as mainR

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var navHostFragment: NavHostFragment
	private lateinit var navController: NavController

	private val mainNavigationViewmodel by viewModels<MainNavigationViewModel>()
	private val mainViewmodel by viewModels<MainViewModel>()

	private val notificationPermissionLauncher = getPermissionsLauncher(
		onSuccess = {},
		onDenied = { makeToast(R.string.notification_permission_denied) },
		onExplained = {
			makeToast(R.string.notification_permission_explained)
			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
			val uri = Uri.fromParts(SCHEME_PACKAGE, packageName, null)
			intent.data = uri
			startActivity(intent)
		}
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		initNavigation()
		requestNotificationPermission()
		observeNavigationEvents()
		observeUiState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		mainViewmodel.uiState.collect { uiState ->
			setNetworkStateBarUiState(uiState)
		}
	}

	private fun initNavigation() {
		navHostFragment =
			supportFragmentManager.findFragmentById(mainR.id.container_main) as NavHostFragment
		navController = navHostFragment.findNavController()
		initBottomNavigationView()
	}

	private fun initBottomNavigationView() {
		binding.bnvMain.setupWithNavController(navController)
	}

	private fun setNetworkStateBarUiState(uiState: MainUiState) {
		with(binding) {
			when (uiState.networkState) {
				NetworkState.CONNECTED -> networkStateBar.visibility = View.GONE
				NetworkState.DISCONNECTED -> {
					networkStateBar.setText(R.string.please_connect_the_network)
					networkStateBar.setBackgroundColor(Color.parseColor("#666666"))
					networkStateBar.visibility = View.VISIBLE
				}
			}
		}
	}

	private fun observeNavigationEvents() = lifecycleScope.launch {
		mainNavigationViewmodel.navigateEvent.collect { route ->
			val fragmentId = route.getResId()
			binding.bnvMain.selectedItemId = fragmentId
		}
	}

	private fun requestNotificationPermission() {
		notificationPermissionLauncher.launch(notificationPermission)
	}

	companion object {
		private const val SCHEME_PACKAGE = "package"
	}
}