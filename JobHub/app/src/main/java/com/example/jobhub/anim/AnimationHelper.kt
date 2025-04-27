package com.example.jobhub.anim

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View

class AnimationHelper {

    companion object {
        fun animateScale(view: View, duration: Long = 100) {
            ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
            ).apply {
                this.duration = duration
                start()
            }
        }
    }
}