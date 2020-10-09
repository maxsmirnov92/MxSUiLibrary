package net.maxsmr.android.recyclerview.adapters.base.selection.multi;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import net.maxsmr.android.recyclerview.adapters.base.BaseRecyclerViewAdapter;
import net.maxsmr.android.recyclerview.adapters.base.selection.BaseSelectionRecyclerViewAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

final class SelectionHelper {

    private final HolderWrapperTracker tracker = new HolderWrapperTracker();
    private final LinkedHashSet<Integer> selectedItems = new LinkedHashSet<>();

    @NotNull
    private final HolderClickListener holderClickListener;
    @NotNull
    private final SelectionListener selectionListener;

    private boolean isSelectable = true;

    private boolean allowResetSelection = true;

    public SelectionHelper(@NotNull HolderClickListener holderClickListener, @NotNull SelectionListener selectionListener) {
        this.holderClickListener = holderClickListener;
        this.selectionListener = selectionListener;
    }

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
            selectionListener.onSelectableChanged(isSelectable);
            return true;
        }
        return false;
    }

    public boolean canSelectAtPosition(int position) {
        return isSelectable() && selectionListener.canSelectAtPosition(position);
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
            selectionListener.onAllowResetSelectionChanged(toggle);
            return true;
        }
        return false;
    }

    public <VH extends BaseSelectionRecyclerViewAdapter.BaseSelectableViewHolder<?>> VH wrapSelectable(
            @NotNull VH holder,
            boolean shouldSetClickListener,
            boolean shouldSetLongClickListener
    ) {
        bindWrapper(new ViewHolderMultiSelectionWrapper<>(holder,
                shouldSetClickListener,
                shouldSetLongClickListener));
        return holder;
    }

    public <VH extends BaseRecyclerViewAdapter.ViewHolder<?>> VH wrapClickable(@NotNull VH holder) {
        bindWrapper(new ViewHolderClickWrapper<>(holder));
        return holder;
    }

    public <H extends RecyclerView.ViewHolder> void recycleHolder(@NotNull H holder) {
        tracker.recycleWrapper(holder.getAdapterPosition());
    }

    private <W extends SelectionHelper.ViewHolderWrapper<?>> void bindWrapper(@NotNull W wrapper) {
        RecyclerView.ViewHolder holder = wrapper.getHolder();
        if (holder != null) {
            tracker.bindWrapper(wrapper, holder.getAdapterPosition());
        }
    }

    public boolean resetItemSelectedByPosition(
            int position,
            int wrapperPosition,
            boolean fromUser
    ) {
        return setItemSelectedByPosition(position, wrapperPosition, false, fromUser);
    }

    public boolean setItemSelectedByPosition(
            int position,
            int wrapperPosition,
            boolean isSelected,
            boolean fromUser
    ) {
        if (canSelectAtPosition(position) && position != NO_POSITION) {
            boolean isAlreadySelected = isItemSelected(position);
            if (isSelected) {
                selectedItems.add(position);
            } else {
                selectedItems.remove(position);
            }
            BaseSelectionRecyclerViewAdapter.ViewHolder<?> holder = null;
            ViewHolderWrapper<?> wrapper = wrapperPosition != NO_POSITION ? tracker.getWrapper(wrapperPosition) : null;
            if (wrapper != null) {
                holder = wrapper.getHolder();
            }
            if (isSelected ^ isAlreadySelected) {
                selectionListener.onSelectionChanged(position, holder, isSelected, fromUser);
            } else if (isSelected) {
                selectionListener.onReselected(position, holder, fromUser);
            }
            return true;
        }
        return false;
    }

    public boolean setItemsSelectedByPositions(
            @Nullable Collection<Integer> positions,
            boolean isSelected,
            boolean fromUser
    ) {
        boolean success = false;
        if (positions != null && !positions.isEmpty()) {
            success = true;
            for (int pos : positions) {
                if (!setItemSelectedByPosition(pos, NO_POSITION, isSelected, fromUser)) {
                    success = false;
                }
            }
        }
        return success;
    }

    public boolean toggleItemSelectedByPosition(int position, int wrapperPosition, boolean fromUser) {
        return setItemSelectedByPosition(position, wrapperPosition, !isItemSelected(position), fromUser);
    }

    public boolean toggleItemsSelectedByPositions(@Nullable Collection<Integer> positions, boolean fromUser) {
        boolean success = true;
        if (positions != null) {
            for (int pos : positions) {
                if (!toggleItemSelectedByPosition(pos, NO_POSITION, fromUser)) {
                    success = false;
                }
            }
        }
        return success;
    }

    public void clearSelection(boolean fromUser) {
        if (!selectedItems.isEmpty()) {
            final Set<Integer> selectedCopy = new LinkedHashSet<>(selectedItems);
            selectedItems.clear();
            for (Integer position : selectedCopy) {
                selectionListener.onSelectionChanged(position, null, false, fromUser);
            }
        }
        tracker.clear();
    }

    public LinkedHashSet<Integer> getSelectedItems() {
        return new LinkedHashSet<>(selectedItems);
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

    abstract static class ViewHolderWrapper<VH extends BaseRecyclerViewAdapter.ViewHolder<?>> implements View.OnClickListener, View.OnLongClickListener {

        @NotNull
        final WeakReference<VH> mWrappedHolderRef;

        @Nullable
        final View clickableView;
        @Nullable
        final View longClickableView;

        ViewHolderWrapper(@NotNull VH holder) {
            this.mWrappedHolderRef = new WeakReference<>(holder);
            this.clickableView = holder.getClickableView();
            this.longClickableView = holder.getLongClickableView();
        }

        @Nullable
        public final VH getHolder() {
            return mWrappedHolderRef.get();
        }

        protected boolean shouldSetClickListener() {
            return clickableView != null;
        }

        protected boolean shouldSetLongClickListener() {
            return longClickableView != null;
        }

        void initListeners(@NotNull VH holder) {
            if (shouldSetClickListener() && clickableView != null) {
                holder.setClickListener(this);
                clickableView.setOnClickListener(this);
            }
            if (shouldSetLongClickListener() && longClickableView != null) {
                holder.setLongClickListener(this);
                longClickableView.setOnClickListener(this);
            }
        }
    }

    class ViewHolderMultiSelectionWrapper<VH extends BaseSelectionRecyclerViewAdapter.BaseSelectableViewHolder<?>> extends ViewHolderWrapper<VH> {

        private final boolean shouldSetClickListener;

        private final boolean shouldSetLongClickListener;

        ViewHolderMultiSelectionWrapper(
                VH holder,
                boolean shouldSetClickListener,
                boolean shouldSetLongClickListener
        ) {
            super(holder);
            this.shouldSetClickListener = shouldSetClickListener;
            this.shouldSetLongClickListener = shouldSetLongClickListener;
            initListeners(holder);
        }

        @Override
        protected boolean shouldSetClickListener() {
            return shouldSetClickListener;
        }

        @Override
        protected boolean shouldSetLongClickListener() {
            return shouldSetLongClickListener;
        }

        @Override
        public final void onClick(View v) {
            VH holder = mWrappedHolderRef.get();
            if (holder != null) {
                final int listPosition = selectionListener.getListPosition(holder.getAdapterPosition());
                changeSelectedStateFromUiNotify(listPosition, holder);
                holderClickListener.onHolderClick(listPosition, holder);
            }
        }

        @Override
        public final boolean onLongClick(View v) {
            VH holder = mWrappedHolderRef.get();
            if (holder != null) {
                final int listPosition = selectionListener.getListPosition(holder.getAdapterPosition());
                changeSelectedStateFromUiNotify(listPosition, holder);
                holderClickListener.onHolderLongClick(listPosition, holder);
            }
            return true;
        }

        private void changeSelectedStateFromUiNotify(int position, @NotNull BaseSelectionRecyclerViewAdapter.ViewHolder<?> holder) {
            final boolean wasSelected = isItemSelected(position);
            if (!changeSelectedStateFromUi(position, holder.getAdapterPosition())) {
                selectionListener.handleSelected(holder, wasSelected);
            }
        }

        private boolean changeSelectedStateFromUi(int position, int wrapperPosition) {
            if (canSelectAtPosition(position)) {
                if (isItemSelected(position)) {
                    if (isResetSelectionAllowed()) {
                        return resetItemSelectedByPosition(position, wrapperPosition, true);
                    } else {
                        // current state is selected, triggering reselect, state must be not changed
                        setItemSelectedByPosition(position, wrapperPosition, true, true);
                        return false;
                    }
                } else {
                    return setItemSelectedByPosition(position, wrapperPosition, true, true);
                }
            }
            return false;
        }
    }

    class ViewHolderClickWrapper<VH extends BaseRecyclerViewAdapter.ViewHolder<?>> extends ViewHolderWrapper<VH> {

        ViewHolderClickWrapper(VH holder) {
            super(holder);
            initListeners(holder);
        }

        @Override
        public final void onClick(View v) {
            VH holder = mWrappedHolderRef.get();
            if (holder != null) {
                holderClickListener.onHolderClick(selectionListener.getListPosition(holder.getAdapterPosition()), holder);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            VH holder = mWrappedHolderRef.get();
            if (holder != null) {
                return holderClickListener.onHolderLongClick(selectionListener.getListPosition(holder.getAdapterPosition()), holder);
            }
            return false;
        }
    }
}