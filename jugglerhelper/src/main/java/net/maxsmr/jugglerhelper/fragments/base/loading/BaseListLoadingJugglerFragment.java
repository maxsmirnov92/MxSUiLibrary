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
import net.maxsmr.commonutils.android.gui.views.RecyclerScrollableController;
import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.jugglerhelper.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseListLoadingJugglerFragment<I extends Comparable<I>, Adapter extends BaseRecyclerViewAdapter<I, ?>> extends BaseLoadingJugglerFragment<List<I>>
        implements BaseRecyclerViewAdapter.OnItemClickListener<I>, BaseRecyclerViewAdapter.OnItemLongClickListener<I>, BaseRecyclerViewAdapter.OnItemAddedListener<I>, BaseRecyclerViewAdapter.OnItemsSetListener<I>, BaseRecyclerViewAdapter.OnItemsRemovedListener<I>, RecyclerScrollableController.OnLastItemVisibleListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(BaseListLoadingJugglerFragment.class);

    private ExecutorService service;

    protected RecyclerView recycler;

    protected RecyclerScrollableController recyclerScrollableController;

    protected Adapter adapter;

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

    @NonNull
    protected List<? extends RecyclerView.ItemDecoration> getItemDecorations() {
        return Collections.emptyList();
    }

    @NonNull
    protected abstract Adapter initAdapter();

    protected abstract boolean allowReloadOnNetworkRestored();

    protected void reloadAdapter(@Nullable List<I> items) {
        logger.debug("reloadAdapter(), items=" + items);
        if (adapter != null) {
            sortAndRemoveDuplicateItemsFromList(items);
            adapter.setItems(items);
        }
    }

    protected void loading(boolean isLoading) {
        super.loading(isLoading);
        recycler.setVisibility(View.GONE);
    }

    @Override
    protected void afterLoading(@Nullable List<I> items) {
        logger.debug("afterLoading");
        if (items != null && !items.isEmpty()) {
            onLoaded(items);
        } else {
            onEmpty();
        }
    }

    protected boolean isDataEmpty() {
        return adapter.isEmpty();
    }

    protected void processEmpty() {
        super.processEmpty();
        boolean hasItems = !isDataEmpty();
        recycler.setVisibility(hasItems ? View.VISIBLE : View.GONE);
    }

    @CallSuper
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
        for (RecyclerView.ItemDecoration decoration : getItemDecorations()) {
            if (decoration != null) {
                recycler.addItemDecoration(decoration);
            }
        }
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(recyclerScrollableController = new RecyclerScrollableController(this));

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

        recycler.removeOnScrollListener(recyclerScrollableController);
        recyclerScrollableController = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service.shutdown();
    }

    @Nullable
    protected abstract Comparator<I> getSortComparator();

    protected boolean isDuplicateItems(@Nullable I one, @Nullable I another) {
        return CompareUtils.objectsEqual(one, another); // TODO default
    }

    /**
     * @param items will be modified if contains duplicate items
     * @return removed duplicate items
     */
    @NonNull
    protected List<I> sortAndRemoveDuplicateItemsFromList(@Nullable List<I> items) {

        final Comparator<I> comparator = getSortComparator();

        List<I> duplicateItems = new ArrayList<>();

        if (items != null) {

            if (comparator != null) {
                Collections.sort(items, comparator);
            }

            Iterator<I> iterator = items.iterator();

            int index = -1;

            while (iterator.hasNext()) {

                I item = iterator.next();
                index++;

                boolean isDuplicate = false;

                for (int i = index + 1; i < items.size(); i++) {
                    I compareItem = items.get(i);
                    if (isDuplicateItems(compareItem, item)) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate || item == null) {
                    logger.debug("removing duplicate item: " + item + "...");
                    iterator.remove();
                    index--;
                    duplicateItems.add(item);
                }
            }

            if (comparator != null) {
                Collections.sort(items, comparator);
                logger.debug("sorted items: " + items);
            }
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

    @CallSuper
    protected void onLoaded(@Nullable List<I> items) {
        isLoadErrorOccurred = false;
        reloadAdapter(items);
        processEmpty();
    }

    protected void resortAdapterItems() {
        resortAdapterItems(getSortComparator());
    }

    protected void resortAdapterItems(@Nullable Comparator<? super I> comparator) {
        List<I> items = adapter.getItems();
        if (comparator == null) {
            Collections.sort(items);
        } else {
            Collections.sort(items, comparator);
        }
        if (!items.equals(adapter.getItems())) {
            adapter.setItems(items);
        }
    }

    private void postActionOnRecyclerView(@NonNull final Runnable r, final long delay) {

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

        if (!service.isShutdown()) {
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

    private void postActionOnRecyclerView(@NonNull final Runnable r) {
        postActionOnRecyclerView(r, 0);
    }

}