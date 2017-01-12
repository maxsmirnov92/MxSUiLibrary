package net.maxsmr.jugglerhelper.fragments.base;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import net.maxsmr.jugglerhelper.R;
import net.maxsmr.networkutils.NetworkHelper;
import net.maxsmr.permissionchecker.PermissionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseLoadingJugglerFragment<I> extends BaseJugglerFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final Logger logger = LoggerFactory.getLogger(BaseLoadingJugglerFragment.class);

    private BroadcastReceiver networkReceiver;

    protected boolean isLoadErrorOccurred = false;
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
    protected void onBindViews(@NonNull View rootView) {
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
        if (PermissionUtils.has(getContext(), "android.permission.ACCESS_NETWORK_STATE") && !isNetworkBroadcastReceiverRegistered()) {
            getContext().registerReceiver(networkReceiver = new NetworkBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkBroadcastReceiver() {
        if (isNetworkBroadcastReceiverRegistered()) {
            getContext().unregisterReceiver(networkReceiver);
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

    private void invalidateLoading() {
        loading(isLoading);
        afterLoading();
    }

    protected boolean enableSwipeRefresh() {
        return true;
    }


    protected abstract boolean allowReloadOnNetworkRestored();

    protected void afterLoading() {
        logger.debug("afterLoading");
            if (!isDataEmpty()) {
                onLoaded();
            } else {
                onEmpty();
            }

    }

    protected final void onStartLoading() {
        logger.debug("onStartLoading()");
        if (progressable != null) {
            progressable.onStart();
        }
    }

    protected final void onStopLoading() {
        logger.debug("onStopLoading()");
        if (progressable != null) {
            progressable.onStop();
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
        if (!isLoading) {
            processEmpty();
        }
    }

    protected abstract boolean isDataEmpty();

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
        isLoadErrorOccurred = true;
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

        invalidateLoading();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getJugglerActivity().getJuggler().activateCurrentState();
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
        return getContext().getString(R.string.data_missing);
    }

    @Nullable
    protected String getErrorText() {
        return getContext().getString(R.string.data_load_failed);
    }

    @CallSuper
    protected void onLoaded() {
        isLoadErrorOccurred = false;
        processEmpty();
    }

    @CallSuper
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
        if (isOnline && isLoadErrorOccurred && allowReloadOnNetworkRestored()) {
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
            if (isAdded()) {
                loading(true);
            }
        }

        @Override
        public void onStop() {
            if (isAdded()) {
                loading(false);
            }
        }
    }

    protected Progressable newLoadFragmentProgressable() {
        return new LoadFragmentProgressable();
    }

    protected Progressable newDialogProgressable() {
        return new DialogProgressable(getContext());
    }

    private class NetworkBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            logger.debug("NetworkBroadcastReceiver :: onReceive(), intent=" + intent);
            if (intent != null && ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                onNetworkStatusChanged(NetworkHelper.isOnline(context));
            }
        }
    }
}