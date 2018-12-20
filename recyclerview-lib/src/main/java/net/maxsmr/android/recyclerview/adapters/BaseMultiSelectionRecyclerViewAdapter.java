package net.maxsmr.android.recyclerview.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Observable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.widget.Checkable;

import com.bejibx.android.recyclerview.selection.HolderClickListener;
import com.bejibx.android.recyclerview.selection.SelectionHelper;
import com.bejibx.android.recyclerview.selection.SelectionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseMultiSelectionRecyclerViewAdapter<I, VH
        extends BaseRecyclerViewAdapter.ViewHolder> extends BaseRecyclerViewAdapter<I, VH> implements HolderClickListener, SelectionListener {

    @NotNull
    protected final ItemSelectedObservable mItemSelectedObservable = new ItemSelectedObservable();

    private SelectionHelper mSelectionHelper;

    @NotNull
    private final Set<SelectionHelper.SelectMode> mSelectModes = new LinkedHashSet<>();

    public BaseMultiSelectionRecyclerViewAdapter(@NotNull Context context, boolean isSelectable) {
        this(context, 0, null, isSelectable);
    }

    public BaseMultiSelectionRecyclerViewAdapter(@NotNull Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items) {
        this(context, itemLayoutId, items, true);
    }

    public BaseMultiSelectionRecyclerViewAdapter(@NotNull Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items, boolean selectable) {
        super(context, itemLayoutId, items);
        initSelectionHelper();
        setSelectable(selectable);
    }

    protected void initSelectionHelper() {
        if (mSelectionHelper == null) {
            mSelectionHelper = new SelectionHelper();
            mSelectionHelper.registerSelectionObserver(this);
            mSelectionHelper.registerHolderClickObserver(this);
        }
    }

    protected void releaseSelectionHelper() {
        if (mSelectionHelper != null) {
            mSelectionHelper.clear();
            mSelectionHelper.unregisterSelectionObserver(this);
            mSelectionHelper.unregisterHolderClickObserver(this);
            mSelectionHelper = null;
        }
    }

    public void registerItemSelectedChangeListener(@NotNull OnItemSelectedChangeListener listener) {
        mItemSelectedObservable.registerObserver(listener);
    }

    public void unregisterItemSelectedChangeListener(@NotNull OnItemSelectedChangeListener listener) {
        mItemSelectedObservable.unregisterObserver(listener);
    }

    public boolean isSelectable() {
        initSelectionHelper();
        return mSelectionHelper.isSelectable();
    }

    public void setSelectable(boolean toggle) {
        initSelectionHelper();
        mSelectionHelper.setSelectable(toggle);
    }


    public boolean isTogglingSelectionAllowed() {
        initSelectionHelper();
        return mSelectionHelper.isTogglingSelectionAllowed();
    }

    public void setAllowTogglingSelection(boolean toggle) {
        initSelectionHelper();
        mSelectionHelper.setAllowTogglingSelection(toggle);
    }

    @NotNull
    public Set<SelectionHelper.SelectMode> getSelectionModes() {
        return new LinkedHashSet<>(mSelectModes);
    }

    @SuppressLint("NewApi")
    public void setSelectionModes(@Nullable Set<SelectionHelper.SelectMode> selectionModes) {
        synchronized (mSelectModes) {
            if (!Objects.equals(selectionModes, mSelectModes)) {
                mSelectModes.clear();
                if (selectionModes != null) {
                    mSelectModes.addAll(selectionModes);
                }
                mItemSelectedObservable.notifySelectModesChanged(getSelectionModes());
                // notify required here
                if (isNotifyOnChange()) {
                    notifyDataSetChanged();
                }
            }
        }
    }

    protected void processSelection(@NotNull VH holder, @Nullable I item, int position) {
        mSelectionHelper.wrapSelectable(holder, mSelectModes); /* mSelectModes.get(position) */

        final boolean isSelected = isItemPositionSelected(position);
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

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        mSelectionHelper.recycleHolder(holder);
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

    @Override
    protected boolean allowSetClickListener(@Nullable I item, int position) {
        return super.allowSetClickListener(item, position) && !mSelectModes.contains(SelectionHelper.SelectMode.CLICK);
    }

    @Override
    protected boolean allowSetLongClickListener(@Nullable I item, int position) {
        return super.allowSetLongClickListener(item, position) && !mSelectModes.contains(SelectionHelper.SelectMode.LONG_CLICK);
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
        clearSelection();
        super.onItemsSet();
    }

    @NotNull
    public List<I> getSelectedItems() {
        List<I> selectedItems = new ArrayList<>();
        Set<Integer> selectedPositions = getSelectedItemsPositions();
        for (Integer pos : selectedPositions) {
            selectedItems.add(getItem(pos));
        }
        return selectedItems;
    }

    @NotNull
    public Set<I> getUnselectedItems() {
        Set<I> unselectedItems = new LinkedHashSet<>();
        Set<Integer> unselectedPositions = getUnselectedItemsPositions();
        for (Integer pos : unselectedPositions) {
            unselectedItems.add(getItem(pos));
        }
        return unselectedItems;
    }

    @NotNull
    public Set<Integer> getSelectedItemsPositions() {
        initSelectionHelper();
        return mSelectionHelper != null ? mSelectionHelper.getSelectedItems() : Collections.emptySet();
    }

    @NotNull
    public Set<Integer> getUnselectedItemsPositions() {
        LinkedHashSet<Integer> unselectedPositions = new LinkedHashSet<>();
        Set<Integer> selectedPositions = getSelectedItemsPositions();
        for (int pos = 0; pos < getItemCount(); pos++) {
            if (!selectedPositions.contains(pos)) {
                unselectedPositions.add(pos);
            }
        }
        return unselectedPositions;
    }

    public boolean isItemPositionSelected(int position) {
        initSelectionHelper();
        rangeCheck(position);
        return mSelectionHelper.isItemSelected(position);
    }

    public boolean isItemSelected(I item) {
        return isItemPositionSelected(indexOf(item));
    }

    public int getSelectedItemsCount() {
        initSelectionHelper();
        return getItemCount() > 0 ? mSelectionHelper != null ? mSelectionHelper.getSelectedItemsCount() : 0 : 0;
    }

    public boolean setItemsSelectedByPositions(@Nullable Collection<Integer> positions, boolean isSelected) {
        return setItemsSelectedByPositions(positions, isSelected, true);
    }

    private boolean setItemsSelectedByPositions(@Nullable Collection<Integer> positions, boolean isSelected, boolean needRangeCheck) {
        initSelectionHelper();
        if (positions != null) {
            for (int pos : positions) {
                if (needRangeCheck || isSelected) {
                    rangeCheck(pos);
                }
            }
        }
        return mSelectionHelper != null && mSelectionHelper.setItemsSelectedByPositions(positions, isSelected, false);
    }

    public boolean setItemSelectedByPosition(int position, boolean isSelected) {
        initSelectionHelper();
        rangeCheck(position);
        return mSelectionHelper != null && mSelectionHelper.setItemSelectedByPosition(position, isSelected, false);
    }

    public boolean toggleItemsSelectedByPositions(@Nullable Collection<Integer> positions) {
        initSelectionHelper();
        if (positions != null) {
            for (int pos : positions) {
                rangeCheck(pos);
            }
        }
        return mSelectionHelper != null && mSelectionHelper.toggleItemsSelectedByPositions(positions, false);
    }

    public boolean toggleItemSelectedByPosition(int position) {
//        if (mSelectionHelper == null) {
//            throw new IllegalStateException(SelectionHelper.class.getSimpleName() + " was not initialized");
//        }
        rangeCheck(position);
        return mSelectionHelper != null && mSelectionHelper.toggleItemSelectedByPosition(position, false);
    }

    public boolean setItemsSelected(@Nullable Collection<I> items, boolean isSelected) {
        List<Integer> positions = new ArrayList<>();
        if (items != null) {
            for (I item : items) {
                int index = indexOf(item);
                if (index > -1) {
                    positions.add(index);
                }
            }
        }
        return setItemsSelectedByPositions(positions, isSelected);
    }

    public boolean setItemSelected(I item, boolean isSelected) {
        return setItemsSelected(Collections.singletonList(item), isSelected);
    }

    public boolean toggleItemsSelected(Collection<I> items) {
        List<Integer> positions = new ArrayList<>();
        if (items != null) {
            for (I item : items) {
                int index = indexOf(item);
                if (index > -1) {
                    positions.add(index);
                }
            }
        }
        return toggleItemsSelectedByPositions(positions);
    }

    public boolean toggleItemSelected(I item) {
        return toggleItemsSelected(Collections.singletonList(item));
    }

    public void clearSelection() {
        initSelectionHelper();
        if (mSelectionHelper != null) {
            if (mSelectionHelper.getSelectedItemsCount() > 0) {
                mSelectionHelper.clearSelection(false);
            }
        }
    }

    protected void onProcessItemSelected(@NotNull VH holder) {

    }

    protected void onProcessItemNotSelected(@NotNull VH holder) {

    }

    @SuppressWarnings("unchecked")
    @Override
    @CallSuper
    public void onSelectedChanged(RecyclerView.ViewHolder holder, boolean isSelected, boolean fromUser) {
        final int position = holder.getAdapterPosition();
        if (position >= 0 && position < getItemCount()) {
            mItemSelectedObservable.notifyItemSelected(position, isSelected, fromUser);
            if (isNotifyOnChange())
                notifyItemChanged(position);
        }
    }

    @Override
    @CallSuper
    public void onReselected(RecyclerView.ViewHolder holder, boolean fromUser) {
        final int position = holder.getAdapterPosition();
        if (position >= 0 && position < getItemCount()) {
            mItemSelectedObservable.notifyItemReselected(position, fromUser);
        }
    }

    @Override
    @CallSuper
    public void onSelectableChanged(boolean isSelectable) {
        // call notify if it needed due to ViewHolder logic or something
        mItemSelectedObservable.notifySelectableChanged(isSelectable);
    }

    @Override
    @CallSuper
    public void onAllowTogglingSelectionChanged(boolean isAllowed) {
        // call notify if it needed due to ViewHolder logic or something
        mItemSelectedObservable.notifyAllowTogglingSelectionChanged(isAllowed);
    }

    @Override
    public void onHolderClick(RecyclerView.ViewHolder holder) {

    }

    @Override
    public boolean onHolderLongClick(RecyclerView.ViewHolder holder) {
        return false;
    }

    @Override
    @CallSuper
    public void release() {
        super.release();
        releaseSelectionHelper();
        mItemSelectedObservable.unregisterAll();
    }

    protected void invalidateSelections() {
        Set<Integer> targetUnselected = new LinkedHashSet<>();
        for (Integer selection : getSelectedItemsPositions()) {
            if (selection != null && selection != RecyclerView.NO_POSITION && !(selection >= 0 && selection < getItemCount())) {
                targetUnselected.add(selection);
            }
        }
        setItemsSelectedByPositions(targetUnselected, false, false);
    }

    protected void invalidateSelectionIndexOnAdd(int to, int count) {
        Set<Integer> targetSelected = new LinkedHashSet<>();
        Set<Integer> targetUnselected = new LinkedHashSet<>();
        if (count >= 1) {
            for (Integer selection : getSelectedItemsPositions()) {
                if (selection != null && selection != RecyclerView.NO_POSITION && selection >= to) {
                    targetUnselected.add(selection);
                    selection += count;
                    if (selection >= 0 && selection < getItemCount()) {
                        targetSelected.add(selection);
                    }
                }
            }
        }
        Iterator<Integer> it = targetUnselected.iterator();
        while (it.hasNext()) {
            Integer index = it.next();
            // unselect old if it's not clashes with target selected
            if (targetSelected.contains(index)) {
                it.remove();
            }
        }
        setItemsSelectedByPositions(targetUnselected, false, false);
        setItemsSelectedByPositions(targetSelected, true);
    }

    protected void invalidateSelectionIndexOnRemove(int from, int count) {
        if (count >= 1) {
            Set<Integer> targetSelected = new LinkedHashSet<>();
            Set<Integer> targetUnselected = new LinkedHashSet<>();
            for (Integer selection : getSelectedItemsPositions()) {
                if (selection != null && selection != RecyclerView.NO_POSITION) {
                    if (selection >= from && selection < from + count) {
                        if (selection >= 0 && selection < getItemCount()) {
                            targetUnselected.add(selection);
                        }
                    } else if (selection >= from + count) {
                        targetUnselected.add(selection);
                        selection -= count;
                        if (selection >= 0 && selection < getItemCount()) {
                            targetSelected.add(selection);
                        }
                    }
                }
            }
            Iterator<Integer> it = targetUnselected.iterator();
            while (it.hasNext()) {
                Integer index = it.next();
                // unselect old if it's not clashes with target selected
                if (targetSelected.contains(index)) {
                    it.remove();
                }
            }
            setItemsSelectedByPositions(targetUnselected, false, false);
            setItemsSelectedByPositions(targetSelected, true);
        }
    }

    protected static class ItemSelectedObservable extends Observable<OnItemSelectedChangeListener> {

        protected void notifyItemSelected(int position, boolean isSelected, boolean fromUser) {
            synchronized (mObservers) {
                for (OnItemSelectedChangeListener l : mObservers) {
                    l.onItemSelected(position, isSelected, fromUser);
                }
            }
        }

        protected void notifyItemReselected(int position, boolean fromUser) {
            synchronized (mObservers) {
                for (OnItemSelectedChangeListener l : mObservers) {
                    l.onItemReselected(position, fromUser);
                }
            }
        }

        protected void notifySelectableChanged(boolean isSelectable) {
            synchronized (mObservers) {
                for (OnItemSelectedChangeListener l : mObservers) {
                    l.onSelectableChanged(isSelectable);
                }
            }
        }

        protected void notifyAllowTogglingSelectionChanged(boolean isAllowed) {
            synchronized (mObservers) {
                for (OnItemSelectedChangeListener l : mObservers) {
                    l.onAllowTogglingSelectionChanged(isAllowed);
                }
            }
        }

        protected void notifySelectModesChanged(@NotNull Set<SelectionHelper.SelectMode> selectModes) {
            synchronized (mObservers) {
                for (OnItemSelectedChangeListener l : mObservers) {
                    l.onSelectModesChanged(selectModes);
                }
            }
        }
    }

    public interface OnItemSelectedChangeListener {

        void onItemSelected(int position, boolean isSelected, boolean fromUser);

        void onItemReselected(int position, boolean fromUser);

        void onSelectableChanged(boolean isSelectable);

        void onAllowTogglingSelectionChanged(boolean isAllowed);

        void onSelectModesChanged(@NotNull Set<SelectionHelper.SelectMode> selectModes);
    }
}
