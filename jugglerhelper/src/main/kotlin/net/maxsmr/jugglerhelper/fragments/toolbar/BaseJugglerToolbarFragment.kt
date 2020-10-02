package net.maxsmr.jugglerhelper.fragments.toolbar

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import me.ilich.juggler.gui.JugglerActivity
import me.ilich.juggler.gui.JugglerToolbarFragment
import me.ilich.juggler.states.State
import net.maxsmr.commonutils.data.text.EMPTY_STRING
import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment
import net.maxsmr.jugglerhelper.navigation.NavigationMode

abstract class BaseJugglerToolbarFragment : JugglerToolbarFragment() {

    @get:LayoutRes
    protected abstract val layoutId: Int

    protected abstract val navigationMode: NavigationMode

    @Suppress("UNCHECKED_CAST")
    protected open val title: String?
        get() {
            val state = state as State<State.Params?>?
            return if (state != null) {
                state.getTitle(requireContext(), state.params)
            } else {
                requireActivity().title?.toString()
            }
        }

    protected open val logo: Drawable? = null

    protected open val upIcon: Drawable?
        get() {
            val state = state
            return state?.getUpNavigationIcon(requireContext())
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initTitle()
        initLogo()
        initNavigationMode()
        initViews(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val actionBar = jugglerActivity.supportActionBar
        actionBar?.hide()
    }

    @CallSuper
    fun onStateActivated(activity: JugglerActivity?, state: State<*>?) {
        initTitle()
        for (fragment in childFragmentManager.fragments) {
            if (fragment is BaseJugglerFragment) {
                fragment.onStateActivated(activity, state)
            } else if (fragment is BaseJugglerToolbarFragment) {
                fragment.onStateActivated(activity, state)
            }
        }
    }

    protected open fun initViews(view: View) {
        // override if needed
    }

    protected open fun applyTitle(title: CharSequence?) {
        toolbar?.title = title
        jugglerActivity.supportActionBar?.title = title
    }

    protected open fun applyLogo(logo: Drawable?) {
        toolbar?.logo = logo
    }

    protected open fun initToolbar() {
        toolbar?.setNavigationOnClickListener { jugglerActivity.onSupportNavigateUp() }
        jugglerActivity.supportActionBar?.let { actionBar ->
            actionBar.title = EMPTY_STRING
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.show()
        }
    }

    protected open fun initTitle() {
        applyTitle(title)
    }

    // setting logo by layout is not working properly
    protected open fun initLogo() {
        applyLogo(logo)
    }

    protected fun initNavigationMode() {
        applyMode(navigationMode, upIcon)
    }

    private fun applyMode(mode: NavigationMode, upIcon: Drawable?) {

        fun setNavigationIconVisible(b: Boolean) {
            jugglerActivity.supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(b)
                it.setHomeButtonEnabled(b)
            }
        }

        val toolbar = toolbar
        if (toolbar != null) {
            upIcon?.let {
                toolbar.navigationIcon = upIcon
            }
            when (mode) {
                NavigationMode.SANDWICH -> setNavigationIconVisible(true)
                NavigationMode.BACK -> setNavigationIconVisible(true)
                NavigationMode.NONE -> {
                    setNavigationIconVisible(false)
                    toolbar.visibility = View.VISIBLE
                }
                NavigationMode.INVISIBLE -> {
                    setNavigationIconVisible(false)
                    toolbar.visibility = View.GONE
                }
            }
        }
    }
}
