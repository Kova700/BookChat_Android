package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMainBinding
import com.example.bookchat.ui.fragment.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val homeFragment by lazy { makeFragment(HomeFragment()) }
    private val bookShelfFragment by lazy { makeFragment(BookShelfFragment()) }
    private val searchFragment by lazy { makeFragment(SearchFragment()) }
    private val chatFragment by lazy { makeFragment(ChatFragment()) }
    private val myPageFragment by lazy { makeFragment(MyPageFragment()) }
    private val fragmentList by lazy { listOf(homeFragment, bookShelfFragment, searchFragment, chatFragment, myPageFragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        with(binding){
            lifecycleOwner =this@MainActivity
            activity = this@MainActivity
        }
        inflateNewFragment(homeFragment)
        setBottomNavigation()
    }

    fun setBottomNavigation(){
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.home_navi_icon -> inflateOldFragment(homeFragment)
                R.id.bookshelf_navi_icon -> inflateOldFragment(bookShelfFragment)
                R.id.search_navi_icon -> inflateOldFragment(searchFragment)
                R.id.chat_navi_icon -> inflateOldFragment(chatFragment)
                R.id.mypage_navi_icon -> inflateOldFragment(myPageFragment)
            }
            true
        }
    }

    private fun makeFragment(fragment :Fragment) :Fragment{
        supportFragmentManager.beginTransaction().add(R.id.main_frame,fragment).commit()
        return fragment
    }

    private fun inflateOldFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().show(fragment).commit()
        fragmentList.filter { !it.javaClass.isAssignableFrom(fragment.javaClass) }
            .forEach { supportFragmentManager.beginTransaction().hide(it).commit() }
    }

    private fun inflateNewFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame, fragment)
            .commit()
    }

}