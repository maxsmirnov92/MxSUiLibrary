package net.maxsmr.jugglerhelper.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import net.maxsmr.commonutils.android.gui.GuiUtils;
import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.jugglerhelper.R;
import net.maxsmr.jugglerhelper.fragments.toolbar.BaseJugglerToolbarFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.gui.JugglerFragment;
import me.ilich.juggler.states.State;
import me.ilich.nestableviewpager.NestablePagerItem;

public abstract class BaseJugglerFragment extends JugglerFragment implements NestablePagerItem {

    @Nullable
    private Bundle savedInstanceState;

    private Menu menu;

    private boolean isCommitAllowed = false;

    public boolean isCommitAllowed() {
        return isAdded() && isCommitAllowed;
    }

    @Nullable
    public Fragment findChildFragmentByTag(String tag) {
        return findFragmentByTag(getChildFragmentManager(), tag);
    }

    @Nullable
    public Fragment findChildFragmentById(int id) {
        return findFragmentById(getChildFragmentManager(), id);
    }

    @Nullable
    public <F extends Fragment> F findChildFragmentByClass(Class<F> clazz) {
        return findFragmentByClass(getChildFragmentManager(), clazz);
    }

    @Nullable
    public Fragment findRootFragmentByTag(String tag) {
        return findFragmentByTag(getActivity() != null ? getActivity().getSupportFragmentManager() : null, tag);
    }

    @Nullable
    public Fragment findRootFragmentById(int id) {
        return findFragmentById(getActivity() != null ? getActivity().getSupportFragmentManager() : null, id);
    }

    @Nullable
    public <F extends Fragment> F findRootFragmentByClass(Class<F> clazz) {
        return findFragmentByClass(getFragmentManager(), clazz);
    }


    @SuppressWarnings("unchecked")
    @NotNull
    protected <T extends JugglerActivity> T getBaseActivity() {
        FragmentActivity activity = getActivity();

        if (activity == null) {
            throw new NullPointerException("not attached to activity");
        }
        if (!(activity instanceof JugglerActivity)) {
            throw new RuntimeException("activity isn't instance of a BaseActivity");
        }
        return (T) activity;
    }

    @ColorInt
    protected int getStatusBarColor() {
        return ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    }

    @ColorInt
    protected int getNavigationBarColor() {
        return ContextCompat.getColor(getContext(), R.color.navigationBarColor);
    }

    @Nullable
    protected String getBaseFontAlias() {
        return null;
    }


    @Nullable
    public Bundle getSavedInstanceState() {
        return savedInstanceState;
    }

    @LayoutRes
    protected abstract int getContentLayoutId();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setHasOptionsMenu(false);
    }

    @NotNull
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getContentLayoutId(), container, false);
        onBindViews(rootView);
        return rootView;
    }

    protected abstract void onBindViews(@NotNull View rootView);

    protected Menu getMenu() {
        return menu;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isCommitAllowed = true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GuiUtils.setStatusBarColor(getActivity().getWindow(), getStatusBarColor());
        GuiUtils.setNavigationBarColor(getActivity().getWindow(), getNavigationBarColor());
    }

    @Override
    public void onResume() {
        super.onResume();
        isCommitAllowed = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isCommitAllowed = false;
    }

    @CallSuper
    public void onTouchEvent(MotionEvent event) {
        List<Fragment> childFragments = getChildFragmentManager().getFragments();

        for (Fragment f : childFragments) {
            if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                ((BaseJugglerFragment) f).onTouchEvent(event);
            }
        }
    }

    @CallSuper
    public void onKeyDown(int keyCode, KeyEvent e) {
        List<Fragment> childFragments = getChildFragmentManager().getFragments();
        for (Fragment f : childFragments) {
            if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                ((BaseJugglerFragment) f).onKeyDown(keyCode, e);
            }
        }
    }

    public boolean onBackPressed() {
        List<Fragment> childFragments = getChildFragmentManager().getFragments();
        for (Fragment f : childFragments) {
            if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                if (((BaseJugglerFragment) f).onBackPressed()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @CallSuper
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<Fragment> childFragments = getChildFragmentManager().getFragments();
        for (Fragment f : childFragments) {
            if (f != null && !f.isDetached())
                f.onActivityResult(requestCode, resultCode, data);
        }
    }


    @CallSuper
    public void onStateActivated(JugglerActivity activity, State<?> state) {
        FragmentManager fm = getChildFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof BaseJugglerFragment) {
                ((BaseJugglerFragment) fragment).onStateActivated(activity, state);
            } else if (fragment instanceof BaseJugglerToolbarFragment) {
                ((BaseJugglerToolbarFragment) fragment).onStateActivated(activity, state);
            }
        }
    }

    @Nullable
    @Override
    public ViewPager getNestedViewPager() {
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isAdded()) {
            List<Fragment> childFragments = getChildFragmentManager().getFragments();
            for (Fragment f : childFragments) {
                if (f != null && !f.isDetached()) {
                    f.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }
    }

    @Nullable
    public static Fragment findFragmentById(FragmentManager fm, int id) {
        if (fm != null) {
            List<Fragment> fragments = fm.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment != null && !fragment.isDetached() && fragment.getId() == id) {
                    return fragment;
                }
            }
        }
        return null;
    }

    @Nullable
    public static Fragment findFragmentByTag(FragmentManager fm, String tag) {
        if (fm != null) {
            List<Fragment> fragments = fm.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment != null && !fragment.isDetached() && CompareUtils.stringsEqual(fragment.getTag(), tag, false)) {
                    return fragment;
                }
            }
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <F extends Fragment> F findFragmentByClass(FragmentManager fm, Class<F> fragmentClass) {
        if (fm != null) {
            List<Fragment> fragments = fm.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment != null && !fragment.isDetached() && fragmentClass.isAssignableFrom(fragment.getClass())) {
                    return (F) fragment;
                }
            }
        }
        return null;
    }
}

