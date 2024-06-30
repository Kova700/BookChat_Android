package com.example.bookchat.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var navHostFragment: NavHostFragment
	private lateinit var navController: NavController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
		binding.lifecycleOwner = this
		initNavigation()
		moveToChannelListIfNeed()
	}

	private fun initNavigation() {
		navHostFragment = supportFragmentManager.findFragmentById(R.id.container_main) as NavHostFragment
		navController = navHostFragment.findNavController()
		initBottomNavigationView()
	}

	private fun initBottomNavigationView() {
		binding.bnvMain.setupWithNavController(navController)
	}

	private fun moveToChannelListIfNeed() {
		if (intent.hasExtra(EXTRA_NEED_SHOW_CHANNEL_LIST).not()) return
		navigateToChannelListFragment()
	}

	private fun navigateToChannelListFragment() {
		navController.navigate(R.id.action_homeFragment_to_channelListFragment)
	}

	companion object {
		const val EXTRA_NEED_SHOW_CHANNEL_LIST = "EXTRA_NEED_SHOW_CHANNEL_LIST"
	}
}