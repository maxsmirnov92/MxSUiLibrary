package net.maxsmr.jugglerhelper.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabGravity
import me.ilich.juggler.gui.JugglerActivity
import me.ilich.juggler.states.State
import net.maxsmr.jugglerhelper.R
import net.maxsmr.jugglerhelper.adapter.CustomFragmentStatePagerAdapter
import net.maxsmr.jugglerhelper.utils.FragmentSearchParams
import net.maxsmr.jugglerhelper.utils.findFragment
import java.lang.reflect.Field

private val ARG_TAB_FRAGMENT_INDEX = BaseTabsJugglerFragment::class.java.simpleName + ".ARG_TAB_FRAGMENT_INDEX"

private const val INIT_TABS_DELAY_DEFAULT = 200L

/**
 * [BaseJugglerFragment] с использованием фрагментного адаптера [CustomFragmentStatePagerAdapter]
 * ViewPager - обязателен, TabLayout - нет
 */
abstract class BaseTabsJugglerFragment<PagerAdapter : CustomFragmentStatePagerAdapter> : BaseJugglerFragment(), OnPageChangeListener, View.OnClickListener {

    override val layoutId: Int = R.layout.tabs

    @IdRes
    protected open val pagerId: Int = R.id.pager

    @IdRes
    protected open val tabLayoutId: Int = R.id.tabLayout

    @TabGravity
    protected open val tabGravity: Int = TabLayout.GRAVITY_CENTER

    protected open val tabMode: Int = TabLayout.MODE_FIXED

    protected open val initCustomTabViewsDelay: Long = INIT_TABS_DELAY_DEFAULT

    /**
     * Кастомный набор созданных вьюх как альтернатива [TabLayout]
     */
    protected val customViewTabsMap = mutableMapOf<String, View>()

    protected val initialTabFragmentIndex: Int = savedInstanceState?.getInt(ARG_TAB_FRAGMENT_INDEX)
            ?: (arguments?.getInt(ARG_TAB_FRAGMENT_INDEX) ?: 0)

    @Suppress("UNCHECKED_CAST")
    protected val statePagerAdapter: PagerAdapter?
        get() = viewPager.adapter as? PagerAdapter

    protected val currentPagesCount: Int
        get() = statePagerAdapter?.count ?: 0

    protected val currentSelectedPageIndex: Int
        get() = viewPager.currentItem

    protected lateinit var viewPager: ViewPager

    protected var tabLayout: TabLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayoutId.let {
            if (it != 0) {
                tabLayout = view.findViewById(it)
            }
        }
        pagerId.let {
            // required
            viewPager = view.findViewById(it)
                    ?: throw RuntimeException("ViewPager with id $it not found")
        }

        tabLayout?.let {
            with(tabGravity) {
                require(this == TabLayout.GRAVITY_CENTER || this == TabLayout.GRAVITY_FILL) { "incorrect tabGravity: $this" }
                it.tabGravity = this
            }
            with(tabMode) {
                require(this == TabLayout.MODE_FIXED || this == TabLayout.MODE_SCROLLABLE) { "incorrect tabMode: $this" }
                it.tabMode = this
            }
        }
        viewPager.addOnPageChangeListener(this)
        reload()
        selectAdapterPage(initialTabFragmentIndex, true)

        // due to juggler fragments init order (first 'content' fragment)
        mainHandler.postDelayed({
            if (isAdded) {
                initCustomViewTabsMap(customViewTabsMap)
                for (v in customViewTabsMap.values) {
                    v.setOnClickListener(this)
                }
                refreshCustomViewTabsByPager()
            }
        }, initCustomTabViewsDelay)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_TAB_FRAGMENT_INDEX, currentSelectedPageIndex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPager.removeOnPageChangeListener(this)
    }

    override fun onPageSelected(position: Int) {
        refreshCustomViewTabsByPager()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}

    @CallSuper
    override fun onClick(v: View) {
        val adapter = statePagerAdapter
        if (adapter != null) {
            val pair = findCustomViewTabByView(v)
            if (pair != null) {
                var selectedIndex = RecyclerView.NO_POSITION
                for (index in 0 until currentPagesCount) {
                    val fragmentInfo = adapter.getFragmentInfo(index)
                    val tag = fragmentInfo.tag
                    if (!TextUtils.isEmpty(tag) && pair.first == tag) {
                        selectedIndex = index
                        break
                    }
                }
                if (selectedIndex in 0 until currentPagesCount) {
                    viewPager.currentItem = selectedIndex
                }
            }
        }
    }

    override fun onStateActivated(activity: JugglerActivity?, state: State<*>?) {
        super.onStateActivated(activity, state)
        val adapter = statePagerAdapter
        if (adapter != null) {
            notifyStateActivated(activity, state, adapter.fragments)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val adapter = statePagerAdapter
        if (adapter != null) {
            notifyActivityResult(requestCode, resultCode, data, adapter.fragments)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val adapter = statePagerAdapter
        if (adapter != null) {
            notifyRequestPermissionsResult(requestCode, permissions, grantResults, adapter.fragments)
        }
    }

    fun notifyFragmentsChanged() {
        val adapter = statePagerAdapter ?: throw RuntimeException("Adapter is not initialized")
        adapter.isNeedToNotify = true
        adapter.notifyDataSetChanged()
        updateTabIcons()
    }

    fun selectTab(position: Int) {
        val adapter = statePagerAdapter ?: throw RuntimeException("Adapter is not initialized")
        if (position < 0 || position >= adapter.count) {
            throw IndexOutOfBoundsException("Incorrect tab index: $position")
        }
        tabLayout?.let {
            val tab = it.getTabAt(position)
            if (tab != null && !tab.isSelected) {
                tab.select()
            }
        } ?: viewPager.setCurrentItem(position, true)
    }

    fun <F : Fragment> selectTabByFragment(params: FragmentSearchParams<F>) {
        val adapter = statePagerAdapter ?: throw RuntimeException("Adapter is not initialized")
        val pair = findFragment(adapter.fragments, params)
        if (pair?.first != null) {
            selectTab(pair.first)
        }
    }

    protected abstract fun initCustomViewTabsMap(viewTabsMap: MutableMap<String, View>)

    protected abstract fun createStatePagerAdapter(): PagerAdapter

    protected open fun getTabIconForPagerFragment(f: Fragment?): Drawable? = null

    protected fun selectAdapterPage(index: Int, updateIfNotSelected: Boolean) {
        var selected = false
        if (index in 0 until currentPagesCount) {
            if (index != currentSelectedPageIndex) {
                viewPager.setCurrentItem(index, true)
                selected = true
            }
        }
        if (!selected && updateIfNotSelected) {
            invalidatePageSelected()
        }
    }

    protected fun invalidatePageSelected() {
        val position = currentSelectedPageIndex
        if (position in 0 until currentPagesCount) {
            onPageSelected(position)
        }
    }

    protected fun refreshCustomViewTabsByPager() {
        val adapter = statePagerAdapter
        if (adapter != null) {
            if (customViewTabsMap.isNotEmpty()) {
                for (index in 0 until currentPagesCount) {
                    val isSelected = currentSelectedPageIndex == index
                    val fragmentInfo = adapter.getFragmentInfo(index)
                    val tag = fragmentInfo.tag
                    findCustomViewTabByTag(tag)?.let {
                        refreshCustomViewTab(it.second, tag, isSelected)
                    }
                }
            }
        }
    }

    protected fun refreshCustomViewTab(tabView: View, tabViewTag: String?, isSelected: Boolean) {
        tabView.isSelected = isSelected
    }

    protected fun findCustomViewTabByTag(tag: String?): Pair<String, View>? =
            customViewTabsMap.entries.find { it.key == tag }?.let { Pair(it.key, it.value) }

    protected fun findCustomViewTabByView(view: View?): Pair<String, View>? =
            customViewTabsMap.entries.find { it.value == view }?.let { Pair(it.key, it.value) }

    @Deprecated("")
    protected fun setTabsTypeface(alias: String?) {
//    FontsHolder.getInstance().apply(tabView, alias, false)
    }

    protected fun getTabViews(): Map<Int, View> {
        val tabViews = mutableMapOf<Int, View>()
        tabLayout?.let {
            for (i in 0 until it.tabCount) {
                val tab = it.getTabAt(i)
                if (tab != null) {
                    var field: Field? = null
                    try {
                        field = tab.javaClass.getDeclaredField("mView")
                    } catch (e: NoSuchFieldException) {
                        e.printStackTrace()
                    }
                    if (field != null) {
                        try {
                            field.isAccessible = true
                            tabViews[i] = field[tab] as View
                        } catch (e: IllegalAccessException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        return tabViews
    }

    @CallSuper
    protected fun reload() {
        viewPager.adapter = createStatePagerAdapter()
        tabLayout?.setupWithViewPager(viewPager)
        notifyFragmentsChanged()
    }

    protected fun updateTabIcons() {
        tabLayout?.let {
            val adapter = statePagerAdapter
            if (adapter != null) {
                for (i in 0 until adapter.count) {
                    val f = adapter.getFragmentInstance(i)
                    val tabIcon = getTabIconForPagerFragment(f)
                    if (tabIcon != null) {
                        val tab = it.getTabAt(i)
                        if (tab != null) {
                            tab.icon = tabIcon
                        }
                    }
                }
            }
        }
    }

}
