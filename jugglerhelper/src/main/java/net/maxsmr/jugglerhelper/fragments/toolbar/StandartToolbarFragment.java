package net.maxsmr.jugglerhelper.fragments.toolbar;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import org.jetbrains.annotations.NotNull;

import net.maxsmr.jugglerhelper.R;
import net.maxsmr.jugglerhelper.navigation.NavigationMode;

public class StandartToolbarFragment extends BaseCustomJugglerToolbarFragment {

    public static final String ARG_NAVIGATION_MODE = StandartToolbarFragment.class.getSimpleName() + ".ARG_NAVIGATION_MODE";
    public static final String ARG_TOOLBAR_LAYOUT_ID = StandartToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_LAYOUT_ID";
    public static final String ARG_TOOLBAR_CONTAINER_ID = StandartToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_CONTAINER_ID";
    public static final String ARG_TOOLBAR_ID = StandartToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_ID";
    public static final String ARG_TOOLBAR_TITLE_ID = StandartToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_TITLE_ID";
    public static final String ARG_TOOLBAR_LOGO_ID = StandartToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_LOGO_ID";

    public static StandartToolbarFragment newInstance(@NotNull NavigationMode navigationMode) {
        return newInstance(navigationMode, 0, 0 , 0, 0, 0);
    }

    public static StandartToolbarFragment newInstance(@NotNull NavigationMode navigationMode,
                                                      @LayoutRes int toolbarLayoutId,
                                                      @IdRes int toolbarContainerId, @IdRes int toolbarId, @IdRes int toolbarTitleId, @IdRes int toolbarLogoId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NAVIGATION_MODE, navigationMode);
        args.putInt(ARG_TOOLBAR_LAYOUT_ID, toolbarLayoutId);
        args.putInt(ARG_TOOLBAR_CONTAINER_ID, toolbarContainerId);
        args.putInt(ARG_TOOLBAR_ID, toolbarId);
        args.putInt(ARG_TOOLBAR_TITLE_ID, toolbarTitleId);
        args.putInt(ARG_TOOLBAR_LOGO_ID, toolbarLogoId);
        StandartToolbarFragment fragment = new StandartToolbarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NotNull
    @Override
    protected NavigationMode getNavigationMode() {
        final Bundle args = getArguments();
        NavigationMode mode = null;
        if (args != null) {
            mode = (NavigationMode) getArguments().getSerializable(ARG_NAVIGATION_MODE);
        }
        if (mode == null) {
            mode = NavigationMode.NONE;
        }
        return mode;
    }

    @LayoutRes
    @Override
    protected int getLayoutId() {
        final Bundle args = getArguments();
        int id = args != null? args.getInt(ARG_TOOLBAR_LAYOUT_ID) : 0;
        return id != 0? id : R.layout.fragment_standart_toolbar;
    }

    @IdRes
    @Override
    protected int getToolbarContainerId() {
        final Bundle args = getArguments();
        return args != null? args.getInt(ARG_TOOLBAR_CONTAINER_ID) : 0;
    }

    @IdRes
    @Override
    protected int getToolbarId() {
        final Bundle args = getArguments();
        int id = args != null? args.getInt(ARG_TOOLBAR_ID) : 0;
        return id != 0? id : R.id.toolbar;
    }

    @IdRes
    @Override
    protected int getToolbarTitleId() {
        final Bundle args = getArguments();
        return args != null? args.getInt(ARG_TOOLBAR_TITLE_ID) : 0;
    }

    @IdRes
    @Override
    protected int getToolbarLogoId() {
        final Bundle args = getArguments();
        return args != null? args.getInt(ARG_TOOLBAR_LOGO_ID) : 0;
    }

}
