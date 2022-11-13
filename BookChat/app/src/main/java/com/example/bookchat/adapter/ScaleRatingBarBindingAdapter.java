package com.example.bookchat.adapter;

import android.widget.RatingBar;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;

import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

@InverseBindingMethods({
        @InverseBindingMethod(type = ScaleRatingBar.class, attribute = "rating"),
})
public class ScaleRatingBarBindingAdapter {
    @BindingAdapter("app:rating")
    public static void setRating(RatingBar view, float rating) {
        if (view.getRating() != rating) {
            view.setRating(rating);
        }
    }
    @BindingAdapter(value = {"onRatingChanged", "ratingAttrChanged"},
            requireAll = false)
    public static void setListeners(ScaleRatingBar view, final ScaleRatingBar.OnRatingChangeListener listener,
                                    final InverseBindingListener ratingChange) {
        if (ratingChange == null) {
            view.setOnRatingChangeListener(listener);
        } else {
            view.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
                @Override
                public void onRatingChange(BaseRatingBar ratingBar, float rating, boolean fromUser) {
                    if (listener != null) {
                        listener.onRatingChange(ratingBar, rating, fromUser);
                    }
                    ratingChange.onChange();
                }
            });
        }
    }
}