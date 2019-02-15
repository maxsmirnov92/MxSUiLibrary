package net.maxsmr.jugglerhelper.fragments.alert;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.maxsmr.jugglerhelper.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ProgressDialogFragment<L extends AlertDialogFragment.EventListener> extends AlertDialogFragment<L> {

    private static <EL extends AlertDialogFragment.EventListener> ProgressDialogFragment<EL> newInstance(@Nullable Bundle args) {
        ProgressDialogFragment<EL> fragment = new ProgressDialogFragment<EL>();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    protected ProgressBar progressBar;

    protected TextView loadingMessageView;

    protected int getProgressBarId() {
        return R.id.pbLoading;
    }

    protected int getLoadingMessageViewId() {
        return R.id.tvLoadingMessage;
    }

    @Override
    protected void onDialogCreated(@NotNull AlertDialog dialog) {
        super.onDialogCreated(dialog);
        if (customView != null) {
            final int progressBarId = getProgressBarId();
            if (progressBarId != 0) {
                progressBar = customView.findViewById(progressBarId);
            }
            final int loadingMessageId = getLoadingMessageViewId();
            if (loadingMessageId != 0) {
                loadingMessageView = customView.findViewById(loadingMessageId);
            }
        }
        if (loadingMessageView != null) {
            final String message = args.getString(Args.ARG_MESSAGE);
            loadingMessageView.setText(message);
        }
    }

    public static class DefaultBuilder extends Builder<ProgressDialogFragment<EventListener>> {

        @Override
        public ProgressDialogFragment<EventListener> build() {
            return newInstance(createArgs());
        }
    }
}
