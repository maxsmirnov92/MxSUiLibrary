package net.maxsmr.android.recyclerview.adapters;

import android.content.Context;
import android.database.Observable;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Checkable;

import com.bejibx.android.recyclerview.selection.SelectionHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseSingleSelectionRecyclerViewAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder<I>> extends BaseRecyclerViewAdapter<I, VH> {

    @NotNull
    protected final ItemSelectedObservable mItemSelectedObservable = new ItemSelectedObservable();

    private int mSelection = RecyclerView.NO_POSITION;

    private boolean mIsSelectable = false;

    private boolean mAllowTogglingSelection = true;

    @Nullable
    private SelectionHelper.SelectMode mSelectMode = null;

    public BaseSingleSelectionRecyclerViewAdapter(@NotNull Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items) {
        this(context, itemLayoutId, items, null, null);
    }

    public BaseSingleSelectionRecyclerViewAdapter(Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items, Drawable defaultDrawable, Drawable selectionDrawable) {
        super(context, itemLayoutId, items);
    }

    public void registerItemSelectedChangeListener(@NotNull OnItemSelectedChangeListener listener) {
        mItemSelectedObservable.registerObserver(listener);
    }

    public void unregisterItemSelectedChangeListener(@NotNull OnItemSelectedChangeListener listener) {
        mItemSelectedObservable.unregisterObserver(listener);
    }

    public boolean isIsSelectable() {
        return mIsSelectable;
    }

    public boolean setIsSelectable(boolean isSelectable) {
        if (this.mIsSelectable != isSelectable) {
            this.mIsSelectable = isSelectable;
            if (!isSelectable) resetSelection();
            return true;
        }
        return false;
    }

    public boolean allowTogglingSelection() {
        return mAllowTogglingSelection;
    }

    public void setAllowTogglingSelection(boolean toggle) {
        mAllowTogglingSelection = toggle;
    }

    // override if need various for each position
    @NotNull
    public Set<SelectionHelper.SelectMode> getSelectModes(int position) {
        if (mSelectMode == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(mSelectMode);
        }
    }

    public void setSelectMode(@Nullable SelectionHelper.SelectMode selectMode) {
        mSelectMode = selectMode;
    }

    @Nullable
    protected Checkable getCheckableView(@NotNull VH holder) {
        if (holder.itemView instanceof Checkable) {
            return (Checkable) holder.itemView;
        }
        return null;
    }

    @Override
    @CallSuper
    protected void processItem(@NotNull VH holder, @Nullable I item, int position) {
        super.processItem(holder, item, position);
        processSelection(holder, item, position);
    }

    protected void processSelection(@NotNull VH holder, @Nullable final I item, final int position) {
        for (SelectionHelper.SelectMode mode : getSelectModes(position)) {
            switch (mode) {
                case CLICK:
                    holder.itemView.setClickable(true);
                    holder.itemView.setOnClickListener(v -> {
                        if (position == mSelection) {
                            if (allowTogglingSelection() || mSelection != position) {
                                toggleSelection(position, true);
                            } else {
                                // current state is selected, triggering reselect
                                setSelection(position, true);
                            }
                        } else {
                            setSelection(position, true);
                        }
                        if (allowSetClickListener(item, position)) {
                            mItemsEventsObservable.notifyItemClick(position, item);
                        }
                    });
                    break;

                case LONG_CLICK:
                    holder.itemView.setLongClickable(true);
                    holder.itemView.setOnLongClickListener(v -> {
                        if (position == mSelection) {
                            if (allowTogglingSelection() || mSelection != position) {
                                toggleSelection(position, true);
                            } else {
                                // current state is selected, triggering reselect
                                setSelection(position, true);
                            }
                        } else {
                            setSelection(position, true);
                        }
                        if (allowSetLongClickListener(item, position)) {
                             return mItemsEventsObservable.notifyItemLongClick(position, item);
                        }
                        return true;
                    });
                    break;
            }
        }
        boolean isSelected = isItemPositionSelected(position);
        Checkable checkableView = getCheckableView(holder);
        if (checkableView != null) {
            checkableView.setChecked(isSelected);
        }
        if (isSelected) {
            onProcessItemSelected(holder);
        } else {
            onProcessItemNotSelected(holder);
        }
    }

    protected void onProcessItemSelected(@NotNull VH holder) {

    }

    protected void onProcessItemNotSelected(@NotNull VH holder) {

    }

    public boolean isSelected() {
        return getSelectedPosition() != RecyclerView.NO_POSITION;
    }

    public int getSelectedPosition() {
        if (mSelection >= 0 && mSelection < getItemCount()) {
            return mSelection;
        }
        return (mSelection = RecyclerView.NO_POSITION);
    }

    @Nullable
    public I getSelectedItem() {
        int selection = getSelectedPosition();
        if (selection >= 0 && selection < getItemCount()) {
            return getItem(selection);
        }
        return null;
    }

    public boolean isItemPositionSelected(int position) {
        rangeCheck(position);
        return mSelection != RecyclerView.NO_POSITION && mSelection == position;
    }

    public boolean isItemSelected(I item) {
        return isItemPositionSelected(indexOf(item));
    }

    public void setSelectionByItem(I item) {
        setSelection(indexOf(item));
    }

    public void setSelection(int selection) {
        setSelection(selection, false);
    }

    protected void setSelection(int selection, boolean fromUser) {
        rangeCheck(selection);
        int previousSelection = mSelection;
        mSelection = selection;
        onSelectionChanged(previousSelection, mSelection, fromUser);
        if (isNotifyOnChange()) {
            boolean isNewSelection = true;
            if (previousSelection >= 0 && previousSelection < getItemCount()) {
                isNewSelection = mSelection != previousSelection;
                if (isNewSelection) {
                    notifyItemChanged(previousSelection);
                }
            }
            if (isNewSelection) {
                notifyItemChanged(selection);
            }
        }
    }

    public void resetSelection() {
        resetSelection(false);
    }

    private void resetSelection(boolean fromUser) {
        if (isSelected()) {
            int previousSelection = mSelection;
            mSelection = RecyclerView.NO_POSITION;
            if (previousSelection < 0 || previousSelection >= getItemCount()) {
                previousSelection = RecyclerView.NO_POSITION;
            }
            onSelectionChanged(previousSelection, mSelection, fromUser);
            if (isNotifyOnChange()) {
                if (previousSelection != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelection);
                }
            }
        }
    }

    public void toggleSelection(int selection) {
        toggleSelection(selection, false);
    }

    private void toggleSelection(int selection, boolean fromUser) {
        rangeCheck(selection);
        if (mSelection == selection) {
            resetSelection(fromUser);
        } else {
            setSelection(selection, fromUser);
        }
    }

    public boolean previousSelection(boolean loop) {
        boolean changed = false;
        int selection = getSelectedPosition();
        if (selection != RecyclerView.NO_POSITION) {
            if (selection >= 1) {
                setSelection(--selection);
                changed = true;
            } else if (loop) {
                setSelection(getItemCount() - 1);
                changed = true;
            }
        }
        return changed;
    }

    public boolean nextSelection(boolean loop) {
        boolean changed = false;
        int selection = getSelectedPosition();
        if (selection != RecyclerView.NO_POSITION) {
            if (selection < getItemCount() - 1) {
                setSelection(++selection);
                changed = true;
            } else if (loop) {
                setSelection(0);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * called before {@link #notifyItemChanged(int)}}
     */
    @CallSuper
    protected void onSelectionChanged(int from, int to, boolean fromUser) {
        if (to != RecyclerView.NO_POSITION) {
            if (from != to) {
                mItemSelectedObservable.notifyItemSetSelection(from, to, fromUser);
            } else {
                mItemSelectedObservable.notifyItemReselect(to, fromUser);
            }
        } else {
            mItemSelectedObservable.notifyItemResetSelection(from, fromUser);
        }
    }

    @Override
    protected void onItemAdded(int to, @Nullable I item) {
        invalidateSelectionIndexOnAdd(to, 1);
        super.onItemAdded(to, item);
    }

    @Override
    protected void onItemsAdded(int to, @NotNull Collection<I> items) {
        invalidateSelectionIndexOnAdd(to, items.size());
        super.onItemsAdded(to, items);
    }

    @Override
    protected void onItemRemoved(int from, @Nullable I item) {
        invalidateSelectionIndexOnRemove(from, 1);
        super.onItemRemoved(from, item);
    }

    @Override
    protected void onItemsRangeRemoved(int from, int to, int previousSize) {
        invalidateSelectionIndexOnRemove(from, from == to ? 1 : to - from);
        super.onItemsRangeRemoved(from, to, previousSize);
    }

    @Override
    protected void onItemsSet() {
        mSelection = RecyclerView.NO_POSITION;
        super.onItemsSet();
    }

    @Override
    public void release() {
        super.release();
        mItemSelectedObservable.unregisterAll();
    }

    protected void invalidateSelectionIndexOnAdd(int to, int count) {
        final int currentCount = getItemCount();
        if (to >= 0 && to < currentCount && count >= 1) {
            int previousSelection = RecyclerView.NO_POSITION;
            if (mSelection != RecyclerView.NO_POSITION) {
                if (mSelection >= to) {
                    previousSelection = mSelection;
                    mSelection += count;
                }
            }
            if (previousSelection >= 0 && previousSelection < currentCount) {
                notifyItemChanged(previousSelection);
            }
            if (mSelection != previousSelection && mSelection >= 0 && mSelection < currentCount) {
                notifyItemChanged(mSelection);
            }
        }
    }

    protected void invalidateSelectionIndexOnRemove(int from, int count) {
        final int currentCount = getItemCount();
        if (from >= 0 && from <= currentCount && count >= 1) {
            int previousSelection = RecyclerView.NO_POSITION;
            if (mSelection != RecyclerView.NO_POSITION) {
                if (mSelection >= from && mSelection < from + count) {
                    previousSelection = mSelection;
                } else if (mSelection >= from + count) {
                    previousSelection = mSelection;
                    mSelection -= count;
                }
            }
            if (previousSelection >= 0 && previousSelection < currentCount) {
                notifyItemChanged(previousSelection);
            }
            if (mSelection != previousSelection && mSelection >= 0 && mSelection < currentCount) {
                notifyItemChanged(mSelection);
            }
        }
    }

    protected static class ItemSelectedObservable extends Observable<OnItemSelectedChangeListener> {

        protected void notifyItemSetSelection(int fromIndex, int toIndex, boolean fromUser) {
            synchronized (mObservers) {
                for (OnItemSelectedChangeListener l : mObservers) {
                    l.onItemSetSelection(fromIndex, toIndex, fromUser);
                }
            }
        }

        protected void notifyItemResetSelection(int index, boolean fromUser) {
            synchronized (mObservers) {
                for (OnItemSelectedChangeListener l : mObservers) {
                    l.onItemResetSelection(index, fromUser);
                }
            }
        }

        protected void notifyItemReselect(int index, boolean fromUser) {
            synchronized (mObservers) {
                for (OnItemSelectedChangeListener l : mObservers) {
                    l.onItemReselect(index, fromUser);
                }
            }
        }
    }

    public interface OnItemSelectedChangeListener {

        void onItemSetSelection(int fromIndex, int toIndex, boolean fromUser);

        void onItemResetSelection(int index, boolean fromUser);

        void onItemReselect(int index, boolean fromUser);
    }
}
