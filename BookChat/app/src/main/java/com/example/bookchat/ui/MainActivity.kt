package com.example.bookchat.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMainBinding
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.example.bookchat.ui.channelList.ChannelListFragment
import com.example.bookchat.utils.makeToast
import com.example.bookchat.utils.permissions.getPermissionsLauncher
import com.example.bookchat.utils.permissions.notificationPermission
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var navHostFragment: NavHostFragment
	private lateinit var navController: NavController

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
		moveToChannelIfNeed()
		requestNotificationPermission()
	}

	private fun initNavigation() {
		navHostFragment =
			supportFragmentManager.findFragmentById(R.id.container_main) as NavHostFragment
		navController = navHostFragment.findNavController()
		initBottomNavigationView()
	}

	private fun initBottomNavigationView() {
		binding.bnvMain.setupWithNavController(navController)
	}

	fun navigateToBookShelfFragment() {
		binding.bnvMain.selectedItemId = R.id.bookShelfFragment
	}

	fun navigateToSearchFragment() {
		binding.bnvMain.selectedItemId = R.id.searchFragment
	}

	private fun moveToChannelIfNeed() {
		if (intent.hasExtra(EXTRA_CHANNEL_ID).not()) return

		val channelId = intent.getLongExtra(EXTRA_CHANNEL_ID, -1)
		moveToChannel(channelId)
	}

	private fun moveToChannel(channelId: Long) {
		val intent = Intent(this, ChannelActivity::class.java)
		intent.putExtra(ChannelListFragment.EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun requestNotificationPermission() {
		notificationPermissionLauncher.launch(notificationPermission)
	}

	companion object {
		private const val SCHEME_PACKAGE = "package"
	}
}