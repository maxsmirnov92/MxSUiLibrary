package net.maxsmr.android.recyclerview.adapters;

import android.content.Context;
import android.database.Observable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseRecyclerViewAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {

    @NotNull
    protected final ItemsEventsObservable<I, VH> mItemsEventsObservable = new ItemsEventsObservable<>();

    @NotNull
    protected final Context mContext;

    @LayoutRes
    protected final int mBaseItemLayoutId;

    @NotNull
    protected final ArrayList<I> mItems = new ArrayList<>();

    protected boolean mNotifyOnChange = true;

    protected BaseRecyclerViewAdapter(@NotNull Context context) {
        this(context, 0, null);
    }

    protected BaseRecyclerViewAdapter(@NotNull Context context, @LayoutRes int baseItemLayoutId, @Nullable Collection<I> items) {
        this.mContext = context;
        this.mBaseItemLayoutId = baseItemLayoutId;
        this.setItems(items);
    }

    public void registerItemsEventsListener(@NotNull ItemsEventsListener<I> listener) {
        mItemsEventsObservable.registerObserver(listener);
    }

    public void unregisterItemsEventsListener(@NotNull ItemsEventsListener<I> listener) {
        mItemsEventsObservable.unregisterObserver(listener);
    }

    protected final void rangeCheck(int position) {
        synchronized (mItems) {
            if (position < 0 || position >= mItems.size()) {
                throw new IndexOutOfBoundsException("incorrect position: " + position);
            }
        }
    }

    protected final void rangeCheckForAdd(int position) {
        synchronized (mItems) {
            if (position < 0 || position > mItems.size()) {
                throw new IndexOutOfBoundsException("incorrect add position: " + position);
            }
        }
    }

    @NotNull
    public final ArrayList<I> getItems() {
        synchronized (mItems) {
            return new ArrayList<>(mItems);
        }
    }

    @Nullable
    public final I getFirstItem() {
        if (!isEmpty()) {
            return getItem(0);
        }
        return null;
    }

    @Nullable
    public final I getLastItem() {
        if (!isEmpty()) {
            return getItem(getItemCount() - 1);
        }
        return null;
    }

    @Nullable
    public final I getItem(int at) throws IndexOutOfBoundsException {
        synchronized (mItems) {
            rangeCheck(at);
            return mItems.get(at);
        }
    }

    public final int indexOf(I item) {
        synchronized (mItems) {
            return mItems.indexOf(item);
        }
    }

    public final int lastIndexOf(I item) {
        synchronized (mItems) {
            return mItems.lastIndexOf(item);
        }
    }

    public void sort(@NotNull Comparator<? super I> comparator) {
        synchronized (mItems) {
            Collections.sort(mItems, comparator);
            if (mNotifyOnChange) {
                notifyDataSetChanged();
            }
        }
    }

    /**
     * @param items null for reset adapter
     */
    public final void setItems(@Nullable Collection<I> items) {
        synchronized (mItems) {
            if (!mItems.equals(items)) {
                clearItems();
                if (items != null) {
                    this.mItems.addAll(items);
                }
                onItemsSet();
            }
        }
    }

    protected void onItemsSet() {
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
        mItemsEventsObservable.notifyItemsSet(getItems());
    }

    public final void clearItems() {
        synchronized (mItems) {
            if (!isEmpty()) {
                int previousSize = getItemCount();
                mItems.clear();
                onItemsRangeRemoved(0, previousSize - 1, previousSize);
            }
        }
    }

    protected void onItemsCleared(int previousSize) {
    }

    public final void addItem(int to, @Nullable I item) throws IndexOutOfBoundsException {
        synchronized (mItems) {
            rangeCheckForAdd(to);
            mItems.add(to, item);
            onItemAdded(to, item);
        }
    }

    public final void addItem(@Nullable I item) {
        addItem(getItemCount(), item);
    }

    public final void addFirstItem(@Nullable I item) throws IndexOutOfBoundsException {
        addItem(0, item);
    }

    @CallSuper
    protected void onItemAdded(int to, @Nullable I item) {
        if (mNotifyOnChange) {
            if (to == 0) {
                notifyDataSetChanged();
            } else {
                notifyItemInserted(to);
            }
        }
        mItemsEventsObservable.notifyItemAdded(to, item);

    }

    public final void addItems(int to, @Nullable Collection<I> items) {
        synchronized (mItems) {
            rangeCheckForAdd(to);
            if (items != null) {
                mItems.addAll(to, items);
                onItemsAdded(to, items);
            }
        }
    }

    public final void addItems(@Nullable Collection<I> items) {
        addItems(getItemCount(), items);
    }

    @CallSuper
    protected void onItemsAdded(int to, @NotNull Collection<I> items) {
        if (mNotifyOnChange) {
            if (to == 0) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeInserted(to, items.size());
            }
        }
        mItemsEventsObservable.notifyItemsAdded(to, items);

    }

    public final void setItem(int in, @Nullable I item) {
        synchronized (mItems) {
            rangeCheck(in);
            mItems.set(in, item);
            onItemSet(in, item);
        }
    }

    @CallSuper
    protected void onItemSet(int in, @Nullable I item) {
        if (mNotifyOnChange) {
            notifyItemChanged(in);
        }
        mItemsEventsObservable.notifyItemSet(in, item);
    }

    @Nullable
    public final I replaceItem(int in, @Nullable I newItem) {
        synchronized (mItems) {
            rangeCheck(in);
            setNotifyOnChange(false);
            I replacedItem = getItem(in);
            mItems.remove(in);
            onItemRemoved(in, replacedItem);
            addItem(in, newItem);
            setNotifyOnChange(true);
            notifyItemChanged(in);
            return replacedItem;
        }
    }

    @Nullable
    public final I replaceItem(@Nullable I replaceableItem, @Nullable I newItem) {
        return replaceItem(indexOf(replaceableItem), newItem);
    }

    @NotNull
    public final List<I> replaceItemsRange(int from, int to, @Nullable Collection<I> newItems) {
        synchronized (mItems) {
            setNotifyOnChange(false);
            List<I> replacedItems = removeItemsRange(from, to);
            addItems(newItems);
            setNotifyOnChange(true);
            notifyItemRangeChanged(from, to - from);
            return replacedItems;
        }
    }

    @Nullable
    public final I removeItem(@Nullable I item) {
        return removeItem(indexOf(item));
    }

    @Nullable
    public final I removeItem(int from) {
        synchronized (mItems) {
            rangeCheck(from);
            I removedItem = getItem(from);
            mItems.remove(from);
            onItemRemoved(from, removedItem);
            return removedItem;
        }
    }

    @CallSuper
    protected void onItemRemoved(int from, @Nullable I item) {
        if (mNotifyOnChange) {
            notifyItemRemoved(from);
        }
        mItemsEventsObservable.notifyItemRemoved(from, item);
    }

    @NotNull
    public final List<I> removeItemsRange(int from, int to) {
        rangeCheck(from);
        rangeCheck(to);
        synchronized (mItems) {
            int previousSize = getItemCount();
            List<I> removed = new ArrayList<>();
            int position = 0;
            Iterator<I> iterator = mItems.iterator();
            while (iterator.hasNext()) {
                if (position >= from && position <= to) {
                    I item = iterator.next();
                    iterator.remove();
                    removed.add(item);
                }
                position++;
            }
            if (!removed.isEmpty()) {
                onItemsRangeRemoved(from, to, previousSize);
            }
            return removed;
        }
    }

    public final void removeAllItems() {
        synchronized (mItems) {
            if (!isEmpty()) {
                removeItemsRange(0, getItemCount() - 1);
            }
        }
    }

    @CallSuper
    protected void onItemsRangeRemoved(int from, int to, int previousSize) {
        if (mNotifyOnChange) {
            notifyItemRangeRemoved(from, to - from);
        }
        mItemsEventsObservable.notifyItemsRangeRemoved(from, to, previousSize);
        if (from == 0 && to == previousSize - 1) {
            onItemsCleared(previousSize);
        }
    }

    protected final View onInflateView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(parent.getContext())
                .inflate(getLayoutIdForViewType(viewType), parent, false);
    }

    @Nullable
    protected View getClickableView(@NotNull VH holder) {
        return holder.itemView;
    }

    @LayoutRes
    protected int getLayoutIdForViewType(int viewType) {
        return mBaseItemLayoutId;
    }

    @Override
    @NotNull
    public abstract VH onCreateViewHolder(@NotNull ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(@NotNull VH holder, int position) {
        final I item = (position >= 0 && position < mItems.size()) ? mItems.get(position) : null;
        processItem(holder, item, position);
    }

    protected boolean allowSetClickListener(@Nullable final I item, final int position) {
        return !isItemEmpty(item, position);
    }

    protected boolean allowSetLongClickListener(@Nullable final I item, final int position) {
        return allowSetClickListener(item, position);
    }

    protected boolean allowFillHolderForItem(@NotNull VH holder, @Nullable final I item, final int position) {
        return true;
    }

    protected boolean isItemEmpty(@Nullable final I item, final int position) {
        return item == null;
    }

    @SuppressWarnings("unchecked")
    @CallSuper
    protected void processItem(@NotNull VH holder, @Nullable final I item, final int position) {

        if (!isItemEmpty(item, position) && item != null) {

            View clickableView = getClickableView(holder);

            if (clickableView != null) {
                clickableView.setOnClickListener(v -> {
                    if (allowSetClickListener(item, position)) {
                        mItemsEventsObservable.notifyItemClick(position, item);
                    }
                });
                clickableView.setOnLongClickListener(v -> {
                    if (allowSetLongClickListener(item, position)) {
                        return mItemsEventsObservable.notifyItemLongClick(position, item);
                    }
                    return false;
                });
            }

            if (allowFillHolderForItem(holder, item, position)) {
                holder.displayData(position, item, getItemCount());
            }

        } else {

            if (allowFillHolderForItem(holder, item, position)) {
                holder.displayEmptyData(position, item, getItemCount());
            }
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public final boolean isNotifyOnChange() {
        return mNotifyOnChange;
    }

    public void setNotifyOnChange(boolean enable) {
        mNotifyOnChange = enable;
    }

    @Override
    @CallSuper
    public void onViewRecycled(@NotNull VH holder) {
        super.onViewRecycled(holder);
        holder.onViewRecycled();
        holder.itemView.setOnClickListener(null);
    }

    @CallSuper
    public void release() {
        mItemsEventsObservable.unregisterAll();
    }

    public static abstract class ViewHolder<I> extends RecyclerView.ViewHolder {

        @NotNull
        protected final Context context;

        public ViewHolder(@NotNull ViewGroup parent, @LayoutRes int layoutId) {
            this(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
        }

        public ViewHolder(@NotNull View view) {
            super(view);
            this.context = view.getContext();
        }

        public void displayData(int position, @NotNull final I item, int count) {
            itemView.setVisibility(View.VISIBLE);
        }

        public void displayEmptyData(int position, @Nullable final I item, int count) {
            itemView.setVisibility(View.GONE);
        }

        protected void onViewRecycled() {
        }
    }

    protected static class ItemsEventsObservable<I, VH extends BaseRecyclerViewAdapter.ViewHolder>
            extends Observable<ItemsEventsListener<I>> {

        protected void notifyItemClick(int position, I item) {
            synchronized (mObservers) {
                for (ItemsEventsListener<I> l : mObservers) {
                    l.onItemClick(position, item);
                }
            }
        }

        protected boolean notifyItemLongClick(int position, I item) {
            synchronized (mObservers) {
                boolean consumed = false;
                for (ItemsEventsListener<I> l : mObservers) {
                    if (l.onItemLongClick(position, item)) {
                        consumed = true;
                    }
                }
                return consumed;
            }
        }

        protected void notifyItemAdded(int to, @Nullable I item) {
            synchronized (mObservers) {
                for (ItemsEventsListener<I> l : mObservers) {
                    l.onItemAdded(to, item);
                }
            }
        }

        protected void notifyItemsAdded(int to, @NotNull Collection<I> items) {
            synchronized (mObservers) {
                for (ItemsEventsListener<I> l : mObservers) {
                    l.onItemsAdded(to, items);
                }
            }
        }

        protected void notifyItemSet(int to, I item) {
            synchronized (mObservers) {
                for (ItemsEventsListener<I> l : mObservers) {
                    l.onItemSet(to, item);
                }
            }
        }

        protected void notifyItemsSet(@NotNull List<I> items) {
            synchronized (mObservers) {
                for (ItemsEventsListener<I> l : mObservers) {
                    l.onItemsSet(items);
                }
            }
        }

        protected void notifyItemRemoved(int from, I item) {
            synchronized (mObservers) {
                for (ItemsEventsListener<I> l : mObservers) {
                    l.onItemRemoved(from, item);
                }
            }
        }

        protected void notifyItemsRangeRemoved(int from, int to, int previousSize) {
            synchronized (mObservers) {
                for (ItemsEventsListener<I> l : mObservers) {
                    l.onItemsRangeRemoved(from, to, previousSize);
                }
            }
        }
    }

    public interface ItemsEventsListener<I> {

        void onItemClick(int position, I item);

        /**
         * @return true if event consumed
         */
        boolean onItemLongClick(int position, I item);

        void onItemAdded(int to, @Nullable I item);

        void onItemsAdded(int to, @NotNull Collection<I> items);

        void onItemSet(int to, I item);

        void onItemsSet(@NotNull List<I> items);

        void onItemRemoved(int from, I item);

        void onItemsRangeRemoved(int from, int to, int previousSize);
    }


}
