package net.maxsmr.jugglerhelper.fragments.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import net.maxsmr.jugglerhelper.fragments.base.toolbar.BaseJugglerToolbarFragment;

import java.util.List;

import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.gui.JugglerFragment;
import me.ilich.juggler.states.State;
import me.ilich.nestableviewpager.NestablePagerItem;

public abstract class BaseJugglerFragment extends JugglerFragment implements NestablePagerItem {

    @Nullable
    private Bundle savedInstanceState;

    private Menu menu;

    private boolean isCommitAllowed;

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
    public Fragment findRootFragmentByTag(String tag) {
        return findFragmentByTag(getActivity() != null ? getActivity().getSupportFragmentManager() : null, tag);
    }

    @Nullable
    public Fragment findRootFragmentById(int id) {
        return findFragmentById(getActivity() != null ? getActivity().getSupportFragmentManager() : null, id);
    }


    @SuppressWarnings("unchecked")
    @NonNull
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

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getContentLayoutId(), container, false);
        onBindViews(rootView);
        return rootView;
    }

    protected abstract void onBindViews(@NonNull View rootView);

    public Menu getMenu() {
        return this.menu;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GuiUtils.setStatusBarColor(getStatusBarColor(), getActivity().getWindow());
        GuiUtils.setNavigationBarColor(getNavigationBarColor(), getActivity().getWindow());
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
        if (childFragments != null) {
            for (Fragment f : childFragments) {
                if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                    ((BaseJugglerFragment) f).onTouchEvent(event);
                }
            }
        }
    }

    @CallSuper
    public void onKeyDown(int keyCode, KeyEvent e) {
        List<Fragment> childFragments = getChildFragmentManager().getFragments();
        if (childFragments != null) {
            for (Fragment f : childFragments) {
                if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                    ((BaseJugglerFragment) f).onKeyDown(keyCode, e);
                }
            }
        }
    }

    public boolean onBackPressed() {
        List<Fragment> childFragments = getChildFragmentManager().getFragments();
        if (childFragments != null) {
            for (Fragment f : childFragments) {
                if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                    if (((BaseJugglerFragment) f).onBackPressed()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    @CallSuper
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<Fragment> childFragments = getChildFragmentManager().getFragments();
        if (childFragments != null) {
            for (Fragment f : childFragments) {
                if (f != null && !f.isDetached())
                    f.onActivityResult(requestCode, resultCode, data);
            }
        }
    }


    @CallSuper
    public void onStateActivated(JugglerActivity activity, State<?> state) {
        FragmentManager fm = getChildFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof BaseJugglerFragment) {
                    ((BaseJugglerFragment) fragment).onStateActivated(activity, state);
                } else if (fragment instanceof BaseJugglerToolbarFragment) {
                    ((BaseJugglerToolbarFragment) fragment).onStateActivated(activity, state);
                }
            }
        }
    }

    @Nullable
    @Override
    public ViewPager getNestedViewPager() {
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isAdded()) {
            for (Fragment f : getChildFragmentManager().getFragments()) {
                if (f != null && !f.isDetached()) {
                    f.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }
    }

    @Nullable
    private static Fragment findFragmentById(FragmentManager fm, int id) {
        if (fm != null) {
            List<Fragment> fragments = fm.getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment != null && !fragment.isDetached() && fragment.getId() == id) {
                        return fragment;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static Fragment findFragmentByTag(FragmentManager fm, String tag) {
        if (fm != null) {
            List<Fragment> fragments = fm.getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment != null && !fragment.isDetached() && CompareUtils.stringsEqual(fragment.getTag(), tag, false)) {
                        return fragment;
                    }
                }
            }
        }
        return null;
    }
}

