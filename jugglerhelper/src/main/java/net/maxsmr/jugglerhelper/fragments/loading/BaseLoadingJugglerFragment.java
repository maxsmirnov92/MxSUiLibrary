package net.maxsmr.jugglerhelper.fragments.loading;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.maxsmr.commonutils.android.gui.GuiUtils;
import net.maxsmr.commonutils.android.gui.fonts.FontsHolder;
import net.maxsmr.commonutils.android.gui.progressable.DialogProgressable;
import net.maxsmr.commonutils.android.gui.progressable.Progressable;
import net.maxsmr.commonutils.android.gui.progressable.WrappedProgressable;
import net.maxsmr.jugglerhelper.R;
import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        return R.id.emptyText;
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
    protected void onBindViews(@NotNull View rootView) {
        swipeRefreshLayout = GuiUtils.findViewById(rootView, getSwipeRefreshLayoutId());
        placeholder = GuiUtils.findViewById(rootView, getEmptyTextId());
        loadingLayout = GuiUtils.findViewById(rootView, getLoadingLayoutId());
        loadingMessageView = GuiUtils.findViewById(rootView, getLoadingMessageViewId());
        retryButton = GuiUtils.findViewById(rootView, getRetryButtonId());
    }

    protected boolean isNetworkBroadcastReceiverRegistered() {
        return networkReceiver != null;
    }

    protected void registerNetworkBroadcastReceiver() {
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED && !isNetworkBroadcastReceiverRegistered()) {
            getContext().registerReceiver(networkReceiver = new NetworkBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkBroadcastReceiver() {
        if (isNetworkBroadcastReceiverRegistered()) {
            getContext().unregisterReceiver(networkReceiver);
            networkReceiver = null;
        }
    }


    @CallSuper
    protected void applyTypeface() {

        View rootView = getView();

        if (rootView == null) {
            throw new IllegalStateException("root view was not created");
        }

        String alias = getBaseFontAlias();

        if (!TextUtils.isEmpty(alias)) {
            FontsHolder.getInstance().apply(placeholder, alias, false);
            FontsHolder.getInstance().apply(loadingLayout, alias, false);
            FontsHolder.getInstance().apply(retryButton, alias, false);
        }
    }


    protected void invalidateLoading(@Nullable I data) {
        loading(isLoading);
        afterLoading(data);
    }

    protected boolean enableSwipeRefresh() {
        return true;
    }


    protected abstract boolean allowReloadOnNetworkRestored();

    protected abstract boolean allowSetInitial();

    @Nullable
    protected abstract I getInitial();

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

    protected void loading(boolean isLoading) {
        this.isLoading = isLoading;
        if (loadingLayout != null) {
            loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (placeholder != null) {
            placeholder.setVisibility(View.GONE);
        }
        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }
        if (swipeRefreshLayout != null && swipeRefreshLayout.isEnabled()) {
            if (swipeRefreshLayout.isRefreshing() != isLoading) {
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        }
    }

    protected abstract boolean isDataEmpty();

    protected abstract boolean isDataEmpty(@Nullable I data);

    protected void processEmpty() {
        boolean isEmpty = isDataEmpty();
        if (placeholder != null) {
            placeholder.setVisibility(!isEmpty ? View.GONE : View.VISIBLE);
            if (isEmpty) {
                placeholder.setText(getEmptyText());
            }
        }
        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }

    }

    @CallSuper
    protected void processError() {
        if (placeholder != null) {
            placeholder.setVisibility(View.VISIBLE);
            placeholder.setText(getErrorText());
        }
        if (retryButton != null) {
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setEnabled(enableSwipeRefresh());
        }

        progressable = initProgressable();

        if (retryButton != null) {
            retryButton.setOnClickListener(this);
        }

        applyTypeface();

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

    protected abstract boolean isLoadErrorOccurred();

    protected void onLoaded(@NotNull I data) {
        if (!isLoadErrorOccurred()) {
            processEmpty();
        }
    }

    protected void onEmpty() {
        if (!isLoadErrorOccurred()) {
            processEmpty();
        }
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

        public LoadFragmentProgressable() {
            if (getContext() == null) {
                throw new IllegalStateException("fragment is not attached");
            }
            if (loadingLayout != null) {
                GuiUtils.setProgressBarColor(ContextCompat.getColor(getContext(), R.color.progress_primary), (ProgressBar) loadingLayout.findViewById(R.id.pbLoading));
            }
            if (swipeRefreshLayout != null) {
//                swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.progressBarColor);
                swipeRefreshLayout.setColorSchemeResources(
                        R.color.progress_start,
                        R.color.progress_primary,
                        R.color.progress_end);
            }
        }

        @Override
        public void onStart() {
            if (isCommitAllowed()) {
                loading(true);
            }
        }

        @Override
        public void onStop() {
            if (isCommitAllowed()) {
                loading(false);
            }
        }
    }

    protected LoadFragmentProgressable newLoadFragmentProgressable() {
        return new LoadFragmentProgressable();
    }

    protected DialogProgressable newDialogProgressable() {
        return new DialogProgressable(getContext());
    }

    protected WrappedProgressable newWrappedLoadFragmentProgressable(Progressable... innerProgressables) {
        final List<Progressable> progressables = new ArrayList<>();
        progressables.add(newLoadFragmentProgressable());
        if (innerProgressables != null) {
            progressables.addAll(Arrays.asList(innerProgressables));
        }
        return new WrappedProgressable(progressables.toArray(new Progressable[progressables.size()])) {
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
}
