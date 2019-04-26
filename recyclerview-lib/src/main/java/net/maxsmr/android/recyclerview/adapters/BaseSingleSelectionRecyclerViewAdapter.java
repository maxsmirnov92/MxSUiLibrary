package net.maxsmr.android.recyclerview.adapters;

import android.content.Context;
import android.database.Observable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Checkable;

import com.bejibx.android.recyclerview.selection.SelectionHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseSingleSelectionRecyclerViewAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder<I>> extends BaseRecyclerViewAdapter<I, VH> {

    @NotNull
    protected final ItemSelectedObservable mItemSelectedObservable = new ItemSelectedObservable();

    private int mSelection = RecyclerView.NO_POSITION;

    private boolean mIsSelectable = false;

    private boolean mAllowResettingSelection = true;

    @Nullable
    private SelectionHelper.SelectMode mSelectMode = null;

    public BaseSingleSelectionRecyclerViewAdapter(@NotNull Context context, boolean isSelectable) {
        this(context, 0, null, isSelectable);
    }

    public BaseSingleSelectionRecyclerViewAdapter(@NotNull Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items) {
        this(context, itemLayoutId, items, true);
    }

    public BaseSingleSelectionRecyclerViewAdapter(Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items, boolean isSelectable) {
        super(context, itemLayoutId, items);
        setSelectable(isSelectable);
    }

    public void registerItemSelectedChangeListener(@NotNull OnItemSelectedChangeListener listener) {
        mItemSelectedObservable.registerObserver(listener);
    }

    public void unregisterItemSelectedChangeListener(@NotNull OnItemSelectedChangeListener listener) {
        mItemSelectedObservable.unregisterObserver(listener);
    }

    public boolean isSelectable() {
        return mIsSelectable;
    }

    public boolean setSelectable(boolean isSelectable) {
        if (mIsSelectable != isSelectable) {
            mIsSelectable = isSelectable;
            if (!isSelectable) resetSelection();
            return true;
        }
        return false;
    }

    public boolean allowResettingSelection() {
        return mAllowResettingSelection;
    }

    public void setAllowResettingSelection(boolean toggle) {
        mAllowResettingSelection = toggle;
    }

    // override if need various for each position
    @NotNull
    public Set<SelectionHelper.SelectMode> getSelectModes(@Nullable final I item, int position) {
        if (mSelectMode == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(mSelectMode);
        }
    }

    public void setSelectMode(@Nullable SelectionHelper.SelectMode selectMode) {
        mSelectMode = selectMode;
    }

    @Override
    protected boolean allowSetClickListener(@Nullable I item, int position) {
        return super.allowSetClickListener(item, position) && !getSelectModes(item, position).contains(SelectionHelper.SelectMode.CLICK);
    }

    @Override
    protected boolean allowSetLongClickListener(@Nullable I item, int position) {
        return super.allowSetLongClickListener(item, position) && !getSelectModes(item, position).contains(SelectionHelper.SelectMode.LONG_CLICK);
    }

    @Nullable
    protected View getSelectableView(@NotNull VH holder) {
        return getClickableView(holder);
    }

    @Override
    @CallSuper
    protected void processItem(@NotNull VH holder, @Nullable I item, int position) {
        super.processItem(holder, item, position);
        processSelection(holder, item, position);
    }

    protected void processSelection(@NotNull VH holder, @Nullable final I item, final int position) {

        boolean isSelected = isItemPositionSelected(position);
        final View selectableView = getSelectableView(holder);
        final View clickableView = getClickableView(holder);

        if (clickableView != null) {
            final Set<SelectionHelper.SelectMode> selectModes = getSelectModes(item, position);
            clickableView.setOnClickListener(v -> {
                if (selectModes.contains(SelectionHelper.SelectMode.CLICK)) {
                    changeSelectedStateFromUiNotifify(position, isSelected, selectableView);
                    if (allowSetClickListener(item, position)) {
                        mItemsEventsObservable.notifyItemClick(position, item);
                    }
                }

            });
            clickableView.setOnLongClickListener(v -> {
                if (selectModes.contains(SelectionHelper.SelectMode.LONG_CLICK)) {
                    changeSelectedStateFromUiNotifify(position, isSelected, selectableView);
                    if (allowSetLongClickListener(item, position)) {
                        return mItemsEventsObservable.notifyItemLongClick(position, item);
                    }
                }
                return false;
            });
        }

        handleSelected(selectableView, isSelected);
        if (isSelected) {
            onProcessItemSelected(holder, item, position);
        } else {
            onProcessItemNotSelected(holder, item, position);
        }
    }

    protected void handleSelected(@Nullable View selectableView, boolean isSelected) {
        if (selectableView != null) {
            if (selectableView instanceof Checkable) {
                final Checkable checkableSelectableView = (Checkable) selectableView;
                checkableSelectableView.setChecked(isSelected);
            } else {
                selectableView.setSelected(isSelected);
            }
        }
    }

    protected boolean changeSelectedStateFromUi(int position) {
        if (isSelectable()) {
            if (position == mSelection) {
                if (allowResettingSelection()) {
                    resetSelection(true);
                } else {
                    // current state is selected, triggering reselect, state must be not changed
                    setSelection(position, true);
                    return false;
                }
            } else {
                setSelection(position, true);
            }
            return true;
        }
        return false;
    }

    protected void changeSelectedStateFromUiNotifify(int position,
                                                     boolean wasSelected,
                                                     View selectableView) {
        if (!changeSelectedStateFromUi(position)) {
            handleSelected(selectableView, wasSelected);
        }
    }

    protected void onProcessItemSelected(@NotNull VH holder, @Nullable final I item, final int position) {

    }

    protected void onProcessItemNotSelected(@NotNull VH holder, @Nullable final I item, final int position) {

    }

    public boolean isSelected() {
        return getSelectedPosition() != RecyclerView.NO_POSITION;
    }

    public int getSelectedPosition() {
        if (mSelection >= 0 && mSelection < getItemCount()) {
            return mSelection;
        }
        return mSelection = RecyclerView.NO_POSITION;
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

    /**
     * @return true if selection changed, false - otherwise
     */
    public boolean setSelection(int selection, boolean fromUser) {
        if (isSelectable()) {
            rangeCheck(selection);
            int previousSelection = mSelection;
            mSelection = selection;
            boolean isNewSelection = true;
            if (previousSelection >= 0 && previousSelection < getItemCount()) {
                isNewSelection = mSelection != previousSelection;
            } else {
                previousSelection = RecyclerView.NO_POSITION;
            }
            // calling it anyway to trigger reselect if needed
            onSelectionChanged(previousSelection, mSelection, fromUser);
            if (isNewSelection) {
                if (isNotifyOnChange()) {
                    notifyItemChanged(selection);
                    if (previousSelection != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelection);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void resetSelection() {
        resetSelection(false);
    }

    /**
     * @return true if was resetted, false - it was already not selected
     */
    public boolean resetSelection(boolean fromUser) {
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
            return true;
        }
        return false;
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
            if (isNotifyOnChange()) {
                if (previousSelection >= 0 && previousSelection < currentCount) {
                    notifyItemChanged(previousSelection);
                }
                if (mSelection != previousSelection && mSelection >= 0 && mSelection < currentCount) {
                    notifyItemChanged(mSelection);
                }
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
            if (isNotifyOnChange()) {
                if (previousSelection >= 0 && previousSelection < currentCount) {
                    notifyItemChanged(previousSelection);
                }
                if (mSelection != previousSelection && mSelection >= 0 && mSelection < currentCount) {
                    notifyItemChanged(mSelection);
                }
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
