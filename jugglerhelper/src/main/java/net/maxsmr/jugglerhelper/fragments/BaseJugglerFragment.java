package net.maxsmr.jugglerhelper.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import net.maxsmr.jugglerhelper.R;
import net.maxsmr.jugglerhelper.activities.BaseJugglerActivity;
import net.maxsmr.jugglerhelper.fragments.toolbar.BaseJugglerToolbarFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.gui.JugglerFragment;
import me.ilich.juggler.states.State;

public abstract class BaseJugglerFragment extends JugglerFragment {

    @NotNull
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    private Bundle savedInstanceState;

    private Menu menu;

    private boolean isCommitAllowed = false;

    public boolean isCommitAllowed() {
        return isAdded() && isCommitAllowed;
    }

    @Override
    protected BaseJugglerActivity getJugglerActivity() {
        return (BaseJugglerActivity) super.getJugglerActivity();
    }

    @Nullable
    public Fragment findChildFragmentById(int id) {
        Pair<Integer, Fragment> fragment = FragmentFinder.findFragmentById(getChildFragmentManager(), id);
        if (fragment != null) {
            return fragment.second;
        }
        return null;
    }

    @Nullable
    public Fragment findChildFragmentByTag(String tag) {
        Pair<Integer, Fragment> fragment = FragmentFinder.findFragmentByTag(getChildFragmentManager(), tag);
        if (fragment != null) {
            return fragment.second;
        }
        return null;
    }


    @Nullable
    public <F extends Fragment> F findChildFragmentByClass(Class<F> clazz) {
        Pair<Integer, F> fragment = FragmentFinder.findFragmentByClass(getChildFragmentManager(), clazz);
        if (fragment != null) {
            return fragment.second;
        }
        return null;
    }

    @Nullable
    public Fragment findRootFragmentById(int id) {
        return getJugglerActivity().findFragmentById(id);
    }

    @Nullable
    public Fragment findRootFragmentByTag(String tag) {
        return getJugglerActivity().findFragmentByTag(tag);
    }

    @Nullable
    public <F extends Fragment> F findRootFragmentByClass(Class<F> clazz) {
        return getJugglerActivity().findFragmentByClass(clazz);
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

    protected void onBindViews(@NotNull View rootView) {

    }

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
    public void onResume() {
        super.onResume();
        isCommitAllowed = true;
        setupWindow();
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

    @Override
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

    @SuppressWarnings("ConstantConditions")
    @Nullable
    protected Drawable getWindowBackground() {
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    @ColorInt
    @Nullable
    protected Integer getStatusBarColor() {
        return ContextCompat.getColor(getContext(), R.color.colorStatusBar);
    }

    @SuppressWarnings("ConstantConditions")
    @ColorInt
    @Nullable
    protected Integer getNavigationBarColor() {
        return ContextCompat.getColor(getContext(), R.color.colorNavigationBar);
    }

    @SuppressWarnings("ConstantConditions")
    protected void setupWindow() {
        final Window window = getActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Integer statusBarColor = getStatusBarColor();
            if (statusBarColor != null) {
                window.setStatusBarColor(statusBarColor);
            }
            Integer navigationBarColor = getNavigationBarColor();
            if (navigationBarColor != null) {
                window.setNavigationBarColor(navigationBarColor);
            }
        }
        Drawable windowBackground = getWindowBackground();
        if (windowBackground != null) {
            window.setBackgroundDrawable(windowBackground);
        }
    }
}

