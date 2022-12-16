package com.example.bookchat

import android.graphics.Canvas
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.adapter.CompleteBookTabAdapter
import com.example.bookchat.adapter.ReadingBookTabAdapter
import com.example.bookchat.utils.Constants.TAG
import kotlin.math.max
import kotlin.math.min

class SwipeHelperCallback(private val swipeViewType :SwipeViewType) : ItemTouchHelper.Callback() {

    //# 수정 사항
    //Item 삭제 전 or 재활용 전 스와이프 상태 다시 원상복구
    //꾸욱 누르면 스와이프되게
    //아이템 다시 터치, 다른 곳 터치, 다른 아이템 터시 시 원상복구

    private var currentPosition: Int? = null //현재 선택된 Item position
    private var previousPosition: Int? = null //이전에 선택되었던 Item position
    private var currentDx = 0f
    private var clamp = 0f //스와이프 임계영역
    //다른 아이템 터치했을때 이미 스와이프 되어있는 아이템 되돌리는 애니메이션 구현하고 코드 정리해야함

    //idle, swiping, dragging과 같은 각각의 상태에서 새로운 상태로의 이동 flag를 생성하여 return
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val swipeView = getSwipeView(viewHolder,swipeViewType)
        setClamp(swipeView.width.toFloat() * SWIPE_VIEW_PERCENT)

        val dragFlags = 0
        val swipeFlags = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    //item 이 새로운 위치로 드래그 되었을 때 호출
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    //item이 스와이프 되었을 때 호출되는 메소드
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Log.d(TAG, "SwipeHelperCallback: onSwiped() 스와이프 되었음!!")
        //스와이프 되었을 때 작동 내용 정의
    }

    //drag된 view가 drop되었거나 , swipe가 cancel되거나 complete되었을때 호출된다.
    //상호작용이 종료되고 애니메이션이 종료 된 후에 호출
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        currentDx = 0f
        getDefaultUIUtil().clearView(getSwipeView(viewHolder,swipeViewType))
        previousPosition = viewHolder.absoluteAdapterPosition
    }

    // ItemTouchHelper로 Swipe 또는 Drag and Drop하여 ViewHolder가 변경될 때 호출
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            currentPosition = viewHolder.absoluteAdapterPosition
            getDefaultUIUtil().onSelected(getSwipeView(it,swipeViewType))
        }
    }

    //onChildDraw 함수를 이용해서 뷰 홀더 전체가 아니라 아이템을 특정시켜서 스와이프 할 수 있습니다.
    //깔린 레이아웃과 스와이프될 레이아웃 두종류가 있으므로, 스와이프 될 레이아웃만 스와이프 되도록 구현합니다.
    //움직일 뷰 지정, 뷰의 이동좌표 설정
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, // 스와이프를 시작한 터치 지점에서 얼만큼 좌우로 움직였는지! (왼쪽 = 음수, 오른쪽 = 양수)
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState != ACTION_STATE_SWIPE) return
        val swipeView = getSwipeView(viewHolder,swipeViewType)
        val isClamped = getSwiped(viewHolder,swipeViewType) //현재 아이템이 스와이프 되어있는지 아닌지 판단
        val x = clampViewPositionHorizontal(swipeView, dX, isClamped, isCurrentlyActive)
        currentDx = x
        getDefaultUIUtil().onDraw(c, recyclerView, swipeView, x, dY, actionState, isCurrentlyActive)
    }

    //현재 아이템의 isClamped , isCurrentlyActive인지 여부에 따라 x좌표 반환
    private fun clampViewPositionHorizontal(
        swipeView: View,
        dX: Float,
        isClamped: Boolean,
        isCurrentlyActive: Boolean
    ): Float {
        // swipeView 가로 길이의 절반까지만 RIGHT 방향으로 swipe 되도록
        val maxSwipe: Float = swipeView.width.toFloat() * SWIPE_VIEW_PERCENT
        val minSwipe: Float = 0F
//        Log.d(TAG, "SwipeHelperCallback: clampViewPositionHorizontal() - dX : $dX") //dX는 증가했다가 다시 0으로 돌아감

        val x = if (isClamped) {
            // View가 고정되었을 때 swipe되는 영역 제한
            if (isCurrentlyActive) clamp + dX else clamp
        } else {
            if (dX > 0F) dX else 0F
        }
        //0 혹은 maxSwipe 사이의 값이 나와야 함
        //max(minSwipe, x) == 고정되었을 때 : 임계값 사이값 혹은 임계값 / 고정되지 않았을 때 : 그냥 움직인 X값
        //min( max(minSwipe, x), maxSwipe )  : 움직인 X값과 임계값 중 작은값
        val tempReturnX = min(max(minSwipe, x), maxSwipe)
//        Log.d(TAG, "SwipeHelperCallback: clampViewPositionHorizontal() - isClamped:$isClamped, x :$x , returnX :$tempReturnX")
        return tempReturnX
    }

    //스와이프 Escape 임계 속도 설정
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    //스와이프 escape 임계 범위 설정
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        val isSwiped = getSwiped(viewHolder,swipeViewType)
        // 현재 View가 고정되어있지 않고 사용자가 clamp 이상 swipe시 isClamped true로 변경 아닐시 false로 변경
        setSwiped(viewHolder, !isSwiped && (0 < currentDx) && (currentDx <= clamp), swipeViewType)
        return 2f
    }

    private fun setSwiped(
        viewHolder: RecyclerView.ViewHolder,
        isSwiped: Boolean,
        swipeViewType :SwipeViewType
    ) {
        when(swipeViewType){
            SwipeViewType.Reading -> { (viewHolder as ReadingBookTabAdapter.ReadingBookItemViewHolder).setSwiped(isSwiped) }
            SwipeViewType.Complete -> { (viewHolder as CompleteBookTabAdapter.CompleteBookItemViewHolder).setSwiped(isSwiped) }
        }
    }

    private fun getSwiped(
        viewHolder: RecyclerView.ViewHolder,
        swipeViewType :SwipeViewType
    ) :Boolean{
        return when(swipeViewType){
            SwipeViewType.Reading -> { (viewHolder as ReadingBookTabAdapter.ReadingBookItemViewHolder).getSwiped() }
            SwipeViewType.Complete -> { (viewHolder as CompleteBookTabAdapter.CompleteBookItemViewHolder).getSwiped() }
        }
    }

    fun setClamp(clamp: Float) {
        this.clamp = clamp
    }

    // 다른 View가 swipe 되거나 터치되면 고정 해제
    // (하지만 스와이프 된 Item이 안보이는 곳에서 새로운 Item 스와이프 시에
    // 스와이프 아이템이 늘어나는 이슈는 해결하지 못했음)
    fun removePreviousClamp(recyclerView: RecyclerView) {
        if (currentPosition == previousPosition)
            return
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getSwipeView(viewHolder,swipeViewType).translationX = 0f
            setSwiped(viewHolder, false, swipeViewType)
            previousPosition = null
        }
    }

    //스와이프 될 뷰홀더 내부의 레이아웃 지정
    private fun getSwipeView(
        viewHolder: RecyclerView.ViewHolder,
        swipeViewType :SwipeViewType
    ): View {
        return when(swipeViewType) {
            SwipeViewType.Reading -> {
                (viewHolder as ReadingBookTabAdapter.ReadingBookItemViewHolder).itemView
                    .findViewById(R.id.swipe_view)
            }
            SwipeViewType.Complete -> {
                (viewHolder as CompleteBookTabAdapter.CompleteBookItemViewHolder).itemView
                    .findViewById(R.id.swipe_view)
            }
        }
    }

    sealed class SwipeViewType{
        object Reading :SwipeViewType()
        object Complete :SwipeViewType()
    }

    companion object {
        private const val SWIPE_VIEW_PERCENT = 0.3F
    }

}