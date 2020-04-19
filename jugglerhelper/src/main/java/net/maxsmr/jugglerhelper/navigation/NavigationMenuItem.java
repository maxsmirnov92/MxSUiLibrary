package net.maxsmr.jugglerhelper.navigation;

import android.content.res.ColorStateList;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

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

    @NotNull
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
