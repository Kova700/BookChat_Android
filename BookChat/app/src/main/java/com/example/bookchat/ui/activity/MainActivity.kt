package com.example.bookchat.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMainBinding
import com.example.bookchat.ui.fragment.*
import com.example.bookchat.utils.Constants.TAG
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val homeFragment by lazy { HomeFragment() }
    private val bookShelfFragment by lazy { BookShelfFragment() }
    private val searchFragment by lazy { SearchFragment() }
    private val chatFragment by lazy { ChatFragment() }
    private val myPageFragment by lazy { MyPageFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        with(binding) {
            lifecycleOwner = this@MainActivity
        }

        addOrReplaceFragment(homeFragment,FRAGMENT_TAG_HOME)
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNaviItemSelectedListener)
    }

    private val bottomNaviItemSelectedListener =
        NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_navi_icon -> { addOrReplaceFragment(homeFragment,FRAGMENT_TAG_HOME) }
                R.id.bookshelf_navi_icon -> { addOrReplaceFragment(bookShelfFragment,FRAGMENT_TAG_BOOKSHELF) }
                R.id.search_navi_icon -> { addOrReplaceFragment(searchFragment,FRAGMENT_TAG_SEARCH) }
                R.id.chat_navi_icon -> { addOrReplaceFragment(chatFragment,FRAGMENT_TAG_CHAT) }
                R.id.mypage_navi_icon -> { addOrReplaceFragment(myPageFragment,FRAGMENT_TAG_MY_PAGE) }
            }
            true
        }

    private fun addFragment(newFragment: Fragment, tag :String) {
        Log.d(TAG, "MainActivity: addFragment() - called")
//        supportFragmentManager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE) //이거 사용을 좀 정리해야함
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        with(fragmentTransaction){
            setReorderingAllowed(true)
            getInflatedFragmentList().forEach { fragment -> hide(fragment) }
            add(R.id.main_frame, newFragment,tag)
            addToBackStack(tag)
            commit()
        }
    }

    private fun replaceFragment(newFragment: Fragment, tag :String) {
        Log.d(TAG, "MainActivity: replaceFragment() - called")
//        supportFragmentManager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE) //이거 사용을 좀 정리해야함
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        with(fragmentTransaction){
            setReorderingAllowed(true)
            getInflatedFragmentList().forEach { fragment -> hide(fragment) }
            show(newFragment)
            addToBackStack(tag)
            commit()
        }
    }

    private fun addOrReplaceFragment(newFragment: Fragment, tag :String) {
        if (newFragment.isAdded) {
            replaceFragment(newFragment,tag)
            return
        }
        addFragment(newFragment,tag)
    }

    private fun getInflatedFragmentList() :List<Fragment>{
        return supportFragmentManager.fragments.filter { it.isVisible }
    }

    private fun getInflatedBottomNaviFragment(inflatedFragmentList :List<Fragment>) :Fragment{
        return inflatedFragmentList.filter { fragment -> fragment.tag in bottomNaviFragmentTags}[0]
    }

    private fun updateBottomNaviIcon(){
        val inflatedBottomNaviFragment = getInflatedBottomNaviFragment(getInflatedFragmentList())

        when(inflatedBottomNaviFragment.tag){
            FRAGMENT_TAG_HOME -> { binding.bottomNavigationView.menu.findItem(R.id.home_navi_icon).isChecked = true }
            FRAGMENT_TAG_BOOKSHELF -> { binding.bottomNavigationView.menu.findItem(R.id.bookshelf_navi_icon).isChecked = true }
            FRAGMENT_TAG_SEARCH -> { binding.bottomNavigationView.menu.findItem(R.id.search_navi_icon).isChecked = true }
            FRAGMENT_TAG_CHAT -> { binding.bottomNavigationView.menu.findItem(R.id.chat_navi_icon).isChecked = true }
            FRAGMENT_TAG_MY_PAGE -> { binding.bottomNavigationView.menu.findItem(R.id.mypage_navi_icon).isChecked = true }
        }
        Log.d(TAG, "MainActivity: updateBottomNaviIcon() - hollCheckList\n" +
                "${binding.bottomNavigationView.menu.findItem(R.id.home_navi_icon).isChecked}\n" +
                "${binding.bottomNavigationView.menu.findItem(R.id.bookshelf_navi_icon).isChecked}\n" +
                "${binding.bottomNavigationView.menu.findItem(R.id.search_navi_icon).isChecked}\n" +
                "${binding.bottomNavigationView.menu.findItem(R.id.chat_navi_icon).isChecked}\n" +
                "${binding.bottomNavigationView.menu.findItem(R.id.mypage_navi_icon).isChecked}\n" +
                "최종 클릭 확인 ")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateBottomNaviIcon()
    }

    companion object{
        const val FRAGMENT_TAG_HOME = "Home"
        const val FRAGMENT_TAG_BOOKSHELF = "BookShelf"
        const val FRAGMENT_TAG_SEARCH = "Search"
        const val FRAGMENT_TAG_CHAT = "Chat"
        const val FRAGMENT_TAG_MY_PAGE = "MyPage"
        val bottomNaviFragmentTags =
            listOf(FRAGMENT_TAG_HOME, FRAGMENT_TAG_BOOKSHELF, FRAGMENT_TAG_SEARCH, FRAGMENT_TAG_CHAT, FRAGMENT_TAG_MY_PAGE)
    }

    //로그 다지우고
    //백스텍 다 끝나고 "'뒤로' 버튼을 한 번 더 누르면 종료됩니다." 구현
    //인스타그램처럼 스택 최대 바텀 버튼 수 만큼 관리 가능하게 구현
    ////addFragment실행후 뒤로가기 하면 add된 애들 다 사라지니까 다시 다른 fragment 누르면 replaceFragment가 아닌 addFragment가 호출됨(오류 없을까 생각)

}