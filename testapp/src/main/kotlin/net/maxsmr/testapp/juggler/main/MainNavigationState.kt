package net.maxsmr.testapp.juggler.main

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import me.ilich.juggler.grid.Cell
import me.ilich.juggler.grid.Grid
import me.ilich.juggler.gui.JugglerFragment
import me.ilich.juggler.states.ContentNavigationState
import me.ilich.juggler.states.ContentToolbarNavigationEndState
import me.ilich.juggler.states.State
import me.ilich.juggler.states.VoidParams
import net.maxsmr.jugglerhelper.fragments.toolbar.CommonToolbarFragment
import net.maxsmr.jugglerhelper.navigation.NavigationMode
import net.maxsmr.testapp.R

class MainNavigationState : ContentToolbarNavigationEndState<VoidParams>(VoidParams.instance()) {

    override fun onConvertContent(params: VoidParams, fragment: JugglerFragment?) = MainFragment()

    override fun onConvertToolbar(params: VoidParams, fragment: JugglerFragment?): JugglerFragment =
            CommonToolbarFragment.newInstance(CommonToolbarFragment.ToolbarOptions(NavigationMode.SANDWICH))

    override fun onConvertNavigation(params: VoidParams, fragment: JugglerFragment?): JugglerFragment =
            MainNavigationFragment.newInstance()

    override fun getUpNavigationIcon(context: Context, params: VoidParams?): Drawable?
            = AppCompatResources.getDrawable(context, R.drawable.ic_menu)

    override fun getTitle(context: Context, params: VoidParams?): String = context.getString(R.string.main_title)

    override fun getTag(): String? = MainNavigationState::class.java.simpleName
}
