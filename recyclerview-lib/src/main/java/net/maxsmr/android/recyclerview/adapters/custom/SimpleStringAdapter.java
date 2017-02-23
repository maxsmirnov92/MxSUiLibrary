package net.maxsmr.android.recyclerview.adapters.custom;


import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter;

import java.util.Collection;

public class SimpleStringAdapter extends BaseRecyclerViewAdapter<String, SimpleStringAdapter.ViewHolder> {

    @IdRes
    protected final int viewResId;

    public SimpleStringAdapter(@NonNull Context context, @LayoutRes int baseItemLayoutId, int viewResId) {
        this(context, baseItemLayoutId, viewResId, null);
    }

    public SimpleStringAdapter(@NonNull Context context, @LayoutRes int baseItemLayoutId, int viewResId, @Nullable Collection<String> items) {
        super(context, baseItemLayoutId, items);
        this.viewResId = viewResId;
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(onInflateView(parent, viewType), viewResId);
    }

    public static class ViewHolder extends BaseRecyclerViewAdapter.ViewHolder<String> {

        protected TextView textView;

        public ViewHolder(@NonNull View view, @IdRes int viewResId) {
            super(view);
            textView = (TextView) itemView.findViewById(viewResId);
            if (textView == null) {
                throw new RuntimeException("view with id " + viewResId + " not found");
            }
        }

        @Override
        protected void displayData(int position, @NonNull String item) {
            super.displayData(position, item);
            textView.setText(item);
            textView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void displayNoData(int position, @Nullable String item) {
            textView.setVisibility(View.GONE);
        }
    }
}
