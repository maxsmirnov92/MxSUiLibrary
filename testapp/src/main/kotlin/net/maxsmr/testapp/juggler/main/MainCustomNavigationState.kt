package net.maxsmr.testapp.juggler.main

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import me.ilich.juggler.grid.Cell
import me.ilich.juggler.grid.Grid
import me.ilich.juggler.gui.JugglerFragment
import me.ilich.juggler.states.State
import me.ilich.juggler.states.VoidParams
import net.maxsmr.jugglerhelper.fragments.toolbar.CommonToolbarFragment
import net.maxsmr.jugglerhelper.navigation.NavigationMode
import net.maxsmr.testapp.R

class MainCustomNavigationState : State<VoidParams>(Grid.custom(R.layout.juggler_layout_content_main,
        Cell.content(), Cell.toolbar(), Cell.navigation()), VoidParams.instance()) {

    override fun onConvertFragment(cellType: Int, params: VoidParams, fragment: JugglerFragment?): JugglerFragment? {
        return when (cellType) {
            Cell.CELL_TYPE_CONTENT -> onConvertContent(params, fragment)
            Cell.CELL_TYPE_TOOLBAR -> onConvertToolbar(params, fragment)
            Cell.CELL_TYPE_NAVIGATION -> onConvertNavigation(params, fragment)
            else -> throw IllegalStateException("Unknown cell type: $cellType")
        }
    }

    protected fun onConvertContent(params: VoidParams, fragment: JugglerFragment?) = MainFragment()

    protected fun onConvertToolbar(params: VoidParams, fragment: JugglerFragment?): JugglerFragment =
            CommonToolbarFragment.newInstance(CommonToolbarFragment.ToolbarOptions(NavigationMode.SANDWICH))

    protected fun onConvertNavigation(params: VoidParams, fragment: JugglerFragment?): JugglerFragment =
            MainNavigationFragment.newInstance()

    override fun getUpNavigationIcon(context: Context, params: VoidParams?): Drawable?
            = AppCompatResources.getDrawable(context, R.drawable.ic_menu)

    override fun getTitle(context: Context, params: VoidParams?): String? = context.getString(R.string.main_title)

    override fun getTag(): String? = MainCustomNavigationState::class.java.simpleName
}
