package net.maxsmr.jugglerhelper.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.maxsmr.commonutils.android.gui.GuiUtils;

import me.ilich.juggler.gui.JugglerNavigationFragment;

public abstract class BaseJugglerNavigationFragment extends JugglerNavigationFragment implements DrawerLayout.DrawerListener {

    @Nullable
    private Bundle savedInstanceState;

    @Nullable
    public Bundle getSavedInstanceState() {
        return savedInstanceState;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setHasOptionsMenu(false);
    }

    @NotNull
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutId(), container, false);
        onBindViews(rootView);
        return rootView;
    }

    protected void onBindViews(@NotNull View rootView) {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDrawerLayout().addDrawerListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getDrawerLayout().removeDrawerListener(this);
    }

    @Override
    public boolean onBackPressed() {
        return changeDrawerState(false) || super.onBackPressed();
    }

    @Override
    public boolean onUpPressed() {
        return changeDrawerState(true) || super.onUpPressed();
    }

    protected int getDrawerGravity() {
        return GravityCompat.START;
    }

    @NotNull
    @Override
    public DrawerLayout getDrawerLayout() {
        DrawerLayout drawerLayout = super.getDrawerLayout();
        if (drawerLayout == null) {
            throw new RuntimeException(DrawerLayout.class.getSimpleName() + " is not initialized");
        }
        return drawerLayout;
    }

    public boolean isDrawerOpen() {
        final DrawerLayout drawerLayout = getDrawerLayout();
        return drawerLayout.isDrawerOpen(getDrawerGravity());
    }

    public boolean changeDrawerState(boolean open) {
        final int drawerGravity = getDrawerGravity();
        final DrawerLayout drawerLayout = getDrawerLayout();
        if (!open && drawerLayout.isDrawerOpen(drawerGravity)) {
            drawerLayout.closeDrawer(drawerGravity);
            return true;
        } else if (open && !drawerLayout.isDrawerOpen(drawerGravity)) {
            drawerLayout.openDrawer(drawerGravity);
            return true;
        }

        return false;
    }

    public void revertDrawerState() {
        final int drawerGravity = getDrawerGravity();
        final DrawerLayout drawerLayout = getDrawerLayout();
        if (drawerLayout.isDrawerOpen(drawerGravity)) {
            drawerLayout.closeDrawer(drawerGravity);
        } else {
            drawerLayout.openDrawer(drawerGravity);
        }
    }


    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        GuiUtils.hideKeyboard(getActivity());
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        GuiUtils.hideKeyboard(getActivity());
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
