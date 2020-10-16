package net.maxsmr.jugglerhelper.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun <F : Fragment> findFragment(fragmentManager: FragmentManager, params: FragmentSearchParams<F>): Pair<Int, Fragment>? =
        findFragment(fragmentManager.fragments, params)

fun <F : Fragment> findFragment(fragments: Collection<Fragment?>, params: FragmentSearchParams<F>): Pair<Int, Fragment>? =
        filterFragments(fragments, params).toList().getOrNull(0)

fun <F : Fragment> filterFragments(fragmentManager: FragmentManager, params: FragmentSearchParams<F>): Map<Int, Fragment> =
        filterFragments(fragmentManager.fragments, params)

@Suppress("UNCHECKED_CAST")
fun <F : Fragment> filterFragments(fragments: Collection<Fragment?>, params: FragmentSearchParams<F>): Map<Int, Fragment> {
    val result = mutableMapOf<Int, Fragment>()
    val filteredFragments: List<Pair<Int, Fragment>> = fragments
            .mapIndexed { index, fragment -> Pair(index, fragment) }
            .filter { fragmentPair ->
                val fragment = fragmentPair.second
                with(params) {
                    var isMatch = false
                    if (fragment != null && !fragment.isDetached) {
                        isMatch = true
                        if (id != null && id != fragment.id) {
                            isMatch = false
                        }
                        if (tag != null && tag != fragment.tag) {
                            isMatch = false
                        }
                        if (fragmentClass != null && !fragmentClass.isAssignableFrom(fragment.javaClass)) {
                            isMatch = false
                        }
                        if (this.fragment != null && this.fragment != fragment) {
                            isMatch = false
                        }
                        if (additionalPredicate != null && !additionalPredicate.invoke(fragment)) {
                            isMatch = false
                        }
                    }
                    isMatch
                }
            }.mapNotNull {
                if (it.second == null) {
                    return@mapNotNull null
                }
                it
            } as List<Pair<Int, Fragment>>
    filteredFragments.forEach {
        result[it.first] = it.second
    }
    return result
}

data class FragmentSearchParams<F : Fragment>(
        val id: Int? = null,
        val tag: String? = null,
        val fragmentClass: Class<F>? = null,
        val fragment: F? = null,
        val additionalPredicate: ((Fragment) -> Boolean)? = null
)