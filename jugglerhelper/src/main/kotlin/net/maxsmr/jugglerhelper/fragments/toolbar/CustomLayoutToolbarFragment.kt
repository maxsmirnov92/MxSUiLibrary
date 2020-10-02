package net.maxsmr.jugglerhelper.fragments.toolbar

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import net.maxsmr.jugglerhelper.R
import net.maxsmr.jugglerhelper.navigation.NavigationMode
import java.io.Serializable

private val ARG_TOOLBAR_OPTS = CustomLayoutToolbarFragment::class.java.simpleName + ".ARG_TOOLBAR_OPTS"

class CustomLayoutToolbarFragment : BaseCustomJugglerToolbarFragment() {

    private lateinit var options: ToolbarOptions

    override val navigationMode: NavigationMode
        get() = options.navigationMode

    override val layoutId: Int
        get() = options.layoutId

    override val toolbarTitleId: Int
        get() = options.toolbarTitleId

    override val toolbarLogoId: Int
        get() = options.toolbarLogoId

    override val logo: Drawable?
        get() {
            options.toolbarLogoDrawableResId.let {
                return if (it != 0) ContextCompat.getDrawable(requireContext(), it) else null
            }
        }

    override fun getToolbarId(): Int = options.toolbarId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        options = (arguments?.getSerializable(ARG_TOOLBAR_OPTS) as? ToolbarOptions)
                ?: ToolbarOptions()
    }

    data class ToolbarOptions(
            val navigationMode: NavigationMode = NavigationMode.NONE,
            @LayoutRes val layoutId: Int = R.layout.fragment_standart_toolbar,
            @IdRes val toolbarId: Int = R.id.toolbar,
            @IdRes val toolbarTitleId: Int = 0,
            @IdRes val toolbarLogoId: Int = 0,
            @DrawableRes val toolbarLogoDrawableResId: Int = 0
    ) : Serializable

    companion object {

        fun newInstance(options: ToolbarOptions): CustomLayoutToolbarFragment {
            val args = Bundle()
            args.putSerializable(ARG_TOOLBAR_OPTS, options)
            val fragment = CustomLayoutToolbarFragment()
            fragment.arguments = args
            return fragment
        }
    }
}