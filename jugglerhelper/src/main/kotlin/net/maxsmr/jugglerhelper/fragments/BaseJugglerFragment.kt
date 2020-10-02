package net.maxsmr.jugglerhelper.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import me.ilich.juggler.gui.JugglerActivity
import me.ilich.juggler.gui.JugglerFragment
import me.ilich.juggler.states.State
import net.maxsmr.commonutils.android.gui.fragments.FragmentFinder
import net.maxsmr.jugglerhelper.R
import net.maxsmr.jugglerhelper.activities.BaseJugglerActivity
import net.maxsmr.jugglerhelper.fragments.toolbar.BaseJugglerToolbarFragment

abstract class BaseJugglerFragment : JugglerFragment() {

    @get:LayoutRes
    protected abstract val layoutId: Int

    protected open val screenOrientation: Int? = null

    protected open val windowBackground: Drawable? = null

    @ColorInt
    protected open val statusBarColor: Int = ContextCompat.getColor(requireContext(), R.color.colorStatusBar)

    @ColorInt
    protected open val navigationBarColor: Int = ContextCompat.getColor(requireContext(), R.color.colorNavigationBar)

    protected val mainHandler = Handler(Looper.getMainLooper())

    protected var savedInstanceState: Bundle? = null
        private set

    private var isCommitAllowed = false

    fun isCommitAllowed(): Boolean = isAdded && isCommitAllowed

    override fun getJugglerActivity(): BaseJugglerActivity =
            super.getJugglerActivity() as BaseJugglerActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        isCommitAllowed = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupWindow()
    }

    override fun onResume() {
        super.onResume()
        isCommitAllowed = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isCommitAllowed = false
    }

    override fun onBackPressed(): Boolean {
        if (isAdded) {
            for (f in childFragmentManager.fragments) {
                if (f is BaseJugglerFragment && f.isAdded()) {
                    if (f.onBackPressed()) {
                        return true
                    }
                }
            }
        }
        return false
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        isCommitAllowed = true
        notifyActivityResult(requestCode, resultCode, data, childFragmentManager.fragments)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        notifyRequestPermissionsResult(requestCode, permissions, grantResults, childFragmentManager.fragments)
    }

    @CallSuper
    open fun onStateActivated(activity: JugglerActivity?, state: State<*>?) {
        notifyStateActivated(activity, state, childFragmentManager.fragments)
    }

    @CallSuper
    fun onTouchEvent(event: MotionEvent?) {
        if (isAdded) {
            for (f in childFragmentManager.fragments) {
                if (f is BaseJugglerFragment && f.isAdded()) {
                    f.onTouchEvent(event)
                }
            }
        }
    }

    @CallSuper
    fun onKeyDown(keyCode: Int, e: KeyEvent?) {
        if (isAdded) {
            for (f in childFragmentManager.fragments) {
                if (f is BaseJugglerFragment && f.isAdded()) {
                    f.onKeyDown(keyCode, e)
                }
            }
        }
    }

    fun findChildFragmentById(id: Int): Fragment? =
            FragmentFinder.findFragmentById(childFragmentManager, id)?.second

    fun findChildFragmentByTag(tag: String?): Fragment? =
            FragmentFinder.findFragmentByTag(childFragmentManager, tag)?.second

    fun <F : Fragment?> findChildFragmentByClass(clazz: Class<F>?): F? =
            FragmentFinder.findFragmentByClass(childFragmentManager, clazz)?.second

    fun findRootFragmentById(id: Int): Fragment? =
            jugglerActivity.findFragmentById(id)

    fun findRootFragmentByTag(tag: String?): Fragment? =
            jugglerActivity.findFragmentByTag(tag)

    fun <F : Fragment?> findRootFragmentByClass(clazz: Class<F>?): F? =
            jugglerActivity.findFragmentByClass(clazz)

    protected open fun initViews(view: View) {
        // override if needed
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <P : State.Params> getParams(): P? {
        state?.let {
            return try {
                it.params as? P
            } catch (e: ClassCastException) {
                null
            }
        }
        return null
    }

    protected fun <P : State.Params> getParamsOrThrow(): P =
            getParams() ?: throw NullPointerException("Params not defined")

    @Suppress("UNCHECKED_CAST")
    protected fun <T : BaseJugglerActivity> getBaseActivity(): T =
            jugglerActivity as T

    @SuppressLint("WrongConstant")
    protected fun setupWindow() {
        with(requireActivity()) {
            val orientation = screenOrientation
            if (orientation != null) {
                requestedOrientation = orientation
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBarColor.let {
                    if (it != 0) {
                        window.statusBarColor = it
                    }
                }
                navigationBarColor.let {
                    if (it != 0) {
                        window.navigationBarColor = it
                    }
                }
            }
            windowBackground?.let {
                window.setBackgroundDrawable(it)
            }
        }
    }

    protected fun notifyStateActivated(activity: JugglerActivity?, state: State<*>?, fragments: Collection<Fragment?>?) {
        if (isAdded) {
            if (fragments != null) {
                for (fragment in fragments) {
                    if (fragment != null && fragment.isAdded) {
                        if (fragment is BaseJugglerFragment) {
                            fragment.onStateActivated(activity, state)
                        } else if (fragment is BaseJugglerToolbarFragment) {
                            fragment.onStateActivated(activity, state)
                        }
                    }
                }
            }
        }
    }

    protected fun notifyActivityResult(requestCode: Int, resultCode: Int, data: Intent?, fragments: Collection<Fragment?>?) {
        if (isAdded) {
            if (fragments != null) {
                for (f in fragments) {
                    if (f != null && f.isAdded) {
                        f.onActivityResult(requestCode, resultCode, data)
                    }
                }
            }
        }
    }

    protected fun notifyRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray, fragments: Collection<Fragment?>?) {
        if (isAdded) {
            if (fragments != null) {
                for (f in fragments) {
                    if (f != null && f.isAdded) {
                        f.onRequestPermissionsResult(requestCode, permissions, grantResults)
                    }
                }
            }
        }
    }
}

