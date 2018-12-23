package net.maxsmr.jugglerhelper.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;

import net.maxsmr.commonutils.android.gui.adapters.CustomFragmentStatePagerAdapter;
import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.commonutils.data.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class FragmentFinder {

    private FragmentFinder() {
        throw new AssertionError("No instances");
    }

    @Nullable
    public static Pair<Integer, Fragment> findFragmentById(@Nullable FragmentManager fm, int id) {
        if (fm != null) {
            return findFragmentById(fm.getFragments(), id);
        }
        return null;
    }

    @Nullable
    public static Pair<Integer, Fragment> findFragmentById(@Nullable Collection<Fragment> fragments, int id) {
        return Predicate.Methods.findWithIndex(fragments, fragment -> fragment != null && !fragment.isDetached() && fragment.getId() == id);
    }

    @Nullable
    public static Pair<Integer, Fragment> findFragmentByTag(FragmentManager fm, String tag) {
        if (fm != null) {
            return findFragmentByTag(fm.getFragments(), tag);
        }
        return null;
    }

    @Nullable
    public static Pair<Integer, Fragment> findFragmentByTag(@Nullable Collection<Fragment> fragments, String tag) {
        return Predicate.Methods.findWithIndex(fragments, fragment ->   fragment != null && !fragment.isDetached() && CompareUtils.stringsEqual(fragment.getTag(), tag, false));

    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <F extends Fragment> Pair<Integer, F> findFragmentByClass(@Nullable FragmentManager fm, @Nullable Class<F> fragmentClass) {
        if (fm != null) {
            return findFragmentByClass(fm.getFragments(), fragmentClass);
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <F extends Fragment> Pair<Integer, F> findFragmentByClass(@Nullable Collection<Fragment> fragments, @Nullable Class<F> fragmentClass) {
        Pair<Integer, Fragment> result = null;
        if (fragmentClass != null) {
            result = Predicate.Methods.findWithIndex(fragments, fragment -> fragment != null && !fragment.isDetached() && fragmentClass.isAssignableFrom(fragment.getClass()));
        }
        if (result != null) {
            return new Pair<>(result.first, (F) result.second);
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <F extends Fragment> Pair<Integer, F> findFragmentByInstance(@Nullable FragmentManager fm, @Nullable F targetFragment) {
        if (fm != null) {
            return findFragmentByInstance(fm.getFragments(), targetFragment);
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <F extends Fragment> Pair<Integer, F> findFragmentByInstance(@Nullable Collection<Fragment> fragments, @Nullable F targetFragment) {
        Pair<Integer, Fragment> result = Predicate.Methods.findWithIndex(fragments, fragment -> fragment != null && !fragment.isDetached() &&
                CompareUtils.objectsEqual(fragment, targetFragment) &&
                (targetFragment == null || targetFragment.getClass().isAssignableFrom(fragment.getClass())));
        if (result != null) {
            return new Pair<>(result.first, (F) result.second);
        }
        return null;
    }

    // TODO update CustomFragmentStatePagerAdapter
    @Deprecated
    @Nullable
    public static <F extends Fragment> Pair<Integer, F> findFragmentByClass(@NotNull Class<F> fragmentClass, @Nullable CustomFragmentStatePagerAdapter pagerAdapter) {
        Pair<Integer, F> result = null;
        if (pagerAdapter != null) {
            for (int index = 0; index < pagerAdapter.getCount(); index++) {
                Fragment fragment = pagerAdapter.getFragmentInstance(index);
                if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                    //noinspection unchecked
                    result = new Pair<>(index, (F) fragment);
                    break;
                }
                index++;
            }
        }
        return result;
    }
}