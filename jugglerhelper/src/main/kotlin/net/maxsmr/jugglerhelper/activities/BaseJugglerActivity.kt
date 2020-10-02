package net.maxsmr.jugglerhelper.activities

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import me.ilich.juggler.Juggler
import me.ilich.juggler.gui.JugglerActivity
import me.ilich.juggler.states.State
import net.maxsmr.commonutils.android.gui.fragments.FragmentFinder
import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment

abstract class BaseJugglerActivity : JugglerActivity() {

    @AnimRes
    protected open val finishEnterAnimation: Int = 0

    @AnimRes
    protected open val finishExitAnimation: Int = 0

    protected val activityState: State<*>?
        get() {
            val intent = intent
                    ?: throw IllegalStateException("Intent is null")
            val state: State<*>
            state = if (intent.hasExtra(EXTRA_STATE)) {
                getIntent().getSerializableExtra(EXTRA_STATE) as State<*>
            } else {
                createState()
            }
            return state
        }

    var isCommitAllowed = false
        private set

    var isActivityResumed = false
        private set

    protected var initialState: State<*>? = null
        private set

    protected var savedInstanceState: Bundle? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        intent.putExtra(Juggler.DATA_ANIMATION_FINISH_ENTER, finishEnterAnimation)
        intent.putExtra(Juggler.DATA_ANIMATION_FINISH_EXIT, finishExitAnimation)
        initialState = activityState
        intent.putExtra(EXTRA_STATE, overrideInitialState(initialState))
        this.savedInstanceState = savedInstanceState
        isCommitAllowed = true
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedInstanceState = outState
        isCommitAllowed = false
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        isCommitAllowed = true
    }

    override fun onPause() {
        super.onPause()
        isActivityResumed = false
    }

    override fun onResume() {
        super.onResume()
        isActivityResumed = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        for (f in supportFragmentManager.fragments) {
            if (f is BaseJugglerFragment && f.isAdded()) {
                f.onTouchEvent(event)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        for (f in supportFragmentManager.fragments) {
            if (f is BaseJugglerFragment && f.isAdded()) {
                f.onKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (f in supportFragmentManager.fragments) {
            if (f != null && f.isAdded) {
                f.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    fun findFragmentById(id: Int): Fragment? =
            FragmentFinder.findFragmentById(supportFragmentManager, id)?.second

    fun findFragmentByTag(tag: String?): Fragment? =
            FragmentFinder.findFragmentByTag(supportFragmentManager, tag)?.second

    fun <F : Fragment?> findFragmentByClass(clazz: Class<F>?): F? =
            FragmentFinder.findFragmentByClass(supportFragmentManager, clazz)?.second

    protected open fun overrideInitialState(state: State<*>?): State<*>? = state
}
