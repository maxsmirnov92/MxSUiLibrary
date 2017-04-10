package net.maxsmr.jugglerhelper.fragments.base.loading;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter;
import net.maxsmr.commonutils.android.gui.GuiUtils;
import net.maxsmr.commonutils.android.gui.views.recycler.RecyclerScrollableController;
import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.jugglerhelper.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseListLoadingJugglerFragment<I extends Comparable<I>, Adapter extends BaseRecyclerViewAdapter<I, ?>> extends BaseLoadingJugglerFragment<List<I>>
        implements BaseRecyclerViewAdapter.OnItemClickListener<I>, BaseRecyclerViewAdapter.OnItemLongClickListener<I>, BaseRecyclerViewAdapter.OnItemAddedListener<I>, BaseRecyclerViewAdapter.OnItemsSetListener<I>, BaseRecyclerViewAdapter.OnItemsRemovedListener<I>, RecyclerScrollableController.OnLastItemVisibleListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(BaseListLoadingJugglerFragment.class);

    private final List<RecyclerView.ItemDecoration> registeredDecorations = new LinkedList<>();

    private final List<RecyclerView.OnScrollListener> registeredScrollListeners = new LinkedList<>();

    protected ExecutorService service;

    protected RecyclerView recycler;

    protected Adapter adapter;

    @NonNull
    protected List<RecyclerView.ItemDecoration> getRegisteredDecorations() {
        return Collections.unmodifiableList(registeredDecorations);
    }

    @NonNull
    protected List<RecyclerView.OnScrollListener> getRegisteredScrollListeners() {
        return Collections.unmodifiableList(registeredScrollListeners);
    }

    @Nullable
    protected RecyclerView.LayoutManager getRegisteredLayoutManager() {
        return recycler.getLayoutManager();
    }

    /**
     * @return 0 if it's not used or recycler has different layouts
     */
    @LayoutRes
    protected abstract int getBaseItemLayoutId();

    @IdRes
    protected int getRecyclerId() {
        return R.id.recycler;
    }

    @CallSuper
    protected void onBindViews(@NonNull View rootView) {
        super.onBindViews(rootView);
        recycler = GuiUtils.findViewById(rootView, getRecyclerId());
    }

    @NonNull
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
                    registeredDecorations.add(decoration);
                }
            }
        }
    }

    protected void removeItemDecorations() {
        Iterator<RecyclerView.ItemDecoration> it = registeredDecorations.iterator();
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

    @NonNull
    protected abstract Adapter initAdapter();

    protected abstract boolean allowReloadOnNetworkRestored();

    protected abstract boolean allowDuplicateItems();

    protected abstract boolean allowSort();

    protected void reloadAdapter(@Nullable List<I> items) {
        logger.debug("reloadAdapter(), items=" + items);
        if (adapter != null) {
            if (!allowDuplicateItems()) {
                sortAndRemoveDuplicateItemsFromList(items);
            } else if (allowSort()) {
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        View rootView = getView();

        if (rootView == null) {
            throw new IllegalStateException("root view was not created");
        }

        if (recycler == null) {
            throw new RuntimeException("recycler view not found");
        }

        adapter = initAdapter();
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        adapter.setOnItemAddedListener(this);
        adapter.setOnItemsSetListener(this);
        adapter.setOnItemsRemovedListener(this);

        recycler.setLayoutManager(getRecyclerLayoutManager());

        addItemDecorations();
        addScrollListeners();

        recycler.setAdapter(adapter);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        adapter.setOnItemClickListener(null);
        adapter.setOnItemLongClickListener(null);
        adapter.setOnItemAddedListener(null);
        adapter.setOnItemsSetListener(null);
        adapter.setOnItemsRemovedListener(null);

        removeItemDecorations();
        removeScrollListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service.shutdown();
        service = null;
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
    @NonNull
    protected List<I> sortAndRemoveDuplicateItemsFromList(@Nullable List<I> items) {

        List<I> duplicateItems = new ArrayList<>();

        if (items != null) {

            List<I> filteredItems = new ArrayList<>();

            if (allowSort()) {
                sortItems(items, getSortComparator());
            }

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

            if (allowSort()) {
                sortItems(items, getSortComparator());
            }

//            Iterator<I> iterator = items.iterator();
//
//            int index = -1;
//
//            while (iterator.hasNext()) {
//
//                I item = iterator.next();
//                index++;
//
//                boolean isDuplicate = false;
//
//                    for (int i = index + 1; i < items.size(); i++) {
//                        I compareItem = items.get(i);
//                        if (isDuplicateItems(compareItem, item)) {
//                            isDuplicate = true;
//                            break;
//                        }
//                    }
//
//                if (isDuplicate || item == null) {
//                    logger.debug("removing duplicate item: " + item + "...");
//                    iterator.remove();
//                    index--;
//                    duplicateItems.add(item);
//                }
//            }
        }

        return duplicateItems;
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
    public void onItemsAdded(int to, @NonNull Collection<I> items) {
    }

    @Override
    public void onItemSet(int to, I item) {
    }

    @Override
    public void onItemsSet(@NonNull List<I> items) {
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

    protected void onLoaded(@NonNull List<I> items) {
        reloadAdapter(items);
        super.onLoaded(items);
    }

    protected void postActionOnRecyclerView(@NonNull final Runnable r, final long delay) {

        if (delay < 0) {
            throw new IllegalArgumentException("incorrect delay: " + delay);
        }

        final Runnable wrappedRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    r.run();
                }
            }
        };

        if (service != null && !service.isShutdown()) {
            if (recycler.isComputingLayout()) service.submit(new Runnable() {
                @Override
                public void run() {
                    while (recycler.isComputingLayout()) ;
                    new Handler(Looper.getMainLooper()).postDelayed(wrappedRunnable, delay);
                }
            });
            else {
                new Handler(Looper.getMainLooper()).postDelayed(wrappedRunnable, delay);
            }
        }
    }

    protected void postActionOnRecyclerView(@NonNull final Runnable r) {
        postActionOnRecyclerView(r, 0);
    }

}
