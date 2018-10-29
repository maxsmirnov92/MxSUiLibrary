package net.maxsmr.jugglerhelper.fragments.toolbar;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

    public static final String ARG_NAVIGATION_MODE = BaseJugglerToolbarFragment.class.getSimpleName() + ".ARG_NAVIGATION_MODE";

    @LayoutRes
    protected abstract int getLayoutId();

    @Nullable
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

        Toolbar toolbar = GuiUtils.findViewById(rootView, getToolbarId());

        if (toolbar == null) {
            throw new IllegalStateException("toolbar was not found");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getJugglerActivity().onSupportNavigateUp();
            }
        });

        ActionBar actionBar = getJugglerActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.empty);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }
    }

    protected void initTitle() {
        String title = null;
        State<State.Params> state = (State<State.Params>) getState();
        if (state != null) {
            title = state.getTitle(getContext(), state.getParams());
        } else {
            CharSequence activityTitle = getActivity().getTitle();
            title = activityTitle != null ? activityTitle.toString() : null;
        }
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
    }

    protected void initNavigationMode() {
        setMode(getNavigationMode(), getUpIcon());
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    protected NavigationMode getNavigationMode() {
        return (NavigationMode) getArguments().getSerializable(ARG_NAVIGATION_MODE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();
        initTitle();
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
    protected final CharSequence getTitle() {
        ActionBar actionBar = getJugglerActivity().getSupportActionBar();
        return actionBar != null ? actionBar.getTitle() : null;
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

    protected void setTitle(@StringRes int textRestId) {
        setTitle(getString(textRestId));
    }

    public void setLogo(Drawable icon) {
        Toolbar toolbar = getToolbar();
        if (toolbar == null) {
            throw new RuntimeException("toolbar was not initialized");
        }
        toolbar.setLogo(icon);
    }

    @CallSuper
    public void onStateActivated(JugglerActivity activity, State<?> state) {
        initTitle();
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

}
