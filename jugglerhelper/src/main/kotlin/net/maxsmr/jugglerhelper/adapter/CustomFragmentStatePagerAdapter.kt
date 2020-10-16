package net.maxsmr.jugglerhelper.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import java.util.*

open class CustomFragmentStatePagerAdapter(private val context: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    val isEmpty: Boolean
        get() = count == 0

    val fragments: List<Fragment> get() {
        val fragments = mutableListOf<Fragment>()
        for (p in this.fragmentInfos) {
            fragments.add(p.fragment)
        }
        return fragments
    }

    val fragmentInfos = mutableListOf<FragmentInfo>()

    private val fragmentStateMap: MutableMap<Fragment, Boolean> = HashMap()

    var isNeedToNotify = true

    var listener: FragmentStateListener? = null

    fun getFragmentInfo(position: Int): FragmentInfo {
        rangeCheck(position)
        return this.fragmentInfos[position]
    }

    fun getFragmentInstance(position: Int): Fragment = getFragmentInfo(position).fragment

    fun getFragmentTitle(position: Int): String? = getFragmentInfo(position).title

    fun addFragments(infos: List<FragmentInfo>?) {
        if (infos != null) {
            this.fragmentInfos.addAll(infos)
            notifyDataSetChanged()
        }
    }

    fun addFragment(p: FragmentInfo?) {
        if (p != null) {
            this.fragmentInfos.add(p)
            notifyDataSetChanged()
        }
    }

    fun setFragments(fragments: Collection<FragmentInfo>?) {
        this.fragmentInfos.clear()
        if (fragments != null) {
            this.fragmentInfos.addAll(fragments)
        }
        notifyDataSetChanged()
    }

    fun removeFragment(position: Int): Fragment? {
        rangeCheck(position)
        val pair = this.fragmentInfos[position]
        val removed = pair.fragment
        this.fragmentInfos.removeAt(position)
        notifyDataSetChanged()
        return removed
    }

    fun clear() {
        this.fragmentInfos.clear()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return this.fragmentInfos.size
    }

    override fun getItem(position: Int): Fragment = getFragmentInstance(position)

    override fun getPageTitle(position: Int): CharSequence? = getFragmentTitle(position)

    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        rangeCheck(position)
        val pair = this.fragmentInfos[position]
        fragmentStateMap[pair.fragment] = true
        return super.instantiateItem(container, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        rangeCheck(position)
        val pair = this.fragmentInfos[position]
        fragmentStateMap[pair.fragment] = false
        super.destroyItem(container, position, `object`)
    }

    override fun notifyDataSetChanged() {
        if (isNeedToNotify) super.notifyDataSetChanged()
    }

    override fun finishUpdate(container: ViewGroup) {
        super.finishUpdate(container)
        listener?.let {
            for (p in this.fragmentInfos) {
                val attached = fragmentStateMap[p.fragment]
                if (attached != null) {
                    if (attached) {
                        it.onFragmentAttach(p.fragment)
                    } else {
                        it.onFragmentDetach(p.fragment)
                    }
                }
            }
        }
    }
    @Throws(Throwable::class)
    protected fun finalize() {
        fragmentInfos.clear()
        fragmentStateMap.clear()
    }

    private fun rangeCheck(index: Int) {
        require(index in 0 until this.fragmentInfos.size) {"Incorrect fragment index: $index"}
    }

    interface FragmentStateListener {

        fun onFragmentAttach(f: Fragment?)

        fun onFragmentDetach(f: Fragment?)
    }

    data class FragmentInfo(
            val fragment: Fragment,
            val title: String?,
            // при использовании PagerAdapter никогда не будет тега -> пользуем этот
            val tag: String? = null
    ) {

        override fun toString(): String {
            return "FragmentInfo(fragment=$fragment, title=$title, tag='$tag')"
        }
    }
}
