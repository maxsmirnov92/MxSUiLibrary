package net.maxsmr.jugglerhelper.fragments.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.IdRes
import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment

private val ARG_EXPIRED_TIME = BaseSplashJugglerFragment::class.java.name + ".ARG_EXPIRED_TIME"

abstract class BaseSplashJugglerFragment : BaseJugglerFragment() {

    protected abstract val splashTimeout: Long

    @IdRes
    protected open val layoutClickResId = 0

    private val navigateRunnable = Runnable {
        if (isAdded) {
            onSplashTimeout()
        }
    }
    private var expiredTime: Long = 0

    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expiredTime = savedInstanceState?.getLong(ARG_EXPIRED_TIME) ?: expiredTime
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(layoutClickResId) {
            if (this != 0) {
                view.findViewById<View>(this)?.setOnClickListener {
                    mainHandler.removeCallbacks(navigateRunnable)
                    onSplashTimeout()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val timeout = splashTimeout
        require(timeout >= 0) { "incorrect splash timeout" }
        startTime = System.currentTimeMillis() - expiredTime
        mainHandler.postDelayed(navigateRunnable, timeout - expiredTime)
    }

    override fun onStop() {
        super.onStop()
        mainHandler.removeCallbacks(navigateRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(ARG_EXPIRED_TIME, (if (startTime > 0) System.currentTimeMillis() - startTime else 0).also { expiredTime = it })
    }

    protected abstract fun onSplashTimeout()
}