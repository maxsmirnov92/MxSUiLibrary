package net.maxsmr.android.recyclerview.adapters.base.selection.multi;

import android.util.SparseArray;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class HolderWrapperTracker {

    private final SparseArray<SelectionHelper.ViewHolderWrapper> mWrappersByPosition = new SparseArray<>();

    public void bindWrapper(SelectionHelper.ViewHolderWrapper wrapper, int position) {
        mWrappersByPosition.put(position, wrapper);
    }

    public void recycleWrapper(int position) {
        SelectionHelper.ViewHolderWrapper wrapper = mWrappersByPosition.get(position);
        if (wrapper != null) {
            final RecyclerView.ViewHolder holder = wrapper.getHolder();
            if (holder != null && holder.isRecyclable()) {
                if (wrapper.shouldSetClickListener() && wrapper.clickableView != null) {
                    wrapper.clickableView.setOnClickListener(null);
                }
                if (wrapper.shouldSetLongClickListener() && wrapper.longClickableView != null) {
                    wrapper.longClickableView.setOnClickListener(null);
                }
                mWrappersByPosition.remove(position);
            }
        }
    }

    @Nullable
    public SelectionHelper.ViewHolderWrapper getWrapper(int position) {
        SelectionHelper.ViewHolderWrapper wrapper = mWrappersByPosition.get(position);

        boolean correct = true;

        if (wrapper == null) {
            correct = false;
        } else {
            final RecyclerView.ViewHolder holder = wrapper.getHolder();
            if (holder != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != position && adapterPosition != RecyclerView.NO_POSITION) {
                    correct = false;
                }
            } else {
                correct = false;
            }
        }

        if (!correct) {
            mWrappersByPosition.remove(position);
            return null;
        }

        return wrapper;
    }

    @NotNull
    public List<SelectionHelper.ViewHolderWrapper> getTrackedWrappers() {
        List<SelectionHelper.ViewHolderWrapper> wrappers = new ArrayList<>();

        for (int i = 0; i < mWrappersByPosition.size(); i++) {
            int key = mWrappersByPosition.keyAt(i);
            SelectionHelper.ViewHolderWrapper wrapper = getWrapper(key);
            if (wrapper != null) {
                wrappers.add(wrapper);
            }
        }

        return wrappers;
    }

    public void clear() {
        mWrappersByPosition.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        clear();
    }
}
