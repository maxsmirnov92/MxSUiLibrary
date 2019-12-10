package net.maxsmr.jugglerhelper.fragments.toolbar;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.maxsmr.commonutils.android.gui.GuiUtils;
import net.maxsmr.jugglerhelper.R;
import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment;
import net.maxsmr.jugglerhelper.navigation.NavigationMode;

import java.util.List;

import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.gui.JugglerToolbarFragment;
import me.ilich.juggler.states.State;

public abstract class BaseJugglerToolbarFragment extends JugglerToolbarFragment {

    @LayoutRes
    protected abstract int getLayoutId();

    @NotNull
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutId(), container, false);
        onBindViews(rootView);
        return rootView;
    }

    protected void onBindViews(@NotNull View rootView) {
    }

    protected void initToolbar() {
        View rootView = getView();

        if (rootView == null) {
            throw new IllegalStateException("root view was not created");
        }

        Toolbar toolbar = rootView.findViewById(getToolbarId());

        if (toolbar == null) {
            throw new IllegalStateException("toolbar was not found");
        }

        toolbar.setNavigationOnClickListener(v -> getJugglerActivity().onSupportNavigateUp());

        ActionBar actionBar = getJugglerActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }
    }

    protected void initTitle() {
        setTitle(getTitle());
    }

    // setting logo by layout is not working properly
    protected void initLogo() {
        setLogo(getLogo());
    }

    protected void initNavigationMode() {
        setMode(getNavigationMode(), getUpIcon());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();
        initTitle();
        initLogo();
        initNavigationMode();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ActionBar actionBar = getJugglerActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void setMode(NavigationMode mode, @Nullable Drawable upIcon) {

        if (mode != null) {

            Toolbar toolbar = getToolbar();

            if (toolbar != null) {

                if (upIcon != null) {
                    toolbar.setNavigationIcon(upIcon);
                }

                switch (mode) {
                    case SANDWICH:
                        setNavigationIconVisible(true);
                        break;
                    case BACK:
                        setNavigationIconVisible(true);
                        break;
                    case NONE:
                        setNavigationIconVisible(false);
                        toolbar.setVisibility(View.VISIBLE);
                        break;
                    case INVISIBLE:
                        setNavigationIconVisible(false);
                        toolbar.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }

    @Nullable
    protected Drawable getUpIcon() {
        State state = getState();
        return state != null ? state.getUpNavigationIcon(getContext()) : null;
    }

    private void setNavigationIconVisible(boolean b) {
        ActionBar actionBar = getJugglerActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(b);
            actionBar.setHomeButtonEnabled(b);
        }
    }

    @Nullable
    public String getTitle() {
        String title = null;
        @SuppressWarnings("unchecked") State<State.Params> state = (State<State.Params>) getState();
        if (state != null) {
            title = state.getTitle(getContext(), state.getParams());
        } else {
            Activity activity = getActivity();
            if (activity != null) {
                CharSequence activityTitle = activity.getTitle();
                title = activityTitle != null ? activityTitle.toString() : null;
            }
        }
        return title;
    }

    public void setTitle(CharSequence text) {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            toolbar.setTitle(text);
        }
        ActionBar actionBar = getJugglerActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    public void setTitle(@StringRes int textRestId) {
        setTitle(getString(textRestId));
    }

    @Nullable
    protected abstract Drawable getLogo();

    public void setLogo(Drawable icon) {
        Toolbar toolbar = getToolbar();
        if (toolbar == null) {
            throw new RuntimeException("toolbar was not initialized");
        }
        toolbar.setLogo(icon);
    }

    @NotNull
    protected abstract NavigationMode getNavigationMode();

    @CallSuper
    public void onStateActivated(JugglerActivity activity, State<?> state) {
        initTitle();
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

}
