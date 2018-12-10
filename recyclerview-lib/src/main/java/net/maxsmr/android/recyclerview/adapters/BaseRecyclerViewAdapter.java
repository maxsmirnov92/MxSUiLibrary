package net.maxsmr.android.recyclerview.adapters;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class BaseRecyclerViewAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {

    @NotNull
    protected final Context mContext;

    @LayoutRes
    protected final int mBaseItemLayoutId;

    @NotNull
    private final ArrayList<I> mItems = new ArrayList<>();

    private OnProcessingItemListener<I, VH> mProcessingItemListener;

    protected OnItemClickListener<I> mItemClickListener;

    protected OnItemLongClickListener<I> mItemLongClickListener;

    protected OnItemAddedListener<I> mItemAddedListener;

    protected OnItemsRemovedListener<I> mItemsRemovedListener;

    protected OnItemsSetListener<I> mItemsSetListener;

    private boolean mNotifyOnChange = true;

    protected BaseRecyclerViewAdapter(@NotNull Context context) {
        this(context, 0, null);
    }

    protected BaseRecyclerViewAdapter(@NotNull Context context, @LayoutRes int baseItemLayoutId, @Nullable Collection<I> items) {
        this.mContext = context;
        this.mBaseItemLayoutId = baseItemLayoutId;
        this.setItems(items);
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
        if (mItemsSetListener != null) {
            mItemsSetListener.onItemsSet(getItems());
        }
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

    public final void addFirstItem(@Nullable I item) throws IndexOutOfBoundsException{
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
        if (mItemAddedListener != null) {
            mItemAddedListener.onItemAdded(to, item);
        }
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
        if (mItemAddedListener != null) {
            mItemAddedListener.onItemsAdded(to, items);
        }
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
        if (mItemsSetListener != null) {
            mItemsSetListener.onItemSet(in, item);
        }
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
        if (mItemsRemovedListener != null) {
            mItemsRemovedListener.onItemRemoved(from, item);
        }
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
        if (mItemsRemovedListener != null) {
            mItemsRemovedListener.onItemsRangeRemoved(from, to, previousSize);
        }
        if (from == 0 && to == previousSize - 1) {
            onItemsCleared(previousSize);
        }
    }

    protected final View onInflateView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(parent.getContext())
                .inflate(getLayoutIdForViewType(viewType), parent, false);
    }

    @LayoutRes
    protected int getLayoutIdForViewType(int viewType) {
        return mBaseItemLayoutId;
    }

    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        final I item = (position >= 0 && position < mItems.size()) ? mItems.get(position) : null;
        processItem(holder, item, position);
    }

    protected boolean allowSetClickListener(@Nullable final I item, final int position) {
        return true;
    }

    protected boolean allowSetLongClickListener(@Nullable final I item, final int position) {
        return true;
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

        if (mProcessingItemListener != null) {
            mProcessingItemListener.onProcessingItem(holder, item, position);
        }

        if (!isItemEmpty(item, position) && item != null) {

            if (allowSetClickListener(item, position)) {
                holder.itemView.setOnClickListener(v -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(position, item);
                    }
                });
            }

            if (allowSetLongClickListener(item, position)) {
                holder.itemView.setOnLongClickListener(v -> {
                    boolean consumed = false;
                    if (mItemLongClickListener != null) {
                        consumed = mItemLongClickListener.onItemLongClick(position, item);
                    }
                    return consumed;
                });
            }

            if (allowFillHolderForItem(holder, item, position)) {
                holder.displayData(position, item);
            }

        } else {
            
            if (allowFillHolderForItem(holder, item, position)) {
                holder.displayNoData(position, item);
            }
        }
    }

    public final boolean isEmpty() {
        return getItemCount() == 0;
    }

    @Override
    public final int getItemCount() {
        return mItems.size();
    }

    public final boolean isNotifyOnChange() {
        return mNotifyOnChange;
    }

    public void setNotifyOnChange(boolean enable) {
        mNotifyOnChange = enable;
    }

    public void setOnItemClickListener(OnItemClickListener<I> listener) {
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<I> listener) {
        this.mItemLongClickListener = listener;
    }

    public void setOnProcessingItemListener(OnProcessingItemListener<I, VH> l) {
        this.mProcessingItemListener = l;
    }

    @Override
    @CallSuper
    public void onViewRecycled(VH holder) {
        super.onViewRecycled(holder);
        holder.onViewRecycled();
        holder.itemView.setOnClickListener(null);
    }

    public void setOnItemAddedListener(OnItemAddedListener<I> itemAddedListener) {
        this.mItemAddedListener = itemAddedListener;
    }

    public void setOnItemsSetListener(OnItemsSetListener<I> itemsSetListener) {
        this.mItemsSetListener = itemsSetListener;
    }

    public void setOnItemsRemovedListener(OnItemsRemovedListener<I> itemsRemovedListener) {
        this.mItemsRemovedListener = itemsRemovedListener;
    }

    public static abstract class ViewHolder<I> extends RecyclerView.ViewHolder {

        @NotNull
        protected final Context context;

        public ViewHolder(@NotNull View view) {
            super(view);
            this.context = view.getContext();
        }

        protected void displayData(int position, @NotNull final I item) {
            itemView.setVisibility(View.VISIBLE);
        }

        protected void displayNoData(int position, @Nullable final I item) {
            itemView.setVisibility(View.GONE);
        }

        protected void onViewRecycled() {
        }
    }

    public interface OnItemClickListener<I> {
        void onItemClick(int position, I item);
    }

    public interface OnItemLongClickListener<I> {

        /**
         * @return true if event consumed
         */
        boolean onItemLongClick(int position, I item);
    }

    public interface OnProcessingItemListener<I, VH extends BaseRecyclerViewAdapter.ViewHolder> {
        void onProcessingItem(@NotNull VH holder, @Nullable I item, int position);
    }

    public interface OnItemAddedListener<I> {
        void onItemAdded(int to, @Nullable I item);

        void onItemsAdded(int to, @NotNull Collection<I> items);
    }

    public interface OnItemsSetListener<I> {
        void onItemSet(int to, I item);

        void onItemsSet(@NotNull List<I> items);
    }

    public interface OnItemsRemovedListener<I> {
        void onItemRemoved(int from, I item);

        void onItemsRangeRemoved(int from, int to, int previousSize);
    }

}
