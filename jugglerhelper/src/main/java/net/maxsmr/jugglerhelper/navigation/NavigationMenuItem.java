package ru.gokidgo.gui.navigation;

import android.content.res.ColorStateList;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.Collection;

import ru.gokidgo.R;

import static android.R.attr.id;

public class NavigationMenuItem implements Serializable {

    public final IMenuItemId id;

    @StringRes
    public final int titleResId;

    @DrawableRes
    public final int iconResId;

    @Nullable
    public final ColorStateList textColorStateList;

    public NavigationMenuItem(IMenuItemId id, @StringRes int titleResId, @DrawableRes int iconResId) {
        this.id = id;
        this.titleResId = titleResId;
        this.iconResId = iconResId;
        this.textColorStateList = null;
    }

    public NavigationMenuItem(IMenuItemId id, @StringRes int titleResId, @DrawableRes int iconResId, @Nullable ColorStateList textColorStateList) {
        this.id = id;
        this.titleResId = titleResId;
        this.iconResId = iconResId;
        this.textColorStateList = textColorStateList;
    }

    @Override
    public String toString() {
        return "NavigationMenuItem{" +
                "id=" + id +
                ", titleResId=" + titleResId +
                ", iconResId=" + iconResId +
                ", textColorStateList=" + textColorStateList +
                '}';
    }

    public interface IMenuItemId {

        int getId();
    }
}
