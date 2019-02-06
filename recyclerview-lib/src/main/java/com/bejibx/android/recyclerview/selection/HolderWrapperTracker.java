package com.bejibx.android.recyclerview.selection;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HolderWrapperTracker {

    private final SparseArray<SelectionHelper.ViewHolderWrapper> mWrappersByPosition = new SparseArray<>();

    public void bindWrapper(SelectionHelper.ViewHolderWrapper wrapper, int position) {
        mWrappersByPosition.put(position, wrapper);
    }

    public void recycleWrapper(int position) {
        SelectionHelper.ViewHolderWrapper wrapper = mWrappersByPosition.get(position);
        if (wrapper != null) {
            final RecyclerView.ViewHolder holder = wrapper.getHolder();
            if (holder != null && holder.isRecyclable()) {
                if (wrapper instanceof SelectionHelper.ViewHolderMultiSelectionWrapper) {
                    Set<SelectionHelper.SelectMode> selectModes = ((SelectionHelper.ViewHolderMultiSelectionWrapper) wrapper).getSelectModes();
                    if (selectModes.contains(SelectionHelper.SelectMode.CLICK)) {
                        holder.itemView.setOnClickListener(null);
                    }
                    if (selectModes.contains(SelectionHelper.SelectMode.LONG_CLICK)) {
                        holder.itemView.setOnLongClickListener(null);
                    }
                } else if (wrapper instanceof SelectionHelper.ViewHolderClickWrapper) {
                    holder.itemView.setOnClickListener(null);
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
                if (adapterPosition != position && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
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
