package net.maxsmr.android.recyclerview.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroupViewHolders {

    public GroupViewHolders() {
        throw new AssertionError("no instances.");
    }

    public static void expandAllGroups(ExpandableListView listView) {
        if (listView != null && listView.getExpandableListAdapter() != null) {
            for (int i = 0; i < listView.getExpandableListAdapter().getGroupCount(); i++) {
                listView.expandGroup(i);
            }
        }
    }

    public static void collapseAllGroups(ExpandableListView listView) {
        if (listView != null && listView.getExpandableListAdapter() != null) {
            for (int i = 0; i < listView.getExpandableListAdapter().getGroupCount(); i++) {
                listView.collapseGroup(i);
            }
        }
    }

    public static abstract class BaseGroupViewHolder<I> {

        @NotNull
        protected View itemView;

        public BaseGroupViewHolder(int itemLayoutId, @NotNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext()).inflate(itemLayoutId, parent, false));
        }

        public BaseGroupViewHolder(@NotNull View itemView) {
            this.itemView = itemView;
            onBindView(this.itemView);
        }

        @NotNull
        public View getItemView() {
            return itemView;
        }

        protected abstract void onBindView(@NotNull View itemView);

        protected boolean isItemEmpty(@Nullable I item) {
            return item == null;
        }

        public void bindData(@NotNull I item, int groupPosition) {
            itemView.setVisibility(View.VISIBLE);
        }

        public void bindNoData(int groupPosition) {
            itemView.setVisibility(View.GONE);
        }
    }

    public abstract static class BaseChildViewHolder<I> {

        @NotNull
        protected View itemView;

        public BaseChildViewHolder(int itemLayoutId, @NotNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext()).inflate(itemLayoutId, parent, false));
        }

        public BaseChildViewHolder(@NotNull View itemView) {
            this.itemView = itemView;
            onBindView(itemView);
        }

        @NotNull
        public View getItemView() {
            return itemView;
        }

        protected abstract void onBindView(@NotNull View itemView);

        public void bindData(@NotNull I item, int groupPosition, int childPosition) {
            itemView.setVisibility(View.VISIBLE);
        }

        public void bindNoData(int groupPosition, int childPosition) {
            itemView.setVisibility(View.GONE);
        }
    }
}
