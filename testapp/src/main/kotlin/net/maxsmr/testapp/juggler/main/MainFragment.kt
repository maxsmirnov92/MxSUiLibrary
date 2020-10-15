package net.maxsmr.testapp.juggler.main

import android.view.View
import net.maxsmr.jugglerhelper.fragments.BaseTabsJugglerFragment
import net.maxsmr.testapp.juggler.main.adapter.MainFragmentPagerAdapter

class MainFragment : BaseTabsJugglerFragment<MainFragmentPagerAdapter>() {

    override fun fillCustomViewTabsMap(viewTabsMap: Map<View, String>) {
    }

    override fun createStatePagerAdapter() = MainFragmentPagerAdapter(requireContext(), childFragmentManager)
}