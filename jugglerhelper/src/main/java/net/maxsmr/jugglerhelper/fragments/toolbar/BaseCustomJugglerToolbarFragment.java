package net.maxsmr.jugglerhelper.fragments.toolbar;

import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;

import org.jetbrains.annotations.Nullable;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.maxsmr.jugglerhelper.R;

public abstract class BaseCustomJugglerToolbarFragment extends BaseJugglerToolbarFragment {

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
            super.setTitle(getString(R.string.no_data));
            titleView.setText(title);
            titleView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        } else {
            super.setTitle(title);
        }
    }

    @Override
    public void setLogo(@Nullable Drawable logo) {
        View rootView = getView();
        if (rootView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " is not attached to activity");
        }
        ImageView logoView = rootView.findViewById(getToolbarLogoId());
        if (logoView != null) {
            super.setLogo(null);
            logoView.setImageDrawable(logo);
            logoView.setVisibility(logo == null ? View.GONE : View.VISIBLE);
        } else {
            super.setLogo(logo);
        }
    }
}
