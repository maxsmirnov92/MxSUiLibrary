package net.maxsmr.jugglerhelper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import me.ilich.juggler.gui.JugglerNavigationFragment

abstract class BaseJugglerNavigationFragment : JugglerNavigationFragment(), DrawerListener {

    @get:LayoutRes
    protected abstract val layoutId: Int

    protected open val drawerGravity: Int = GravityCompat.START

    val isDrawerOpen: Boolean
        get() = drawerLayout.isDrawerOpen(drawerGravity)

    protected var savedInstanceState: Bundle? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drawerLayout.addDrawerListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        drawerLayout.removeDrawerListener(this)
    }

    override fun onBackPressed(): Boolean {
        return changeDrawerState(false) || super.onBackPressed()
    }

    override fun onUpPressed(): Boolean {
        return changeDrawerState(true) || super.onUpPressed()
    }

    override fun getDrawerLayout(): DrawerLayout {
        return super.getDrawerLayout()
                ?: throw RuntimeException(DrawerLayout::class.java.simpleName + " is not initialized")
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

    override fun onDrawerOpened(drawerView: View) {
//        hideKeyboard(activity)
    }

    override fun onDrawerClosed(drawerView: View) {
//        hideKeyboard(activity)
    }

    override fun onDrawerStateChanged(newState: Int) {}

    override fun openDrawer() {
        changeDrawerState(true)
    }

    override fun closeDrawer() {
        changeDrawerState(false)
    }

    fun revertDrawerState() {
        val drawerGravity = drawerGravity
        val drawerLayout = drawerLayout
        if (drawerLayout.isDrawerOpen(drawerGravity)) {
            drawerLayout.closeDrawer(drawerGravity)
        } else {
            drawerLayout.openDrawer(drawerGravity)
        }
    }

    private fun changeDrawerState(open: Boolean): Boolean {
        val drawerGravity = drawerGravity
        val drawerLayout = drawerLayout
        if (!open && drawerLayout.isDrawerOpen(drawerGravity)) {
            drawerLayout.closeDrawer(drawerGravity)
            return true
        } else if (open && !drawerLayout.isDrawerOpen(drawerGravity)) {
            drawerLayout.openDrawer(drawerGravity)
            return true
        }
        return false
    }
}
