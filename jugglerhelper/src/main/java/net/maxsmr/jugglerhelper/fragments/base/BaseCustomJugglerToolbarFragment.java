package net.maxsmr.jugglerhelper.fragments.base;

import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.maxsmr.jugglerhelper.R;

public abstract class BaseCustomJugglerToolbarFragment extends BaseJugglerToolbarFragment {

    public static final String ARG_TOOLBAR_LAYOUT_ID = BaseCustomJugglerToolbarFragment.class.getSimpleName() + ".ARG_TOOLBAR_LAYOUT_ID";

    protected ViewGroup toolbarContainer;

    @SuppressWarnings("ResourceType")
    @Override
    @LayoutRes
    protected int getLayoutId() {
        return getArguments() != null? getArguments().getInt(ARG_TOOLBAR_LAYOUT_ID) : 0;
    }

    @IdRes
    protected abstract int getToolbarContainerId();

    @IdRes
    protected abstract int getToolbarTitleId();

    @IdRes
    protected abstract int getToolbarLogoId();

    @Override
    @CallSuper
    protected void onBindViews(@NonNull View rootView) {
        toolbarContainer = (ViewGroup) rootView.findViewById(getToolbarContainerId());
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        if (getJugglerActivity().getSupportActionBar() != null) {
            getJugglerActivity().getSupportActionBar().setTitle(null);
        }
        Toolbar toolbar = getToolbar();
        if (toolbar == null) {
            throw new RuntimeException("toolbar was not initialized");
        }
        toolbar.setTitle(null);
        TextView titleView = (TextView) toolbar.findViewById(getToolbarTitleId());
        if (titleView != null) {
            titleView.setText(title);
            titleView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void setLogo(@Nullable Drawable logo) {
        Toolbar toolbar = getToolbar();
        if (toolbar == null) {
            throw new RuntimeException("toolbar was not initialized");
        }
        toolbar.setLogo(null);
        ImageView logoView = (ImageView) toolbar.findViewById(getToolbarLogoId());
        if (logoView != null) {
            logoView.setImageDrawable(logo);
            logoView.setVisibility(logo == null ? View.GONE : View.VISIBLE);
        }
    }
}
