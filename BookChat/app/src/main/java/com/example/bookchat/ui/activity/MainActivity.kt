package com.example.bookchat.ui.activity

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMainBinding
import com.example.bookchat.ui.fragment.*
import com.example.bookchat.utils.Constants.TOKEN_PATH
import java.io.File

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

/*아래 삭제 예정*/
    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame, fragment)
            .commit()
    }

    fun clickMenu() {
        with(binding){
//            if(drawerlayout.isDrawerOpen(Gravity.RIGHT)) {
//                drawerlayout.closeDrawer(Gravity.RIGHT)
//                return
//            }
//            drawerlayout.openDrawer(Gravity.RIGHT)
        }
    }
//    fun changePage(activityType: ActivityType) {
//        val targetActivity = when(activityType) {
//            ActivityType.bookShelfActivity -> { BookShelfActivity::class.java }
//            ActivityType.searchActivity -> { SearchActivity::class.java }
//        }
//        val intent = Intent(this, targetActivity)
//        startActivity(intent)
//    }
    fun clickSignOut(){
        val dialog = AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Light_Dialog)
        dialog.setTitle("정말 로그아웃하시겠습니까?")
            .setPositiveButton("취소",object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                }
            })
            .setNeutralButton("로그아웃",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    deleteToken()
                    finish()
                }
            })
            .setCancelable(true) //백버튼으로 닫히게 설정
            .show()
    }
    fun deleteToken(){
        val token = File(TOKEN_PATH)
        if (token.exists()) token.delete()
    }
    private fun getUserInfo(){
//        binding.profile.clipToOutline = true //프로필 라운딩
//        binding.userModel?.activityInitialization()
    }

}