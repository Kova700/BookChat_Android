package com.example.bookchat

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class ViewPagerInnerRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )
    private val parentViewPager by lazy { getParentViewPager2() }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        if (isFingerUpEvent(event)) {
            setParentViewPagerEnable(true)
        }
        return true
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        setParentViewPagerEnable(this.scrollState != SCROLL_STATE_DRAGGING)
    }

    private fun setParentViewPagerEnable(flag :Boolean){
        parentViewPager?.isUserInputEnabled = flag
    }

    private fun isFingerUpEvent(event: MotionEvent?) =
        event?.action == MotionEvent.ACTION_UP

    private fun getParentViewPager2(): ViewPager2?{
        var view :View? = this
        while (view !is ViewPager2) {
            view = view?.parent as? View
                ?: return null
        }
        return view
    }
}