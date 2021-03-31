package net.maxsmr.testapp.juggler.main

import android.os.Bundle
import android.view.View
import net.maxsmr.jugglerhelper.fragments.BaseTabsJugglerFragment
import net.maxsmr.testapp.R
import net.maxsmr.testapp.juggler.main.adapter.MainFragmentPagerAdapter
import net.maxsmr.testapp.juggler.main.adapter.TAG_ONE
import net.maxsmr.testapp.juggler.main.adapter.TAG_TWO

class MainFragment : BaseTabsJugglerFragment<MainFragmentPagerAdapter>() {

    private lateinit var tabOneView: View
    private lateinit var tabTwoView: View

    override val initCustomTabViewsDelay: Long = 0

    override val layoutId: Int = R.layout.fragment_main

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabOneView = view.findViewById(R.id.viewTabOne)
        tabTwoView = view.findViewById(R.id.viewTabTwo)
    }

    override fun initCustomViewTabsMap(viewTabsMap: MutableMap<String, View>) {
        viewTabsMap[TAG_ONE] = tabOneView
        viewTabsMap[TAG_TWO] = tabTwoView
    }

    override fun createStatePagerAdapter() = MainFragmentPagerAdapter(requireContext(), childFragmentManager)
}