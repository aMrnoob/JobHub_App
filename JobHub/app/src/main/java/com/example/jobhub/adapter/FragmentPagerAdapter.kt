package com.example.jobhub.adapter

import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.example.jobhub.R

class FragmentPagerAdapter(
    activity: FragmentActivity,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(activity) {

    private var currentPosition = 0

    fun updateCurrentPosition(position: Int) {
        currentPosition = position
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)

        holder.itemView.animation = AnimationUtils.loadAnimation(
            holder.itemView.context,
            if (position > currentPosition) R.anim.slide_in_right else R.anim.slide_in_left
        )
    }
}