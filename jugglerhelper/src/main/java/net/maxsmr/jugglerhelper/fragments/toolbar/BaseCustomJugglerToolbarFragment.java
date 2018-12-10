package net.maxsmr.jugglerhelper.fragments.toolbar;

import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseCustomJugglerToolbarFragment extends BaseJugglerToolbarFragment {

    @IdRes
    protected abstract int getToolbarContainerId();

    @IdRes
    protected abstract int getToolbarTitleId();

    @IdRes
    protected abstract int getToolbarLogoId();

    @Override
    public void setTitle(@Nullable CharSequence title) {
        View rootView = getView();
        if (rootView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " is not attached to activity");
        }
        TextView titleView = rootView.findViewById(getToolbarTitleId());
        if (titleView != null) {
            super.setTitle("");
            titleView.setText(title);
            titleView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        } else {
            super.setTitle(title);
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
