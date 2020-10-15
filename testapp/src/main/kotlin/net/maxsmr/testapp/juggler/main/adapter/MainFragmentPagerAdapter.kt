package net.maxsmr.testapp.juggler.main.adapter

import android.content.Context
import androidx.fragment.app.FragmentManager
import net.maxsmr.commonutils.android.gui.fragments.adapters.CustomFragmentStatePagerAdapter
import net.maxsmr.testapp.R

class MainFragmentPagerAdapter(context: Context, fm: FragmentManager): CustomFragmentStatePagerAdapter(context, fm) {

    init {
        addFragment(TabOneFragment(), R.string.tab_one_title)
        addFragment(TabTwoFragment(), R.string.tab_two_title)
    }
}