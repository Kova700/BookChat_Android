package com.example.bookchat.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
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

    private val bottomNaviFragmentStack = ArrayDeque<Fragment>()
    private var backPressedTime :Long= 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        with(binding) {
            lifecycleOwner = this@MainActivity
        }

        setBottomNavigation()
        addFragment(homeFragment,FRAGMENT_TAG_HOME)
    }

    private fun setBottomNavigation(){
        val bottomNaviItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_navi_icon -> { addOrReplaceFragment(homeFragment,FRAGMENT_TAG_HOME) }
                R.id.bookshelf_navi_icon -> { addOrReplaceFragment(bookShelfFragment,FRAGMENT_TAG_BOOKSHELF) }
                R.id.search_navi_icon -> { addOrReplaceFragment(searchFragment,FRAGMENT_TAG_SEARCH) }
                R.id.chat_navi_icon -> { addOrReplaceFragment(chatFragment,FRAGMENT_TAG_CHAT) }
                R.id.mypage_navi_icon -> { addOrReplaceFragment(myPageFragment,FRAGMENT_TAG_MY_PAGE) }
            }
            true
        }
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNaviItemSelectedListener)
    }


    private fun addOrReplaceFragment(newFragment: Fragment, tag :String) {
        if (newFragment.isAdded) {
            replaceFragment(newFragment)
            return
        }
        addFragment(newFragment,tag)
    }

    private fun addFragment(newFragment: Fragment, tag :String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        with(fragmentTransaction){
            setReorderingAllowed(true)
            stackNowFragment()
            hideFragments(fragmentTransaction)
            add(R.id.main_frame, newFragment,tag)
            commitNow()
        }
    }

    private fun replaceFragment(newFragment: Fragment) {
        if(newFragment.isVisible) return

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        with(fragmentTransaction){
            setReorderingAllowed(true)
            stackNowFragment()
            hideFragments(fragmentTransaction)
            show(newFragment)
            commitNow()
        }
    }

    private fun replaceFragmentInStack(newFragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        with(fragmentTransaction){
            setReorderingAllowed(true)
            hideFragments(fragmentTransaction)
            show(newFragment)
            commitNow()
        }
    }

    private fun hideFragments(fragmentTransaction : FragmentTransaction){
        val inflatedFragmentList = getInflatedFragmentList()
        inflatedFragmentList.forEach { fragment -> fragmentTransaction.hide(fragment) }
    }

    private fun stackNowFragment(){
        val fragment = getInflatedBottomNaviFragment(getInflatedFragmentList()) ?: return
        if (bottomNaviFragmentStack.contains(fragment)) removeFragmentInStack(fragment)
        addFragmentInStack(fragment)
    }

    private fun addFragmentInStack(fragment: Fragment){
        if (bottomNaviFragmentStack.contains(fragment)) return
        bottomNaviFragmentStack.add(fragment)
    }

    private fun removeFragmentInStack(fragment: Fragment){
        if(fragment == homeFragment) return
        bottomNaviFragmentStack.remove(fragment)
    }

    private fun getInflatedFragmentList() :List<Fragment>{
        return supportFragmentManager.fragments.filter { it.isVisible }
    }

    private fun getInflatedBottomNaviFragment(inflatedFragmentList :List<Fragment>) :Fragment?{
        return inflatedFragmentList.firstOrNull { fragment -> fragment.tag in bottomNaviFragmentTags }
    }

    private fun updateBottomNaviIcon(){
        val inflatedBottomNaviFragment = getInflatedBottomNaviFragment(getInflatedFragmentList())
        val bottomNavigationView = binding.bottomNavigationView.menu
        when(inflatedBottomNaviFragment?.tag){
            FRAGMENT_TAG_HOME -> { bottomNavigationView.findItem(R.id.home_navi_icon).isChecked = true }
            FRAGMENT_TAG_BOOKSHELF -> { bottomNavigationView.findItem(R.id.bookshelf_navi_icon).isChecked = true }
            FRAGMENT_TAG_SEARCH -> { bottomNavigationView.findItem(R.id.search_navi_icon).isChecked = true }
            FRAGMENT_TAG_CHAT -> { bottomNavigationView.findItem(R.id.chat_navi_icon).isChecked = true }
            FRAGMENT_TAG_MY_PAGE -> { bottomNavigationView.findItem(R.id.mypage_navi_icon).isChecked = true }
        }
    }

    private fun inflateFragmentInStack(){
        val fragment = bottomNaviFragmentStack.removeLastOrNull() ?: return
        replaceFragmentInStack(fragment)
    }

    //자식 FragmentBackStack 다 털고 BottomNaviFragmentBackStack 털어야 함
    override fun onBackPressed() {
        Log.d(TAG, "MainActivity: onBackPressed() - called")
        if(supportFragmentManager.backStackEntryCount != 0){
            supportFragmentManager.popBackStackImmediate()
            return
        }
        backPressEvent()
        inflateFragmentInStack()
        updateBottomNaviIcon()
    }

    private fun backPressEvent() {
        if (!bottomNaviFragmentStack.isEmpty()) return

        val toast = Toast.makeText(this, R.string.message_back_press, Toast.LENGTH_SHORT)
        if (System.currentTimeMillis() > backPressedTime + 2000){
            backPressedTime = System.currentTimeMillis()
            toast.show()
            return
        }

        toast.cancel()
        super.onBackPressed()
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

}