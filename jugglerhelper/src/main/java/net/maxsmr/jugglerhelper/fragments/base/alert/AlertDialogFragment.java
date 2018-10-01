package net.maxsmr.jugglerhelper.fragments.base.alert;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;

public class AlertDialogFragment extends DialogFragment {

    private static AlertDialogFragment newInstance(@Nullable Bundle args) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    protected EventListener eventListener;

    protected Bundle args;

    @Nullable
    private AlertID alertId = null;

    @Nullable
    public AlertID getAlertId() {
        return alertId;
    }

    public void setAlertId(@Nullable AlertID alertId) {
        this.alertId = alertId;
    }

    public void setEventListener(@Nullable EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = getArguments();
        if (args == null) {
            throw new IllegalStateException("specify " + AlertDialogFragment.class.getSimpleName() + " arguments");
        }
    }

    @Override
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        final LayoutInflater inflater = super.onGetLayoutInflater(savedInstanceState);
        if (args.containsKey(Args.ARG_CANCELABLE)) {
            setCancelable(args.getBoolean(Args.ARG_CANCELABLE));
        }
        if (eventListener != null) {
            eventListener.onDialogCreated(this, getDialog());
        }
        return inflater;
    }

    @Nullable
    @Override
    public AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    @CallSuper
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createBuilder(args).create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (eventListener != null) {
            eventListener.onDialogDismiss(this);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (eventListener != null) {
            eventListener.onDialogCancel(this);
        }
    }

    /** override if want to create own {@linkplain AlertDialog.Builder} */
    @NonNull
    protected AlertDialog.Builder createBuilder(@NonNull Bundle args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(args.getString(Args.ARG_TITLE))
                .setMessage(args.getString(Args.ARG_MESSAGE));
        if (args.containsKey(Args.ARG_ICON_RES_ID)) {
            builder.setIcon(args.getInt(Args.ARG_ICON_RES_ID));
        }
        if (args.containsKey(Args.ARG_CUSTOM_VIEW_RES_ID)) {
            builder.setView(args.getInt(Args.ARG_CUSTOM_VIEW_RES_ID));
        }

        final DialogInterface.OnClickListener clickListener = (dialog, which) -> {
            if (eventListener != null) {
                eventListener.onDialogButtonClick(AlertDialogFragment.this, which);
            }
        };

        if (args.containsKey(Args.ARG_BUTTON_POSITIVE)) {
            builder.setPositiveButton(args.getString(Args.ARG_BUTTON_POSITIVE), clickListener);
        }
        if (args.containsKey(Args.ARG_BUTTON_NEUTRAL)) {
            builder.setNeutralButton(args.getString(Args.ARG_BUTTON_NEUTRAL), clickListener);
        }
        if (args.containsKey(Args.ARG_BUTTON_NEGATIVE)) {
            builder.setNegativeButton(args.getString(Args.ARG_BUTTON_NEGATIVE), clickListener);
        }
        builder.setOnKeyListener((dialog, keyCode, event) -> eventListener != null && eventListener.onDialogKey(AlertDialogFragment.this, keyCode, event));
        return builder;
    }

    public static abstract class Builder<F extends AlertDialogFragment> {

        protected String title;
        protected String message;
        @DrawableRes
        protected int iconResId;
        @LayoutRes
        protected int customViewResId;
        protected boolean cancelable;
        protected String buttonPositive, buttonNeutral, buttonNegative;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setIconResId(int iconResId) {
            this.iconResId = iconResId;
            return this;
        }

        public Builder setCustomView(int customViewResId) {
            this.customViewResId = customViewResId;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setButtons(String positive, String neutral, String negative) {
            buttonNegative = negative;
            buttonNeutral = neutral;
            buttonPositive = positive;
            return this;
        }

        protected Bundle fillArgs() {
            Bundle args = new Bundle();
            if (title != null) {
                args.putString(Args.ARG_TITLE, title);
            }
            if (message != null) {
                args.putString(Args.ARG_MESSAGE, message);
            }
            if (iconResId != 0) {
                args.putInt(Args.ARG_ICON_RES_ID, iconResId);
            }
            if (customViewResId != 0) {
                args.putInt(Args.ARG_CUSTOM_VIEW_RES_ID, customViewResId);
            }
            if (buttonPositive != null) {
                args.putString(Args.ARG_BUTTON_POSITIVE, buttonPositive);
            }
            if (buttonNeutral != null) {
                args.putString(Args.ARG_BUTTON_NEUTRAL, buttonNeutral);
            }
            if (buttonNegative != null) {
                args.putString(Args.ARG_BUTTON_NEGATIVE, buttonNegative);
            }
            args.putBoolean(Args.ARG_CANCELABLE, cancelable);
            return args;
        }

        public abstract F build();
    }

    public interface EventListener {

        void onDialogCreated(@NonNull AlertDialogFragment fragment, @Nullable AlertDialog dialog);

        void onDialogButtonClick(@NonNull AlertDialogFragment fragment, int which);

        boolean onDialogKey(@NonNull AlertDialogFragment fragment, int keyCode, KeyEvent event);

        void onDialogCancel(@NonNull AlertDialogFragment fragment);

        void onDialogDismiss(@NonNull AlertDialogFragment fragment);
    }

    public interface AlertID {

        int getId();
    }

    protected interface Args {

        String ARG_TITLE = Args.class.getName() + ".ARG_TITLE";
        String ARG_MESSAGE = Args.class.getName() + ".ARG_MESSAGE";
        String ARG_ICON_RES_ID = Args.class.getName() + ".ARG_ICON_RES_ID";
        String ARG_CUSTOM_VIEW_RES_ID = Args.class.getName() + ".ARG_CUSTOM_VIEW_RES_ID";
        String ARG_CANCELABLE = Args.class.getName() + ".ARG_CANCELABLE";
        String ARG_BUTTON_POSITIVE = Args.class.getName() + ".ARG_BUTTON_OK";
        String ARG_BUTTON_NEGATIVE = Args.class.getName() + ".ARG_BUTTON_NEGATIVE";
        String ARG_BUTTON_NEUTRAL = Args.class.getName() + ".ARG_BUTTON_NEUTRAL";
    }


    public static class DefaultBuilder extends Builder<AlertDialogFragment> {

        @Override
        public AlertDialogFragment build() {
            return newInstance(fillArgs());
        }
    }

}
