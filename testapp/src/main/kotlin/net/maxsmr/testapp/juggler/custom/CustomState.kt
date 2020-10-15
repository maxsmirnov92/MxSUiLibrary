package net.maxsmr.testapp.juggler.custom

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import me.ilich.juggler.grid.Cell
import me.ilich.juggler.grid.Cell.CELL_TYPE_TOOLBAR
import me.ilich.juggler.grid.Grid
import me.ilich.juggler.gui.JugglerFragment
import me.ilich.juggler.states.State
import net.maxsmr.jugglerhelper.fragments.toolbar.CommonToolbarFragment
import net.maxsmr.jugglerhelper.navigation.NavigationMode
import net.maxsmr.testapp.R

private const val CELL_TYPE_PART_ONE = 5
private const val CELL_TYPE_PART_TWO = 6
private const val CELL_TYPE_PART_THREE = 7

class CustomState(title: String) : State<CustomState.CustomParams>(Grid.custom(R.layout.juggler_layout_content_custom,
        Cell.toolbar(),
        Cell.custom(R.id.container_part_one, CELL_TYPE_PART_ONE),
        Cell.custom(R.id.container_part_two, CELL_TYPE_PART_TWO),
        Cell.custom(R.id.container_part_three, CELL_TYPE_PART_THREE)), CustomParams(title)) {

    override fun onConvertFragment(cellType: Int, params: CustomParams, fragment: JugglerFragment?): JugglerFragment {
        return when (cellType) {
            CELL_TYPE_TOOLBAR -> onConvertToolbar(params, fragment)
            CELL_TYPE_PART_ONE -> CustomOneFragment()
            CELL_TYPE_PART_TWO -> CustomTwoFragment()
            CELL_TYPE_PART_THREE -> CustomThreeFragment()
            else -> throw IllegalStateException("Unknown cell type: $cellType")
        }
    }

    protected fun onConvertToolbar(params: CustomParams?, fragment: JugglerFragment?): JugglerFragment = CommonToolbarFragment.newInstance(
            CommonToolbarFragment.ToolbarOptions(NavigationMode.BACK, R.layout.fragment_toolbar_custom_title, R.id.toolbar, R.id.tvToolbarTitle, R.id.ivToolbarLogo, R.drawable.ic_logo_custom)
    )

    override fun getUpNavigationIcon(context: Context, params: CustomParams?): Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_white)

    override fun getTitle(context: Context, params: CustomParams): String? {
        return params.title
    }

    data class CustomParams(val title: String) : Params()
}
