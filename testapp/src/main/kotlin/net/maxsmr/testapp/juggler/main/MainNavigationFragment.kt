package net.maxsmr.testapp.juggler.main

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.GravityCompat
import me.ilich.juggler.change.NewActivityAdd
import net.maxsmr.jugglerhelper.fragments.BaseJugglerNavigationFragment
import net.maxsmr.testapp.R
import net.maxsmr.testapp.juggler.custom.CustomState
import org.jetbrains.annotations.NotNull

class MainNavigationFragment : BaseJugglerNavigationFragment() {

    private lateinit var navigationHeaderView: ImageView

    override val layoutId: Int = R.layout.fragment_main_navigation

    override val drawerGravity: Int = GravityCompat.END

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationHeaderView= view.findViewById(R.id.ivNavigationHeader)
        navigationHeaderView.setOnClickListener {
            navigateTo().state(NewActivityAdd(CustomState("Custom title")))
        }
    }
    override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
//        closeDrawer()
    }

    companion object {

        @NotNull
        fun newInstance() : MainNavigationFragment {
            val args = Bundle()
            addDrawerGravityToBundle(args, GravityCompat.END)
            val fragment = MainNavigationFragment()
            fragment.arguments = args
            return fragment
        }
    }
}