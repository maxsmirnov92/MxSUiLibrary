package net.maxsmr.testapp.juggler.main.adapter

import android.content.Context
import androidx.fragment.app.FragmentManager
import net.maxsmr.jugglerhelper.adapter.CustomFragmentStatePagerAdapter
import net.maxsmr.testapp.R

const val TAG_ONE = "one"
const val TAG_TWO = "two"

class MainFragmentPagerAdapter(context: Context, fm: FragmentManager): CustomFragmentStatePagerAdapter(context, fm) {
    
    init {
        addFragment(FragmentInfo(TabOneFragment(), context.getString(R.string.tab_one_title), TAG_ONE))
        addFragment(FragmentInfo(TabTwoFragment(), context.getString(R.string.tab_two_title), TAG_TWO))
    }
}