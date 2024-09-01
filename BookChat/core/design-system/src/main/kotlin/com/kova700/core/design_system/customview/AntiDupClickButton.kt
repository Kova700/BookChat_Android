package com.kova700.core.design_system.customview

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

class AntiDupClickButton : MaterialButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var isClicked = false

    override fun callOnClick(): Boolean {
        if(isClicked) return false
        delayCallback()
        return super.callOnClick()
    }

    override fun performClick(): Boolean {
        if(isClicked) return false
        delayCallback()
        return super.performClick()
    }

    private fun delayCallback(){
        isClicked = true
        postDelayed({
            isClicked = false
        }, INTAERVAL)
    }

    companion object{
        private const val INTAERVAL = 400L
    }
}