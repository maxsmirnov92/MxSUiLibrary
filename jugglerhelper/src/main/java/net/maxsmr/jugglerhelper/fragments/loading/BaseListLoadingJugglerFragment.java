package net.maxsmr.jugglerhelper.fragments.loading;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.maxsmr.android.recyclerview.adapters.base.BaseRecyclerViewAdapter;
import net.maxsmr.android.recyclerview.scroll.RecyclerScrollableController;
import net.maxsmr.jugglerhelper.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Deprecated
public abstract class BaseListLoadingJugglerFragment<I, Adapter extends BaseRecyclerViewAdapter<I, ?>>
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
    protected void initViews(@NotNull View rootView) {
        super.initViews(rootView);
        recycler = rootView.findViewById(getRecyclerId());
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

    protected void reloadAdapter(@Nullable List<I> items) {
        if (adapter != null) {
            if (!allowDuplicateItems()) {
                removeDuplicateItemsFromList(items);
            }
            adapter.setItems(items, true);
        }
    }

    protected boolean isDataEmpty() {
        return adapter.isEmpty();
    }

    @Override
    protected boolean isDataEmpty(List<I> data) {
        return data == null || data.isEmpty();
    }

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
        invalidateRecyclerVisibility();
    }

    protected void processEmpty() {
        super.processEmpty();
        invalidateRecyclerVisibility();
    }

    protected void processError() {
        super.processError();
        invalidateRecyclerVisibility();
    }

    protected boolean isRecyclerVisible() {
        return !(allowHideRecyclerOnFail() && isLoadErrorOccurred()
                || allowHideRecyclerOnEmpty() && isDataEmpty()
                || allowHideRecyclerOnLoading() && isLoading);
    }

    protected void invalidateRecyclerVisibility() {
        recycler.setVisibility(isRecyclerVisible() ? View.VISIBLE : View.GONE);
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

    protected boolean isDuplicateItems(@Nullable I one, @Nullable I another) {
        return one == another;
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
    public void onItemClick(int position, @Nullable I item) {

    }

    @Override
    public boolean onItemLongClick(int position, @Nullable I item) {
        return false;
    }

    @Override
    public void onItemFocusChanged(int position, @Nullable I item) {

    }

    @Override
    public void onItemAdded(int to, @Nullable I item, int previousSize) {

    }

    @Override
    public void onItemsAdded(int to, @NotNull Collection<? extends I> items, int previousSize) {

    }

    @Override
    public void onItemSet(int to, @Nullable I item) {

    }

    @Override
    public void onItemsSet(@NotNull List<? extends I> items) {

    }

    @Override
    public void onItemRemoved(int from, @Nullable I item) {

    }

    @Override
    public void onItemsRangeRemoved(int from, int to, int previousSize, @NotNull List<? extends I> removedItems) {

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
                    getMainHandler().postDelayed(wrappedRunnable, delay);
                });
                try {
                    task.get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                getMainHandler().postDelayed(r, delay);
            }
        }

    }

    protected void postActionOnRecyclerView(@NotNull final Runnable r) {
        postActionOnRecyclerView(r, 0);
    }

}
