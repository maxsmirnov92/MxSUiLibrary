package net.maxsmr.jugglerhelper.fragments.toolbar;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.maxsmr.jugglerhelper.R;
import net.maxsmr.jugglerhelper.navigation.NavigationMode;

public class CustomLayoutToolbarFragment extends BaseCustomJugglerToolbarFragment {

    public static final String ARG_NAVIGATION_MODE = CustomLayoutToolbarFragment.class.getSimpleName() + ".ARG_NAVIGATION_MODE";
    public static final String ARG_TOOLBAR_LAYOUT_ID = CustomLayoutToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_LAYOUT_ID";
    public static final String ARG_TOOLBAR_ID = CustomLayoutToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_ID";
    public static final String ARG_TOOLBAR_TITLE_ID = CustomLayoutToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_TITLE_ID";
    public static final String ARG_TOOLBAR_LOGO_ID = CustomLayoutToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_LOGO_ID";
    public static final String ARG_TOOLBAR_LOGO_DRAWABLE_RES_ID = CustomLayoutToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_LOGO_DRAWABLE_RES_ID";

    @NotNull
    public static CustomLayoutToolbarFragment newInstance(@NotNull NavigationMode navigationMode) {
        return newInstance(navigationMode, 0);
    }

    @NotNull
    public static CustomLayoutToolbarFragment newInstance(@NotNull NavigationMode navigationMode, @LayoutRes int toolbarLayoutId) {
        return newInstance(navigationMode, toolbarLayoutId, 0, 0, 0, 0);
    }

    @NotNull
    public static CustomLayoutToolbarFragment newInstance(
            @NotNull NavigationMode navigationMode,
            @LayoutRes int toolbarLayoutId,
            @IdRes int toolbarId, @IdRes int toolbarTitleId, @IdRes int toolbarLogoId,
            @DrawableRes int toolbarLogoDrawableResId
    ) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NAVIGATION_MODE, navigationMode);
        args.putInt(ARG_TOOLBAR_LAYOUT_ID, toolbarLayoutId);
        args.putInt(ARG_TOOLBAR_ID, toolbarId);
        args.putInt(ARG_TOOLBAR_TITLE_ID, toolbarTitleId);
        args.putInt(ARG_TOOLBAR_LOGO_ID, toolbarLogoId);
        args.putInt(ARG_TOOLBAR_LOGO_DRAWABLE_RES_ID, toolbarLogoDrawableResId);
        CustomLayoutToolbarFragment fragment = new CustomLayoutToolbarFragment();
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
        final int id = args != null ? args.getInt(ARG_TOOLBAR_LAYOUT_ID) : 0;
        return id != 0 ? id : R.layout.fragment_standart_toolbar;
    }

    @IdRes
    @Override
    protected int getToolbarId() {
        final Bundle args = getArguments();
        final int id = args != null ? args.getInt(ARG_TOOLBAR_ID) : 0;
        return id != 0 ? id : R.id.toolbar;
    }

    @IdRes
    @Override
    protected int getToolbarTitleId() {
        final Bundle args = getArguments();
        return args != null ? args.getInt(ARG_TOOLBAR_TITLE_ID) : 0;
    }

    @IdRes
    @Override
    protected int getToolbarLogoId() {
        final Bundle args = getArguments();
        return args != null ? args.getInt(ARG_TOOLBAR_LOGO_ID) : 0;
    }

    @Nullable
    @Override
    protected Drawable getLogo() {
        final Bundle args = getArguments();
        final int drawableResId = args != null ? args.getInt(ARG_TOOLBAR_LOGO_DRAWABLE_RES_ID) : 0;
        return drawableResId != 0 ? ContextCompat.getDrawable(getContext(), drawableResId) : null;
    }
}
