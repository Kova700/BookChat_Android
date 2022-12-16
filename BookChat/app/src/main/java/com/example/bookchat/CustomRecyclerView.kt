package com.example.bookchat

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class CustomRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val parentViewPager: ViewPager2?
        get() {
            var view = parent as? View
            while (view != null && view !is ViewPager2) {
                view = view.parent as? View
            }
            return view as? ViewPager2
        }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
         super.onTouchEvent(event)
        if(event?.action == MotionEvent.ACTION_UP) {
            parentViewPager?.isUserInputEnabled = true
        }

        return true
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        parentViewPager?.isUserInputEnabled = (this.scrollState != SCROLL_STATE_DRAGGING)
    }
}