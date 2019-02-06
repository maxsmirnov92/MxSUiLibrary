package net.maxsmr.android.recyclerview.adapters.sample;


import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SampleStringAdapter extends BaseRecyclerViewAdapter<String, SampleStringAdapter.ViewHolder> {

    @IdRes
    protected final int viewResId;

    public SampleStringAdapter(@NotNull Context context, @LayoutRes int baseItemLayoutId, int viewResId) {
        this(context, baseItemLayoutId, viewResId, null);
    }

    public SampleStringAdapter(@NotNull Context context, @LayoutRes int baseItemLayoutId, int viewResId, @Nullable Collection<String> items) {
        super(context, baseItemLayoutId, items);
        this.viewResId = viewResId;
    }

    @NotNull
    @Override
    public final ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(onInflateView(parent, viewType), viewResId);
    }

    public static class ViewHolder extends BaseRecyclerViewAdapter.ViewHolder<String> {

        protected TextView textView;

        public ViewHolder(@NotNull View view, @IdRes int viewResId) {
            super(view);
            textView = (TextView) itemView.findViewById(viewResId);
            if (textView == null) {
                throw new RuntimeException("view with id " + viewResId + " not found");
            }
        }

        @Override
        public void displayData(int position, @NotNull String item, int count) {
            super.displayData(position, item, count);
            textView.setText(item);
            textView.setVisibility(View.VISIBLE);
        }

        @Override
        public void displayEmptyData(int position, @Nullable String item, int count) {
            super.displayEmptyData(position, item, count);
            textView.setVisibility(View.GONE);
        }
    }
}
