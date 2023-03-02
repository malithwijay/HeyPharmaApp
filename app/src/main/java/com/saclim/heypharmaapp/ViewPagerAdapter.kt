package com.saclim.heypharmaapp


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager):FragmentPagerAdapter(fragmentManager) {

    private val fragments = listOf(
        OnBoarding1Fragment(),
        OnBoarding2Fragment(),
        OnBoarding3Fragment()

    )

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }



}