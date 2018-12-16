package net.maxsmr.jugglerhelper.fragments.loading;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter;
import net.maxsmr.commonutils.android.gui.GuiUtils;
import net.maxsmr.commonutils.android.gui.views.recycler.RecyclerScrollableController;
import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.jugglerhelper.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class BaseListLoadingJugglerFragment<I extends Comparable<I>, Adapter extends BaseRecyclerViewAdapter<I, ?>>
        extends BaseLoadingJugglerFragment<List<I>>
        implements BaseRecyclerViewAdapter.ItemsEventsListener<I>, RecyclerScrollableController.OnLastItemVisibleListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private final List<RecyclerView.ItemDecoration> registeredItemDecorations = new LinkedList<>();

    private final List<RecyclerView.OnScrollListener> registeredScrollListeners = new LinkedList<>();

    protected ExecutorService service;

    protected RecyclerView recycler;

    protected Adapter adapter;

    @NotNull
    protected List<RecyclerView.ItemDecoration> getRegisteredItemDecorations() {
        return Collections.unmodifiableList(registeredItemDecorations);
    }

    @NotNull
    protected List<RecyclerView.OnScrollListener> getRegisteredScrollListeners() {
        return Collections.unmodifiableList(registeredScrollListeners);
    }

    @Nullable
    protected RecyclerView.LayoutManager getRegisteredLayoutManager() {
        return recycler.getLayoutManager();
    }

    @IdRes
    protected int getRecyclerId() {
        return R.id.recycler;
    }

    @CallSuper
    protected void onBindViews(@NotNull View rootView) {
        super.onBindViews(rootView);
        recycler = GuiUtils.findViewById(rootView, getRecyclerId());
    }

    @NotNull
    protected RecyclerView.LayoutManager getRecyclerLayoutManager() {
        return new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    }

    @Nullable
    protected List<? extends RecyclerView.ItemDecoration> getItemDecorations() {
        return Collections.emptyList();
    }

    @Nullable
    protected List<? extends RecyclerView.OnScrollListener> getScrollListeners() {
        return Collections.emptyList();
    }


    protected void addItemDecorations() {
        removeItemDecorations();
        List<? extends RecyclerView.ItemDecoration> decorations = getItemDecorations();
        if (decorations != null) {
            for (RecyclerView.ItemDecoration decoration : decorations) {
                if (decoration != null) {
                    recycler.addItemDecoration(decoration);
                    registeredItemDecorations.add(decoration);
                }
            }
        }
    }

    protected void removeItemDecorations() {
        Iterator<RecyclerView.ItemDecoration> it = registeredItemDecorations.iterator();
        while (it.hasNext()) {
            RecyclerView.ItemDecoration decoration = it.next();
            recycler.removeItemDecoration(decoration);
            it.remove();
        }
    }

    protected void addScrollListeners() {
        removeScrollListeners();
        List<? extends RecyclerView.OnScrollListener> scrollListeners = getScrollListeners();
        if (scrollListeners != null) {
            for (RecyclerView.OnScrollListener listener : scrollListeners) {
                recycler.addOnScrollListener(listener);
                registeredScrollListeners.add(listener);
            }
        }
    }

    protected void removeScrollListeners() {
        Iterator<RecyclerView.OnScrollListener> it = registeredScrollListeners.iterator();
        while (it.hasNext()) {
            RecyclerView.OnScrollListener listener = it.next();
            recycler.removeOnScrollListener(listener);
            it.remove();
        }
    }

    @NotNull
    protected abstract Adapter initAdapter();

    protected abstract boolean allowDuplicateItems();

    protected abstract boolean allowSort();

    protected void reloadAdapter(@Nullable List<I> items) {
        if (adapter != null) {
            if (!allowDuplicateItems()) {
                removeDuplicateItemsFromList(items);
            }
            if (allowSort()) {
                sortItems(items, getSortComparator());
            }
            adapter.setItems(items);
        }
    }

    protected void loading(boolean isLoading) {
        super.loading(isLoading);
        recycler.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    protected boolean isDataEmpty() {
        return adapter.isEmpty();
    }

    @Override
    protected boolean isDataEmpty(List<I> data) {
        return data == null || data.isEmpty();
    }

    protected void processEmpty() {
        super.processEmpty();
        boolean hasItems = !isDataEmpty();
        recycler.setVisibility(hasItems ? View.VISIBLE : View.GONE);
    }

    protected void processError() {
        super.processError();
        recycler.setVisibility(View.GONE);
    }

    protected void setupAdapter() {

        releaseAdapter();

        adapter = initAdapter();
        adapter.registerItemsEventsListener(this);

        recycler.setLayoutManager(getRecyclerLayoutManager());

        addItemDecorations();
        addScrollListeners();

        recycler.setAdapter(adapter);
    }

    protected void releaseAdapter() {
        if (recycler == null) {
            throw new RuntimeException("recycler view not found");
        }
        if (adapter != null) {
            removeItemDecorations();
            removeScrollListeners();
            adapter.unregisterItemsEventsListener(this);
            recycler.setLayoutManager(null);
            recycler.setAdapter(null);
        }
    }


    @Nullable
    protected abstract Comparator<I> getSortComparator();

    protected boolean isDuplicateItems(@Nullable I one, @Nullable I another) {
        return CompareUtils.objectsEqual(one, another); // TODO default
    }

    protected void sortItems(List<I> items, @Nullable Comparator<? super I> comparator) {
        if (items != null) {
            if (comparator != null) {
                Collections.sort(items, comparator);
            } else {
                Collections.sort(items);
            }
        }
    }

    protected void resortAdapterItems() {
        resortAdapterItems(getSortComparator());
    }

    protected void resortAdapterItems(@Nullable Comparator<? super I> comparator) {
        List<I> items = adapter.getItems();
        sortItems(items, comparator);
        adapter.setItems(items);
    }

    /**
     * @param items will be modified if contains duplicate items
     * @return removed duplicate items
     */
    @NotNull
    protected List<I> removeDuplicateItemsFromList(@Nullable List<I> items) {

        List<I> duplicateItems = new ArrayList<>();

        if (items != null) {

            List<I> filteredItems = new ArrayList<>();

            for (I item : items) {
                if (item != null) {
                    boolean isDuplicate = false;
                    for (I filteredItem : filteredItems) {
                        if (isDuplicateItems(item, filteredItem)) {
                            duplicateItems.add(item);
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        filteredItems.add(item);
                    }
                }
            }

            items.clear();
            items.addAll(filteredItems);
        }

        return duplicateItems;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupAdapter();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service.shutdown();
        service = null;
    }

    @Override
    public void onItemClick(int position, I item) {
    }

    @Override
    public boolean onItemLongClick(int position, I item) {
        return false;
    }

    @Override
    public void onItemAdded(int to, I item) {
    }

    @Override
    public void onItemsAdded(int to, @NotNull Collection<I> items) {
    }

    @Override
    public void onItemSet(int to, I item) {
    }

    @Override
    public void onItemsSet(@NotNull List<I> items) {
    }

    @Override
    public void onItemRemoved(int from, I item) {
    }

    @Override
    public void onItemsRangeRemoved(int from, int to, int previousSize) {
    }

    @Override
    public void onLastItemVisible() {
    }

    @Override
    protected void onLoaded(@NotNull List<I> items) {
        reloadAdapter(items);
        super.onLoaded(items);
    }

    @Override
    public void onEmpty() {
        reloadAdapter(null);
        super.onEmpty();
    }

    protected void postActionOnRecyclerView(@NotNull final Runnable r, final long delay) {

        if (delay < 0) {
            throw new IllegalArgumentException("incorrect delay: " + delay);
        }

        final Runnable wrappedRunnable = () -> {
            if (isAdded()) {
                r.run();
            }
        };

        if (service != null && !service.isShutdown()) {
            if (recycler.isComputingLayout()) {
                Future<?> task = service.submit(() -> {
                    //noinspection StatementWithEmptyBody
                    while (recycler.isComputingLayout()) ;
                    mainHandler.postDelayed(wrappedRunnable, delay);
                });
                try {
                    task.get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mainHandler.postDelayed(r, delay);
            }
        }

}

    protected void postActionOnRecyclerView(@NotNull final Runnable r) {
        postActionOnRecyclerView(r, 0);
    }

}
