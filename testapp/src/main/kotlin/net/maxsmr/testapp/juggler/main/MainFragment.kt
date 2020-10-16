package net.maxsmr.testapp.juggler.main

import android.view.View
import kotlinx.android.synthetic.main.fragment_main.*
import net.maxsmr.jugglerhelper.fragments.BaseTabsJugglerFragment
import net.maxsmr.testapp.R
import net.maxsmr.testapp.juggler.main.adapter.MainFragmentPagerAdapter
import net.maxsmr.testapp.juggler.main.adapter.TAG_ONE
import net.maxsmr.testapp.juggler.main.adapter.TAG_TWO

class MainFragment : BaseTabsJugglerFragment<MainFragmentPagerAdapter>() {

    override val initCustomTabViewsDelay: Long = 0

    override val layoutId: Int = R.layout.fragment_main

    override fun initCustomViewTabsMap(viewTabsMap: MutableMap<String, View>) {
        viewTabsMap[TAG_ONE] = viewTabOne
        viewTabsMap[TAG_TWO] = viewTabTwo
    }

    override fun createStatePagerAdapter() = MainFragmentPagerAdapter(requireContext(), childFragmentManager)
}