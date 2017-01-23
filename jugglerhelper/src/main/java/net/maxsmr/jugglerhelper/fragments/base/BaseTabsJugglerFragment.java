package net.maxsmr.jugglerhelper.fragments.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;

import net.maxsmr.commonutils.android.gui.GuiUtils;
import net.maxsmr.commonutils.android.gui.adapters.CustomFragmentStatePagerAdapter;
import net.maxsmr.commonutils.android.gui.fonts.FontsHolder;
import net.maxsmr.jugglerhelper.R;

import java.lang.reflect.Field;

public abstract class BaseTabsJugglerFragment<PagerAdapter extends CustomFragmentStatePagerAdapter> extends BaseJugglerFragment implements ViewPager.OnPageChangeListener {

    public static final int NO_IDX = -1;

    public static final String ARG_TAB_FRAGMENT_INDEX = BaseTabsJugglerFragment.class.getSimpleName() + ".ARG_TAB_FRAGMENT_INDEX";

    @NonNull
    protected abstract PagerAdapter initStatePagerAdapter();

    @SuppressWarnings("unchecked")
    protected final PagerAdapter getStatePagerAdapter() {
        return (PagerAdapter) viewPager.getAdapter();
    }

    @Nullable
    protected TabLayout tabLayout;

    protected ViewPager viewPager;

    @IdRes
    protected int getTabLayoutId() {
        return R.id.tab_layout;
    }

    @IdRes
    protected int getPagerId() {
        return R.id.pager;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.tabs;
    }

    @Override
    @CallSuper
    protected void onBindViews(@NonNull View rootView) {
        tabLayout = GuiUtils.findViewById(rootView, getTabLayoutId());
        viewPager = GuiUtils.findViewById(rootView, getPagerId());
    }

    protected int getInitialTabFragmentIndex() {
        return getArguments() != null ? getArguments().getInt(ARG_TAB_FRAGMENT_INDEX, -1) : -1;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (viewPager == null) {
            throw new RuntimeException("viewPager not found");
        }

        if (tabLayout != null) {
            int tabGravity = getTabGravity();
            if (tabGravity != TabLayout.GRAVITY_CENTER && tabGravity != TabLayout.GRAVITY_FILL) {
                throw new IllegalArgumentException("incorrect tabGravity: " + tabGravity);
            }
            tabLayout.setTabGravity(tabGravity);

            int tabMode = getTabMode();
            if (tabMode != TabLayout.MODE_FIXED && tabMode != TabLayout.MODE_SCROLLABLE) {
                throw new IllegalArgumentException("incorrect tabMode: " + tabMode);
            }
            tabLayout.setTabMode(tabMode);
        }

        reload();

        viewPager.addOnPageChangeListener(this);

        int tabFragmentIndex = getInitialTabFragmentIndex();
        if (tabFragmentIndex >= 0 && tabFragmentIndex < getStatePagerAdapter().getCount()) {
            if (tabFragmentIndex != viewPager.getCurrentItem()) {
                viewPager.setCurrentItem(tabFragmentIndex, true);
            } else {
                invalidatePageSelected();
            }
        } else {
            invalidatePageSelected();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.removeOnPageChangeListener(this);
    }

    protected void invalidatePageSelected() {
        onPageSelected(viewPager.getCurrentItem());
    }

    @TabLayout.TabGravity
    protected int getTabGravity() {
        return TabLayout.GRAVITY_CENTER;
    }

    @TabLayout.Mode
    protected int getTabMode() {
        return TabLayout.MODE_FIXED;
    }

    protected void setTabsTypeface(String alias) {
        if (tabLayout != null && !TextUtils.isEmpty(alias)) {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    Field field = null;
                    try {
                        field = tab.getClass().getDeclaredField("mView");
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                    if (field != null) {
                        View tabView = null;
                        try {
                            field.setAccessible(true);
                            tabView = (View) field.get(tab);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        FontsHolder.getInstance().apply(tabView, alias, false);
                    }
                }
            }
        }
    }

    @CallSuper
    protected void reload() {

        if (getContext() == null) {
            throw new RuntimeException("not attached to activity");
        }

        String alias = getBaseFontAlias();

        if (!TextUtils.isEmpty(alias)) {
            setTabsTypeface(alias);
        }

        viewPager.setAdapter(initStatePagerAdapter());
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }

        notifyFragmentsChanged();
    }

    protected void updateTabIcons() {
        if (tabLayout != null) {
            PagerAdapter adapter = getStatePagerAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    Fragment f = adapter.getFragmentInstance(i);
                    Drawable tabIcon = getTabIconForFragment(f);
                    if (tabIcon != null) {
                        TabLayout.Tab tab = tabLayout.getTabAt(i);
                        if (tab != null) {
                            tab.setIcon(tabIcon);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    protected abstract Drawable getTabIconForFragment(Fragment f);

    protected void notifyFragmentsChanged() {

        if (getContext() == null) {
            throw new RuntimeException("not attached to activity");
        }

        PagerAdapter adapter = getStatePagerAdapter();

        if (adapter == null) {
            throw new RuntimeException("adapter is not initialized");
        }

        adapter.setNeedToNotify(true);
        adapter.notifyDataSetChanged();

        updateTabIcons();
    }

    public void selectTab(int at) {
        PagerAdapter adapter = getStatePagerAdapter();

        if (adapter == null) {
            throw new RuntimeException("adapter is not initialized");
        }

        if (at < 0 || at >= adapter.getCount()) {
            throw new IndexOutOfBoundsException("incorrect tab index: " + at);
        }

        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(at);
            if (tab != null && !tab.isSelected()) {
                tab.select();
            }
        } else {
            viewPager.setCurrentItem(at, true);
        }
    }

    public void selectTabByFragment(Fragment fragment) {
        PagerAdapter adapter = getStatePagerAdapter();

        if (adapter == null) {
            throw new RuntimeException("adapter is not initialized");
        }

        int index = adapter.fragmentIndexOf(fragment);
        if (index >= 0) {
            selectTab(index);
        }
    }


    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
