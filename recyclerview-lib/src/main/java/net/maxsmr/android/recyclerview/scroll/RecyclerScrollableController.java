package net.maxsmr.android.recyclerview.scroll;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class RecyclerScrollableController extends RecyclerView.OnScrollListener {

    public static final int VISIBLE_THRESHOLD = 5;

    private int previousTotal = 0;
    private boolean loading = true;

    private OnLastItemVisibleListener listener;

    public RecyclerScrollableController(OnLastItemVisibleListener listener) {
        if (listener == null) {
            listener = OnLastItemVisibleListener.STUB;
        }
        this.listener = listener;
    }

    @Override
    public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        if (layoutManager == null) {
            return;
        }

        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        boolean isLastItemVisible = totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD;
        if (!loading && isLastItemVisible) {
            if (listener != null) {
                listener.onLastItemVisible();
            }
            loading = true;
        }
    }

    public interface OnLastItemVisibleListener {

        OnLastItemVisibleListener STUB = () -> { };

        void onLastItemVisible();
    }
}
