package net.maxsmr.jugglerhelper.fragments.loading;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.maxsmr.jugglerhelper.R;
import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment;
import net.maxsmr.jugglerhelper.progressable.DialogProgressable;
import net.maxsmr.jugglerhelper.progressable.Progressable;
import net.maxsmr.jugglerhelper.progressable.WrappedProgressable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public abstract class BaseLoadingJugglerFragment<I> extends BaseJugglerFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    protected BroadcastReceiver networkReceiver;

    protected boolean isLoading = false;

    @Nullable
    protected SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    protected TextView placeholder;

    @Nullable
    protected LinearLayout loadingLayout;

    @Nullable
    protected TextView loadingMessageView;

    @Nullable
    protected Button retryButton;

    @Nullable
    protected Progressable progressable;

    @Nullable
    protected Progressable initProgressable() {
        return newLoadFragmentProgressable();
    }

    @IdRes
    protected int getSwipeRefreshLayoutId() {
        return R.id.swipeRefreshLayout;
    }

    @IdRes
    protected int getEmptyTextId() {
        return R.id.tvEmptyText;
    }

    @IdRes
    protected int getLoadingLayoutId() {
        return R.id.loading;
    }

    @IdRes
    protected int getLoadingMessageViewId() {
        return R.id.tvLoadingMessage;
    }

    @IdRes
    protected int getRetryButtonId() {
        return R.id.btRetry;
    }

    @CallSuper
    protected void initViews(@NotNull View rootView) {
        final int swipeRefreshLayoutId = getSwipeRefreshLayoutId();
        if (swipeRefreshLayoutId != 0) {
            swipeRefreshLayout = rootView.findViewById(swipeRefreshLayoutId);
        }
        final int emptyTextId = getEmptyTextId();
        if (emptyTextId != 0) {
            placeholder = rootView.findViewById(emptyTextId);
        }
        final int loadingLayoutId = getLoadingLayoutId();
        if (loadingLayoutId != 0) {
            loadingLayout = rootView.findViewById(loadingLayoutId);
        }
        final int loadingMessageViewId = getLoadingMessageViewId();
        if (loadingMessageViewId != 0) {
            loadingMessageView = rootView.findViewById(loadingMessageViewId);
        }
        final int retryButtonId = getRetryButtonId();
        if (retryButtonId != 0) {
            retryButton = rootView.findViewById(retryButtonId);
        }
    }

    protected boolean isNetworkBroadcastReceiverRegistered() {
        return networkReceiver != null;
    }

    @SuppressWarnings("ConstantConditions")
    protected void registerNetworkBroadcastReceiver() {
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED && !isNetworkBroadcastReceiverRegistered()) {
            getContext().registerReceiver(networkReceiver = new NetworkBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void unregisterNetworkBroadcastReceiver() {
        if (isNetworkBroadcastReceiverRegistered()) {
            getContext().unregisterReceiver(networkReceiver);
            networkReceiver = null;
        }
    }

    protected void invalidateLoading(@Nullable I data) {
        if (isLoading) {
            onStartLoading();
        } else {
            onStopLoading();
        }
        afterLoading(data);
    }

    protected boolean isSwipeRefreshChangeStateEnabled() {
        return true;
    }

    protected abstract boolean allowReloadOnNetworkRestored();

    protected abstract boolean allowSetInitial();

    @Nullable
    protected abstract I getInitial();

    protected boolean allowRetryButtonOnEmpty() {
        return true;
    }

    protected boolean allowRetryButtonOnError() {
        return true;
    }

    protected void afterLoading(@Nullable I data) {
        if (!isLoadErrorOccurred()) {
            if (data != null && !isDataEmpty(data)) {
                onLoaded(data);
            } else {
                onEmpty();
            }
        } else {
            processError();
        }
    }

    protected final void onStartLoading() {
        if (progressable != null) {
            progressable.onStart();
        }
    }

    protected final void onStopLoading() {
        if (progressable != null) {
            progressable.onStop();
        }
    }

    @Override
    public void onRefresh() {
        if (allowSetInitial()) {
            afterLoading(getInitial());
        }
    }

    protected void processLoading(boolean isLoading) {
        this.isLoading = isLoading;
        if (loadingLayout != null) {
            if (loadingMessageView != null) {
                ColorStateList loadingTextColor = getLoadingTextColor();
                if (loadingTextColor != null) {
                    loadingMessageView.setTextColor(loadingTextColor);
                }
            }
            loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        switchSwipe(isLoading);
        if (isLoading) {
            if (placeholder != null) {
                placeholder.setVisibility(View.GONE);
            }
            if (retryButton != null) {
                retryButton.setVisibility(View.GONE);
            }
        }
    }

    protected void switchSwipe(boolean toggle) {
        if (swipeRefreshLayout != null) {
            if (isSwipeRefreshChangeStateEnabled()) {
                if (swipeRefreshLayout.isRefreshing() != toggle) {
                    swipeRefreshLayout.setRefreshing(toggle);
                }
            }
        }
    }

    protected abstract boolean isDataEmpty();

    protected abstract boolean isDataEmpty(@Nullable I data);

    protected void processEmpty() {
        if (!isLoadErrorOccurred()) {
            boolean isEmpty = isDataEmpty();
            if (placeholder != null) {
                placeholder.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                if (isEmpty) {
                    placeholder.setText(getEmptyText());
                    final ColorStateList emptyTextColor = getEmptyTextColor();
                    if (emptyTextColor != null) {
                        placeholder.setTextColor(emptyTextColor);
                    }
                }
            }
            if (retryButton != null) {
                retryButton.setVisibility(isEmpty && allowRetryButtonOnEmpty() ? View.VISIBLE : View.GONE);
            }
        }
    }

    @CallSuper
    protected void processError() {
        final boolean isLoadErrorOccurred = isLoadErrorOccurred();
        if (placeholder != null) {
            placeholder.setVisibility(isLoadErrorOccurred ? View.VISIBLE : View.GONE);
            if (isLoadErrorOccurred) {
                placeholder.setText(getErrorText());
                final ColorStateList errorTextColor = getErrorTextColor();
                if (errorTextColor != null) {
                    placeholder.setTextColor(errorTextColor);
                }
            }
        }
        if (retryButton != null) {
            retryButton.setVisibility(isLoadErrorOccurred && allowRetryButtonOnError() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setEnabled(isSwipeRefreshChangeStateEnabled());
        }

        progressable = initProgressable();

        if (retryButton != null) {
            retryButton.setOnClickListener(this);
        }

        registerNetworkBroadcastReceiver();

        if (allowSetInitial()) {
            invalidateLoading(getInitial());
        } else {
            invalidateLoading(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(null);
        }
        unregisterNetworkBroadcastReceiver();
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    protected String getEmptyText() {
        return getContext().getString(R.string.no_data);
    }

    @Nullable
    protected String getLoadingText() {
        return getString(R.string.loading);
    }

    @Nullable
    protected String getErrorText() {
        return getContext().getString(R.string.data_load_failed);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    protected ColorStateList getEmptyTextColor() {
        return ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.textColorEmptyMessage));
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    protected ColorStateList getLoadingTextColor() {
        return ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.textColorLoadingMessage));
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    protected ColorStateList getErrorTextColor() {
        return ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.textColorErrorMessage));
    }

    protected abstract boolean isLoadErrorOccurred();

    protected void onLoaded(@NotNull I data) {
        processEmpty();
    }

    protected void onEmpty() {
        processEmpty();
    }


    protected void onRetryClick() {
        onRefresh();
    }

    @Override
    @CallSuper
    public void onClick(View v) {
        if (v.getId() == getRetryButtonId()) {
            onRetryClick();
        }
    }

    @CallSuper
    protected void onNetworkStatusChanged(boolean isOnline) {
        if (isOnline && isLoadErrorOccurred() && allowReloadOnNetworkRestored()) {
            onRefresh();
        }
    }

    protected class LoadFragmentProgressable implements Progressable {

        protected LoadFragmentProgressable() {
            if (getContext() == null) {
                throw new IllegalStateException("fragment is not attached");
            }
            if (loadingLayout != null) {
                setProgressBarColor(ContextCompat.getColor(getContext(), R.color.colorProgressBarPrimary), (ProgressBar) loadingLayout.findViewById(R.id.pbLoading));
            }
            if (swipeRefreshLayout != null) {
//                swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.progressBarColor);
                swipeRefreshLayout.setColorSchemeResources(
                        R.color.colorProgressBarStart,
                        R.color.colorProgressBarPrimary,
                        R.color.colorProgressBarEnd);
            }
        }

        @Override
        public void onStart() {
            if (isAdded()) {
                processLoading(true);
            }
        }

        @Override
        public void onStop() {
            if (isAdded()) {
                processLoading(false);
            }
        }
    }

    protected LoadFragmentProgressable newLoadFragmentProgressable() {
        return new LoadFragmentProgressable();
    }

    @SuppressWarnings("ConstantConditions")
    protected DialogProgressable newDialogProgressable() {
        return new DialogProgressable(getContext());
    }

    protected WrappedProgressable newWrappedLoadFragmentProgressable(Progressable... innerProgressables) {
        final List<Progressable> progressables = new ArrayList<>();
        progressables.add(newLoadFragmentProgressable());
        if (innerProgressables != null) {
            progressables.addAll(Arrays.asList(innerProgressables));
        }
        return new WrappedProgressable(progressables.toArray(new Progressable[0])) {
            @Override
            protected boolean isAlive() {
                return isCommitAllowed();
            }
        };
    }

    private class NetworkBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                onNetworkStatusChanged(isOnline(context));
            }
        }
    }

    protected static boolean isOnline(@NotNull Context context) {
        if (ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                throw new RuntimeException(ConnectivityManager.class.getSimpleName() + " is null");
            }
            @SuppressLint("MissingPermission") final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetInfo != null && activeNetInfo.isConnected() && activeNetInfo.isAvailable();
        }
        return false;
    }

    private static void setProgressBarColor(@ColorInt int color, @Nullable ProgressBar progressBar) {
        if (progressBar != null) {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (progressBar.isIndeterminate() && progressBar.getIndeterminateDrawable() != null) {
                    progressBar.getIndeterminateDrawable().setColorFilter(color, mode);
                }
                if (!progressBar.isIndeterminate() && progressBar.getProgressDrawable() != null) {
                    progressBar.getProgressDrawable().setColorFilter(color, mode);
                }
            } else {
                ColorStateList stateList = ColorStateList.valueOf(color);
                progressBar.setIndeterminateTintMode(mode);
                progressBar.setProgressTintList(stateList);
                progressBar.setSecondaryProgressTintList(stateList);
                progressBar.setIndeterminateTintList(stateList);
                // new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}}, new int[]{color})
            }
        }
    }
}
