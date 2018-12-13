package net.maxsmr.jugglerhelper.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import net.maxsmr.commonutils.android.gui.GuiUtils;
import net.maxsmr.commonutils.android.gui.adapters.CustomFragmentStatePagerAdapter;
import net.maxsmr.commonutils.android.gui.fonts.FontsHolder;
import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.commonutils.data.Predicate;
import net.maxsmr.jugglerhelper.R;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseTabsJugglerFragment<PagerAdapter
        extends CustomFragmentStatePagerAdapter> extends BaseJugglerFragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    public static final String ARG_TAB_FRAGMENT_INDEX = BaseTabsJugglerFragment.class.getSimpleName() + ".ARG_TAB_FRAGMENT_INDEX";

    @NotNull
    protected final Map<View, String> customViewTabsMap = new LinkedHashMap<>();

    @Nullable
    protected TabLayout tabLayout;

    protected ViewPager viewPager;

    @Override
    @CallSuper
    protected void onBindViews(@NotNull View rootView) {
        tabLayout = GuiUtils.findViewById(rootView, getTabLayoutId());
        viewPager = GuiUtils.findViewById(rootView, getPagerId());
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

        viewPager.addOnPageChangeListener(this);

        reload();

        selectAdapterPage(getInitialTabFragmentIndex(), true);

        long delay = getInitCustomTabViewsDelay();
        if (delay < 0) {
            delay = 0;
        }

        // due to juggler fragments init order (first 'content' fragment)
        mainHandler.postDelayed(
                () -> {
                    if (isAdded()) {
                        initCustomViewTabsMap(customViewTabsMap);
                        for (View v : customViewTabsMap.keySet()) {
                            v.setOnClickListener(this);
                        }
                        invalidateCustomViewTabsByPager();
                    }
                }, delay);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_TAB_FRAGMENT_INDEX, getCurrentSelectedPageIndex());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.removeOnPageChangeListener(this);
    }

    protected long getInitCustomTabViewsDelay() {
        return 200;
    }

    protected abstract void initCustomViewTabsMap(@NotNull Map<View, String> viewTabsMap);

    @NotNull
    protected abstract PagerAdapter initStatePagerAdapter();

    @SuppressWarnings("unchecked")
    @Nullable
    protected final PagerAdapter getStatePagerAdapter() {
        return (PagerAdapter) viewPager.getAdapter();
    }

    /** optional map containing views and tags for them */
    @NotNull
    public Map<View, String> getCustomViewTabsMap() {
        return new LinkedHashMap<>(customViewTabsMap);
    }

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

    @Nullable
    protected String getTagForPagerFragment(@NotNull Fragment fragment) {
        return fragment.getClass().getSimpleName();
    }

    protected int getCurrentPagesCount() {
        PagerAdapter adapter = getStatePagerAdapter();
        if (adapter != null) {
            return adapter.getCount();
        }
        return 0;
    }

    protected int getCurrentSelectedPageIndex() {
        return viewPager.getCurrentItem();
    }

    @TabLayout.TabGravity
    protected int getTabGravity() {
        return TabLayout.GRAVITY_CENTER;
    }

    @TabLayout.Mode
    protected int getTabMode() {
        return TabLayout.MODE_FIXED;
    }

    protected int getInitialTabFragmentIndex() {
        return getArguments() != null ? getArguments().getInt(ARG_TAB_FRAGMENT_INDEX) :
                (getSavedInstanceState() != null ? getSavedInstanceState().getInt(ARG_TAB_FRAGMENT_INDEX) : 0);
    }


    protected void selectAdapterPage(int index, boolean updateIfNotSelected) {

        boolean selected = false;
        if (index >= 0 && index < getCurrentPagesCount()) {
            if (index != getCurrentSelectedPageIndex()) {
                viewPager.setCurrentItem(index, true);
                selected = true;
            }
        }
        if (!selected && updateIfNotSelected) {
            invalidatePageSelected();
        }
    }

    protected void invalidatePageSelected() {
        final int position = getCurrentSelectedPageIndex();
        if (position >= 0 && position < getCurrentPagesCount()) {
            onPageSelected(position);
        }
    }

    protected void invalidateCustomViewTabsByPager() {
        PagerAdapter adapter = getStatePagerAdapter();
        if (adapter != null) {
            if (!customViewTabsMap.isEmpty()) {
                for (int index = 0; index < getCurrentPagesCount(); index++) {

                    final boolean isSelected = getCurrentSelectedPageIndex() == index;

                    final Fragment fragment = adapter.getFragmentInstance(index);
                    final String tag = getTagForPagerFragment(fragment);

                    final Map.Entry<View, String> viewEntry = findCustomViewTabByTag(tag);
                    if (viewEntry != null) {
                        invalidateCustomViewTab(viewEntry.getKey(), tag, isSelected);
                    }
                }
            }
        }
    }

    protected void invalidateCustomViewTab(@NotNull View tabView, @Nullable String tabViewTag, boolean isSelected) {
        tabView.setSelected(isSelected);
    }

    @Nullable
    protected Map.Entry<View, String> findCustomViewTabByTag(@Nullable String tag) {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        return Predicate.Methods.find(customViewTabsMap.entrySet(), element -> CompareUtils.stringsEqual(element.getValue(), tag, false));
    }

    @Nullable
    protected Map.Entry<View, String> findCustomViewTabByView(@Nullable View view) {
        return Predicate.Methods.find(customViewTabsMap.entrySet(), element -> element.getKey() == view);
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
    protected Drawable getTabIconForFragment(Fragment f) {
        return null;
    }

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
        invalidateCustomViewTabsByPager();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @CallSuper
    @Override
    public void onClick(View v) {

        PagerAdapter adapter = getStatePagerAdapter();
        if (adapter != null) {

            final Map.Entry<View, String> entry = findCustomViewTabByView(v);
            if (entry != null) {

                int selectedIndex = RecyclerView.NO_POSITION;

                for (int index = 0; index < getCurrentPagesCount(); index++) {

                    final Fragment fragment = adapter.getFragmentInstance(index);
                    final String tag = getTagForPagerFragment(fragment);

                    if (!TextUtils.isEmpty(tag) && CompareUtils.stringsEqual(entry.getValue(), tag, false)) {
                        selectedIndex = index;
                        break;
                    }
                }

                if (selectedIndex >= 0 && selectedIndex < getCurrentPagesCount()) {
                    viewPager.setCurrentItem(selectedIndex);
                }
            }
        }
    }

    @Nullable
    public static <F extends Fragment> Pair<Integer, F> findFragmentByClass(@NotNull Class<F> fragmentClass, CustomFragmentStatePagerAdapter pagerAdapter) {
        Pair<Integer, F> result = null;
        for (int index = 0; index < pagerAdapter.getCount(); index++) {
            Fragment fragment = pagerAdapter.getFragmentInstance(index);
            if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                //noinspection unchecked
                result = new Pair<>(index, (F) fragment);
                break;
            }
            index++;
        }
        return result;
    }
}
