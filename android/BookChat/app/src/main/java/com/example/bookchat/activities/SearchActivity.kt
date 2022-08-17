package com.example.bookchat.activities

import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.adapter.SearchHistoryAdapter
import com.example.bookchat.databinding.ActivitySearchBinding
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.SearchType
import com.example.bookchat.utils.SharedPreferenceManager
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchBinding
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private var optionType = SearchType.TITLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        binding.lifecycleOwner = this@SearchActivity
        binding.activity = this@SearchActivity
        //binding.drawerViewModel = OptionDrawerViewModel() //옵션 사라질 예정.. (ㅠㅠ)

        initSearchWindow()
        initHistoryRcyV()
        initOptionSlide()
    }

    fun initSearchWindow(){
        with(binding){
            //검색창 엔터이벤트 등록
            searchTextEt.setOnEditorActionListener { _, _, _ ->
                if(searchTextEt.text.toString().isNotEmpty()){
                    SharedPreferenceManager.setSearchHistory(searchTextEt.text.toString()) // 검색어 저장
                    openResult() //페이지 이동
                }else{
                    Toast.makeText(this@SearchActivity,"검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
    }
    fun initHistoryRcyV(){
        //검색기록 RcyV설정
        searchHistoryAdapter = SearchHistoryAdapter(SharedPreferenceManager.getSearchHistory())
        searchHistoryAdapter.setItemClickListener( object :SearchHistoryAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(this@SearchActivity, SearchResultActivity::class.java)
                intent.putExtra("SearchKeyWord",SharedPreferenceManager.getSearchHistory().get(position))
                intent.putExtra("OptionType",optionType)
                startActivity(intent)
            }
        })

        with(binding.SearchHistoryRycv){
            adapter = searchHistoryAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@SearchActivity,
                RecyclerView.VERTICAL,false)
        }
    }
    fun initOptionSlide(){
        //옵션 슬라이드 설정
        binding.optionFrame.addPanelSlideListener( object : SlidingUpPanelLayout.PanelSlideListener{
            //패널이 슬라이드 중일 때
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
            }
            //패널의 상태가 변했을 때 (올라왔거나 내려갔거나)
            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    binding.optionFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                }else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                    Toast.makeText(this@SearchActivity,"검색 필터를 설정해주세요.",Toast.LENGTH_SHORT).show()
                }
            }
        })
        //초기값 숨김 지정
        binding.optionFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
    }


    //검색창 클릭 이벤트(열기)
    fun clickSearchWindow(){

        //검색창 이동 애니메이션
        AnimatorInflater.loadAnimator( this,
            R.animator.move_search_window_open
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart { Log.d(TAG, "SearchActivity: clickSearchWindow() - AnimationStart") }
            doOnEnd { Log.d(TAG, "SearchActivity: clickSearchWindow() - AnimationEnd") }
            binding.SearchWindow.pivotX = 0.0f
            binding.SearchWindow.pivotY = 0.0f
            setTarget(binding.SearchWindow)
            start()
        }

        //옵션버튼 이동 애니메이션
        AnimatorInflater.loadAnimator( this,
            R.animator.move_search_option_btn_open
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart { Log.d(TAG, "SearchActivity: clickSearchWindow() - btn_AnimationStart") }
            doOnEnd { Log.d(TAG, "SearchActivity: clickSearchWindow() - btn_AnimationEnd") }
            setTarget(binding.searchOptionBtn)
            start()
        }

        //애니메이션 작동시 노출View 설정
        Handler(Looper.getMainLooper()).postDelayed({
            with(binding){
                SearchText.visibility = INVISIBLE
                searchTextTv.visibility = INVISIBLE
                searchTextEt.visibility = VISIBLE

                // 키보드 포커스 & 키보드 올리기
                searchTextEt.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchTextEt, SHOW_IMPLICIT)

                searchWindowDeleteBtn.visibility = VISIBLE
                searchWindowCencelBtn.visibility = VISIBLE
                recentSearchTitle.visibility = VISIBLE

                //검색기록 노출
                SearchHistoryRycv.visibility = VISIBLE

            }}, 300L)
    }

    //검색창 클릭 이벤트(닫기)
    fun clickCancleBtn(){

        //검색창 이동 애니메이션
        AnimatorInflater.loadAnimator( this,
            R.animator.move_search_window_close
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart { Log.d(TAG, "SearchActivity: clickCancleBtn() - AnimationStart") }
            doOnEnd { Log.d(TAG, "SearchActivity: clickCancleBtn() - AnimationEnd") }
            binding.SearchWindow.pivotX = 0.0f
            binding.SearchWindow.pivotY = 0.0f
            setTarget(binding.SearchWindow)
            start()
        }

        //옵션버튼 이동 애니메이션
        AnimatorInflater.loadAnimator( this,
            R.animator.move_search_option_btn_close
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart { Log.d(TAG, "SearchActivity: clickCancleBtn() - btn_AnimationStart") }
            doOnEnd { Log.d(TAG, "SearchActivity: clickCancleBtn() - btn_AnimationEnd") }
            setTarget(binding.searchOptionBtn)
            start()
        }

        //애니메이션 작동시 노출View 설정
        Handler(Looper.getMainLooper()).postDelayed({
            with(binding){
                SearchText.visibility = VISIBLE
                searchTextTv.visibility = VISIBLE
                searchTextEt.visibility = INVISIBLE
                clearSearchText()

                // 키보드 내리기
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchTextEt.windowToken, 0)

                searchWindowDeleteBtn.visibility = INVISIBLE
                searchWindowCencelBtn.visibility = INVISIBLE
                recentSearchTitle.visibility = INVISIBLE

                //검색기록 숨기기
                SearchHistoryRycv.visibility = INVISIBLE

            }}, 100L
        )
    }

    //뒤로가기 버튼을 눌렀을 때
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(binding.optionFrame.panelState == SlidingUpPanelLayout.PanelState.EXPANDED){
                binding.optionFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    //키보드가 아닌 다른곳 누르면 키보드 내림 (작동 확인 필요)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

    fun clearSearchText(){
        binding.searchTextEt.setText("")
    }

    fun openResult(){
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra("SearchKeyWord",binding.searchTextEt.text.toString())
        intent.putExtra("OptionType",optionType)
        startActivity(intent)
    }

    fun clearHistory(){
        SharedPreferenceManager.clearSearchHistory()
        //리사이클러뷰 갱신
        searchHistoryAdapter.searchHistoryList = SharedPreferenceManager.getSearchHistory()
        searchHistoryAdapter.notifyDataSetChanged()
    }

    fun clickOptionBtn(){
        if(binding.optionFrame.panelState == SlidingUpPanelLayout.PanelState.HIDDEN){
            binding.optionFrame.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
            return
        }
    }

    //옵션 레거시 코드
    fun clizckOptionItem(view : View){

        when(view.id){
            R.id.bookNameOption_btn -> {
                binding.optionFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                optionType = SearchType.TITLE
                changeColorElseOptionBtn(view)
            }
            R.id.authorNameOption_btn -> {
                binding.optionFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                optionType = SearchType.AUTHOR
                changeColorElseOptionBtn(view)
            }
            R.id.isbnOption_btn ->{
                binding.optionFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                optionType = SearchType.ISBN
                changeColorElseOptionBtn(view)

            }
            R.id.chatRoomNameOption_btn ->{
                binding.optionFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                optionType = SearchType.CHATROOMNAME
                changeColorElseOptionBtn(view)
            }
            else -> return
        }
    }

    fun changeColorElseOptionBtn(view : View){
        var textview = view as TextView
        textview.setTextColor(Color.parseColor("#12121D"))
    }


    override fun onRestart() {
        Log.d(TAG, "SearchActivity: onRestart() - 생명주기")
        //Restart() -> Start() -> Resume() 순서로 돌아옴 (돌아올 때 RecyclerView 갱신해야함)
        //EditText 초기화
        clearSearchText()
        //리사이클러뷰 갱신
        initHistoryRcyV()
        searchHistoryAdapter.notifyDataSetChanged()

        super.onRestart()
    }

    override fun onStart() {
        Log.d(TAG, "SearchActivity: onStart() - 생명주기")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "SearchActivity: onResume() - 생명주기")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "SearchActivity: onPause() - 생명주기")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "SearchActivity: onStop() - 생명주기")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "SearchActivity: onDestroy() - 생명주기")
        super.onDestroy()
    }
}