package net.maxsmr.jugglerhelper.fragments.loading;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.viewpager.widget.ViewPager;

import net.maxsmr.android.recyclerview.adapters.BasePagerAdapter;
import net.maxsmr.commonutils.android.gui.views.ViewPagerIndicator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@Deprecated
public abstract class BaseViewPagerListLoadingFragment<I, Adapter extends BasePagerAdapter<I, ?>>
        extends BaseLoadingJugglerFragment<List<I>> implements ViewPager.OnPageChangeListener {

    protected ViewPager viewPager;

    @Nullable
    protected ViewPagerIndicator viewPagerIndicator;

    protected Adapter adapter;

    protected int getTargetPagerPosition() {
        return -1;
    }

    protected int getCurrentPagerPosition() {
        return viewPager.getCurrentItem();
    }

    @Nullable
    protected I getCurrentPagerItem() {
        final int position = getCurrentPagerPosition();
        if (position >= 0 && position < adapter.getCount()) {
            return adapter.getItem(position);
        }
        return null;
    }

    @Override
    protected boolean isDataEmpty() {
        return adapter.isEmpty();
    }

    @Override
    protected boolean isDataEmpty(@Nullable List<I> data) {
        return data == null || data.isEmpty();
    }

    @Override
    protected void initViews(@NotNull View rootView) {
        super.initViews(rootView);
        final int viewPagerId = getViewPagerId();
        viewPager = rootView.findViewById(viewPagerId);
        final int indicatorId = getViewPagerIndicatorId();
        if (indicatorId != 0) {
            viewPagerIndicator = rootView.findViewById(indicatorId);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupAdapter();
        setupViewPager();
        viewPager.addOnPageChangeListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseAdapter();
        viewPager.removeOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        updateViewsByCurrentAdapterPosition(i, adapter.getItem(i), adapter.getCount());
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    protected void onLoaded(@NotNull List<I> data) {
        reloadAdapter(data);
        super.onLoaded(data);
    }

    @Override
    protected void onEmpty() {
        reloadAdapter(null);
        super.onEmpty();
    }

    @NotNull
    protected abstract Adapter initAdapter();

    @IdRes
    protected abstract int getViewPagerId();

    @IdRes
    protected abstract int getViewPagerIndicatorId();

    protected boolean allowHideRecyclerOnLoading() {
        return true;
    }

    protected boolean allowHideRecyclerOnFail() {
        return true;
    }

    protected boolean allowHideRecyclerOnEmpty() {
        return true;
    }

    protected void processLoading(boolean isLoading) {
        super.processLoading(isLoading);
        invalidatePagerVisibility();
    }

    protected void processEmpty() {
        super.processEmpty();
        if (viewPagerIndicator != null) {
            viewPagerIndicator.notifyDataSetChanged();
        }
        final int targetPosition = getTargetPagerPosition();
        if (targetPosition >= 0 && targetPosition < adapter.getCount()) {
            viewPager.setCurrentItem(targetPosition);
        } else {
            final int currentPosition = viewPager.getCurrentItem();
            final int count = adapter.getCount();
            updateViewsByCurrentAdapterPosition(currentPosition, currentPosition >= 0 && currentPosition < count ? adapter.getItem(currentPosition) : null, count);
        }
        invalidatePagerVisibility();
    }

    protected void processError() {
        super.processError();
        invalidatePagerVisibility();
    }

    protected boolean isPagerVisible() {
        return !(allowHideRecyclerOnFail() && isLoadErrorOccurred()
                || allowHideRecyclerOnEmpty() && isDataEmpty()
                || allowHideRecyclerOnLoading() && isLoading);
    }

    protected void invalidatePagerVisibility() {
        final boolean isPagerVisible = isPagerVisible();
        if (isPagerVisible) {
            viewPager.setVisibility(View.VISIBLE);
            if (viewPagerIndicator != null) {
                viewPagerIndicator.setVisibility(View.VISIBLE);
            }
        } else {
            viewPager.setVisibility(View.GONE);
            if (viewPagerIndicator != null) {
                viewPagerIndicator.setVisibility(View.GONE);
            }
        }
    }

    protected abstract void updateViewsByCurrentAdapterPosition(int position, @Nullable I item, int count);

    protected abstract void setupViewPager();

    @CallSuper
    protected void setupAdapter() {
        releaseAdapter();
        adapter = initAdapter();
        applyAdapter();
    }

    @CallSuper
    protected void releaseAdapter() {
        adapter = null;
        applyAdapter();
    }

    protected void applyAdapter() {
        viewPager.setAdapter(adapter);
        if (viewPagerIndicator != null) {
            if (adapter != null) {
                viewPagerIndicator.setViewPager(viewPager);
            } else {
                viewPagerIndicator.setViewPager(null);
            }
        }
    }

    protected void reloadAdapter(Collection<I> items) {
        adapter.setItems(items);
    }
}
