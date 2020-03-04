package com.bejibx.android.recyclerview.selection;

import android.database.Observable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class SelectionHelper {

    private final HolderWrapperTracker tracker = new HolderWrapperTracker();
    private final LinkedHashSet<Integer> selectedItems = new LinkedHashSet<>();

    private final HolderClickObservable holderClickObservable = new HolderClickObservable();
    private final SelectionObservable selectionObservable = new SelectionObservable();

    private boolean isSelectable = true;

    private boolean allowResetSelection = true;

    public boolean isSelectable() {
        return isSelectable;
    }

    /**
     * @return true if changed
     */
    public boolean setSelectable(boolean isSelectable) {
        if (this.isSelectable != isSelectable) {
            this.isSelectable = isSelectable;
            if (!isSelectable) clearSelection(false);
            selectionObservable.notifySelectableChanged(isSelectable);
            return true;
        }
        return false;
    }

    public boolean isResetSelectionAllowed() {
        return allowResetSelection;
    }

    /**
     * @return true if changed
     */
    public boolean setAllowResetSelection(boolean toggle) {
        if (this.allowResetSelection != toggle) {
            this.allowResetSelection = toggle;
            selectionObservable.notifyAllowResetSelectionChanged(toggle);
            return true;
        }
        return false;
    }

    public <H extends RecyclerView.ViewHolder> H wrapSelectable(
            @NotNull H holder,
            @Nullable View clickableView,
            @Nullable View longClickableView,
            @Nullable View selectableView,
            @NotNull Set<SelectTriggerMode> selectTriggerModes
    ) {
        bindWrapper(new ViewHolderMultiSelectionWrapper(holder,
                clickableView,
                longClickableView,
                selectableView,
                selectTriggerModes)
        );
        return holder;
    }

    public <H extends RecyclerView.ViewHolder> H wrapClickable(
            @NotNull H holder,
            @Nullable View clickableView,
            @Nullable View longClickableView
    ) {
        bindWrapper(new ViewHolderClickWrapper(holder, clickableView, longClickableView));
        return holder;
    }

    public <H extends RecyclerView.ViewHolder> void recycleHolder(@NotNull H holder) {
        tracker.recycleWrapper(holder.getAdapterPosition());
    }

    private <W extends SelectionHelper.ViewHolderWrapper> void bindWrapper(@NotNull W wrapper) {
        RecyclerView.ViewHolder holder = wrapper.getHolder();
        if (holder != null)
            tracker.bindWrapper(wrapper, holder.getAdapterPosition());
    }

    public boolean setItemSelectedByHolder(@NotNull RecyclerView.ViewHolder holder, boolean isSelected, boolean fromUser) {
        return setItemSelectedByPosition(holder.getAdapterPosition(), isSelected, fromUser);
    }

    public boolean setItemsSelectedByHolders(@Nullable Collection<RecyclerView.ViewHolder> holders, boolean isSelected, boolean fromUser) {
        boolean success = false;
        if (holders != null) {
            success = true;
            for (RecyclerView.ViewHolder holder : holders) {
                if (holder == null || !setItemSelectedByHolder(holder, isSelected, fromUser)) {
                    success = false;
                }
            }
        }
        return success;
    }

    public boolean toggleItemSelectedByHolder(@NotNull RecyclerView.ViewHolder holder, boolean fromUser) {
        return setItemSelectedByHolder(holder, !isItemSelected(holder), fromUser);
    }

    public boolean toggleItemsSelectedByHolders(@Nullable Collection<RecyclerView.ViewHolder> holders, boolean fromUser) {
        boolean success = true;
        if (holders != null) {
            for (RecyclerView.ViewHolder holder : holders) {
                if (!toggleItemSelectedByHolder(holder, fromUser)) {
                    success = false;
                }
            }
        }
        return success;
    }

    public boolean setItemSelectedByPosition(int position, boolean isSelected, boolean fromUser) {
        if (isSelectable() && position != RecyclerView.NO_POSITION) {
            boolean isAlreadySelected = isItemSelected(position);
            if (isSelected) {
                selectedItems.add(position);
            } else {
                selectedItems.remove(position);
            }
            ViewHolderWrapper wrapper = tracker.getWrapper(position);
            if (wrapper != null) {
                RecyclerView.ViewHolder holder = wrapper.getHolder();
                if (holder != null) {
                    if (isSelected ^ isAlreadySelected) {
                        selectionObservable.notifySelectionChanged(holder, isSelected, fromUser);
                    } else if (isSelected) {
                        selectionObservable.notifyReselected(holder, fromUser);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean setItemsSelectedByPositions(@Nullable Collection<Integer> positions, boolean isSelected, boolean fromUser) {
        boolean success = false;
        if (positions != null) {
            success = true;
            for (int pos : positions) {
                if (!setItemSelectedByPosition(pos, isSelected, fromUser)) {
                    success = false;
                }
            }
        }
        return success;
    }

    public boolean toggleItemSelectedByPosition(int position, boolean fromUser) {
        return setItemSelectedByPosition(position, !isItemSelected(position), fromUser);
    }

    public boolean toggleItemsSelectedByPositions(@Nullable Collection<Integer> positions, boolean fromUser) {
        boolean success = true;
        if (positions != null) {
            for (int pos : positions) {
                if (!toggleItemSelectedByPosition(pos, fromUser)) {
                    success = false;
                }
            }
        }
        return success;
    }

    public void clearSelection(boolean fromUser) {
        if (!selectedItems.isEmpty()) {
            selectedItems.clear();
            for (ViewHolderWrapper wrapper : tracker.getTrackedWrappers()) {
                if (wrapper != null) {
                    final RecyclerView.ViewHolder holder = wrapper.getHolder();
                    if (holder != null) {
                        selectionObservable.notifySelectionChanged(holder, false, fromUser);
                    }
                }
            }
        }
    }

    public LinkedHashSet<Integer> getSelectedItems() {
        return new LinkedHashSet<>(selectedItems);
    }

    public boolean isItemSelected(RecyclerView.ViewHolder holder) {
        return selectedItems.contains(holder.getAdapterPosition());
    }

    public boolean isItemSelected(int position) {
        return selectedItems.contains(position);
    }

    public int getSelectedItemsCount() {
        return selectedItems.size();
    }

    public void clear() {
        tracker.clear();
        selectedItems.clear();
    }

    public final void registerHolderClickObserver(@NotNull HolderClickListener observer) {
        holderClickObservable.registerObserver(observer);
    }

    public final void unregisterSelectionObserver(@NotNull SelectionListener observer) {
        selectionObservable.unregisterObserver(observer);
    }

    public final void registerSelectionObserver(@NotNull SelectionListener observer) {
        selectionObservable.registerObserver(observer);
    }

    public final void unregisterHolderClickObserver(@NotNull HolderClickListener observer) {
        holderClickObservable.unregisterObserver(observer);
    }

    public enum SelectTriggerMode {
        CLICK, LONG_CLICK
    }

    abstract class ViewHolderWrapper implements View.OnClickListener, View.OnLongClickListener {

        protected final WeakReference<RecyclerView.ViewHolder> mWrappedHolderRef;

        @Nullable
        final View clickableView;
        @Nullable
        final View longClickableView;

        protected ViewHolderWrapper(RecyclerView.ViewHolder holder, @Nullable View clickableView, @Nullable View longClickableView) {
            mWrappedHolderRef = new WeakReference<>(holder);
            this.clickableView = clickableView;
            this.longClickableView = longClickableView;
            if (shouldSetClickListener() && clickableView != null) {
                clickableView.setOnClickListener(this);
            }
            if (shouldSetLongClickListener() && longClickableView != null) {
                longClickableView.setOnClickListener(this);
            }
        }

        @Nullable
        public RecyclerView.ViewHolder getHolder() {
            return mWrappedHolderRef.get();
        }

        abstract boolean shouldSetClickListener();

        abstract boolean shouldSetLongClickListener();
    }

    class ViewHolderMultiSelectionWrapper extends ViewHolderWrapper
            implements View.OnLongClickListener {

        @Nullable
        private final View selectableView;

        @NotNull
        private final Set<SelectTriggerMode> selectTriggerModes;

        @NotNull
        public Set<SelectTriggerMode> getSelectModes() {
            return new LinkedHashSet<>(selectTriggerModes);
        }

        ViewHolderMultiSelectionWrapper(
                RecyclerView.ViewHolder holder,
                @Nullable View clickableView,
                @Nullable View longClickableView,
                @Nullable View selectableView,
                @NotNull Set<SelectTriggerMode> selectTriggerModes) {
            super(holder, clickableView, longClickableView);

            this.selectableView = selectableView;
            this.selectTriggerModes = selectTriggerModes;
        }

        @Override
        boolean shouldSetClickListener() {
            return selectTriggerModes.contains(SelectTriggerMode.CLICK);
        }

        @Override
        boolean shouldSetLongClickListener() {
            return selectTriggerModes.contains(SelectTriggerMode.LONG_CLICK);
        }

        @Override
        public final void onClick(View v) {
            RecyclerView.ViewHolder holder = mWrappedHolderRef.get();
            if (holder != null) {
                changeSelectedStateFromUiNotify(holder, () -> holderClickObservable.notifyOnHolderClick(holder));
            }
        }

        @Override
        public final boolean onLongClick(View v) {
            RecyclerView.ViewHolder holder = mWrappedHolderRef.get();
            if (holder != null) {
                changeSelectedStateFromUiNotify(holder, () -> holderClickObservable.notifyOnHolderLongClick(holder));
            }
            return true;
        }

        private void changeSelectedStateFromUiNotify(
                @NotNull RecyclerView.ViewHolder holder,
                @Nullable Runnable nonSelectableAction
        ) {
            final boolean wasSelected = isItemSelected(holder.getAdapterPosition());
            if (!changeSelectedStateFromUi(holder, nonSelectableAction)) {
                if (selectableView != null) {
                    selectionObservable.notifyHandleSelected(selectableView, wasSelected);
                }
            }
        }

        private boolean changeSelectedStateFromUi(@NotNull RecyclerView.ViewHolder holder, @Nullable Runnable nonSelectableAction) {
            if (isSelectable()) {
                if (isItemSelected(holder)) {
                    if (isResetSelectionAllowed()) {
                        return setItemSelectedByHolder(holder, false, true);
                    } else {
                        // current state is selected, triggering reselect, state must be not changed
                        setItemSelectedByHolder(holder, true, true);
                        return false;
                    }
                } else {
                    return toggleItemSelectedByHolder(holder, true);
                }
            } else {
                if (nonSelectableAction != null) {
                    nonSelectableAction.run();
                }
            }
            return false;
        }
    }

    class ViewHolderClickWrapper extends ViewHolderWrapper {

        ViewHolderClickWrapper(
                RecyclerView.ViewHolder holder,
                @Nullable View clickableView,
                @Nullable View longClickableView
        ) {
            super(holder, clickableView, longClickableView);
        }

        @Override
        boolean shouldSetClickListener() {
            return true;
        }

        @Override
        boolean shouldSetLongClickListener() {
            return true;
        }

        @Override
        public final void onClick(View v) {
            RecyclerView.ViewHolder holder = mWrappedHolderRef.get();
            if (holder != null) {
                holderClickObservable.notifyOnHolderClick(holder);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            RecyclerView.ViewHolder holder = mWrappedHolderRef.get();
            if (holder != null) {
                return holderClickObservable.notifyOnHolderLongClick(holder);
            }
            return false;
        }
    }

    private class HolderClickObservable extends Observable<HolderClickListener> {

        public final void notifyOnHolderClick(RecyclerView.ViewHolder holder) {
            synchronized (mObservers) {
                for (HolderClickListener observer : mObservers) {
                    observer.onHolderClick(holder);
                }
            }
        }

        public final boolean notifyOnHolderLongClick(RecyclerView.ViewHolder holder) {
            boolean isConsumed = false;
            synchronized (mObservers) {
                for (HolderClickListener observer : mObservers) {
                    isConsumed = isConsumed || observer.onHolderLongClick(holder);
                }
            }
            return isConsumed;
        }
    }

    private class SelectionObservable extends Observable<SelectionListener> {

        private void notifySelectionChanged(@NotNull RecyclerView.ViewHolder holder, boolean isSelected, boolean fromUser) {
            synchronized (mObservers) {
                for (SelectionListener observer : mObservers) {
                    observer.onSelectedChanged(holder, isSelected, fromUser);
                }
            }
        }

        private void notifyReselected(@NotNull RecyclerView.ViewHolder holder, boolean fromUser) {
            synchronized (mObservers) {
                for (SelectionListener observer : mObservers) {
                    observer.onReselected(holder, fromUser);
                }
            }
        }

        private void notifySelectableChanged(boolean isSelectable) {
            synchronized (mObservers) {
                for (SelectionListener observer : mObservers) {
                    observer.onSelectableChanged(isSelectable);
                }
            }
        }

        private void notifyAllowResetSelectionChanged(boolean isAllowed) {
            synchronized (mObservers) {
                for (SelectionListener observer : mObservers) {
                    observer.onAllowResetSelectionChanged(isAllowed);
                }
            }
        }

        private void notifyHandleSelected(@NotNull View selectableView, boolean isSelected) {
            synchronized (mObservers) {
                for (SelectionListener observer : mObservers) {
                    observer.handleSelected(selectableView, isSelected);
                }
            }
        }
    }
}
