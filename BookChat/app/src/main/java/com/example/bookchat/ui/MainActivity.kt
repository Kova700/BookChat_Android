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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
		binding.lifecycleOwner = this
		initNavigation()
	}

	private fun initNavigation() {
		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.container_main) as NavHostFragment
		val navController = navHostFragment.findNavController()
		initBottomNavigationView(navController)
	}

	private fun initBottomNavigationView(navController: NavController) {
		binding.bnvMain.setupWithNavController(navController)
	}

}