package com.example.bookchat.adapter

import android.widget.RatingBar
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.InverseBindingMethod
import androidx.databinding.InverseBindingMethods
import com.willy.ratingbar.BaseRatingBar
import com.willy.ratingbar.BaseRatingBar.OnRatingChangeListener
import com.willy.ratingbar.ScaleRatingBar

@InverseBindingMethods(InverseBindingMethod(type = ScaleRatingBar::class, attribute = "rating"))
class ScaleRatingBarBindingAdapter {
    companion object{
        @JvmStatic
        @BindingAdapter("app:rating")
        fun setRating(view: RatingBar, rating: Float) {
            if (view.isNotSameRating(rating)) {
                view.rating = rating
            }
        }
        @JvmStatic
        @BindingAdapter(value = ["onRatingChanged", "ratingAttrChanged"], requireAll = false)
        fun setListeners(
            view: ScaleRatingBar, onRatingChangeListener: OnRatingChangeListener?,
            inverseBindingListener: InverseBindingListener?
        ) {
            if (inverseBindingListener.isNull()) {
                view.setOnRatingChangeListener(onRatingChangeListener)
                return
            }
            view.setOnRatingChangeListener { ratingBar: BaseRatingBar?, rating: Float, fromUser: Boolean ->
                onRatingChangeListener?.onRatingChange(ratingBar, rating, fromUser)
                inverseBindingListener?.onChange()
            }
        }

        private fun RatingBar.isNotSameRating(rating: Float) :Boolean =
            this.rating != rating

        private fun InverseBindingListener?.isNull() :Boolean =
            this == null
    }
}