package net.maxsmr.jugglerhelper.fragments.base.alert;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import static net.maxsmr.jugglerhelper.fragments.base.alert.AlertDialogFragment.Args.ARG_CUSTOM_VIEW_RES_ID;

public class AlertDialogFragment extends DialogFragment {

    public interface Args {
        String ARG_TITLE = Args.class.getName() + ".ARG_TITLE";
        String ARG_MESSAGE = Args.class.getName() + ".ARG_MESSAGE";
        String ARG_ICON_RES_ID = Args.class.getName() + ".ARG_ICON_RES_ID";
        String ARG_CUSTOM_VIEW_RES_ID = Args.class.getName() + ".ARG_CUSTOM_VIEW_RES_ID";
        String ARG_CANCELABLE = Args.class.getName() + ".ARG_CANCELABLE";
        String ARG_BUTTON_POSITIVE = Args.class.getName() + ".ARG_BUTTON_OK";
        String ARG_BUTTON_NEGATIVE = Args.class.getName() + ".ARG_BUTTON_NEGATIVE";
        String ARG_BUTTON_NEUTRAL = Args.class.getName() + ".ARG_BUTTON_NEUTRAL";
    }

    public static AlertDialogFragment newInstance(@Nullable Bundle args) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private AlertID alertId = null;

    @Nullable
    private EventListener eventListener;

    public void setAlertId(@Nullable AlertID alertId) {
        this.alertId = alertId;
    }

    @Nullable
    public AlertID getAlertId() {
        return alertId;
    }

    public void setEventListener(@Nullable EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(args.getBoolean(Args.ARG_CANCELABLE))
                    .setTitle(args.getString(Args.ARG_TITLE))
                    .setMessage(args.getString(Args.ARG_MESSAGE));
            if (args.containsKey(Args.ARG_ICON_RES_ID)) {
                builder.setIcon(args.getInt(Args.ARG_ICON_RES_ID));
            }
            if (args.containsKey(ARG_CUSTOM_VIEW_RES_ID)) {
                builder.setView(args.getInt(ARG_CUSTOM_VIEW_RES_ID));
            }

            final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (eventListener != null) {
                        eventListener.onDialogButtonClick(AlertDialogFragment.this, which);
                    }
                }
            };
            if (args.containsKey(Args.ARG_BUTTON_POSITIVE)) {
                builder.setPositiveButton(args.getString(Args.ARG_BUTTON_POSITIVE), clickListener);
            }
            if (args.containsKey(Args.ARG_BUTTON_NEUTRAL)) {
                builder.setNeutralButton(args.getString(Args.ARG_BUTTON_NEUTRAL), clickListener);
            }
            if (args.containsKey(Args.ARG_BUTTON_NEGATIVE)) {
                builder.setNeutralButton(args.getString(Args.ARG_BUTTON_NEGATIVE), clickListener);
            }
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return eventListener != null && eventListener.onDialogKey(AlertDialogFragment.this, keyCode, event);
                }
            });
            return builder.create();
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (eventListener != null) {
            eventListener.onDialogCancel(AlertDialogFragment.this);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (eventListener != null) {
            eventListener.onDialogDismiss(AlertDialogFragment.this);
        }
    }

    public static class Builder {

        String title;
        String message;
        @DrawableRes
        int iconResId;
        @LayoutRes
        int customViewResId;
        boolean cancelable;
        String buttonPositive, buttonNeutral, buttonNegative;

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

        public AlertDialogFragment build() {
            Bundle args = new Bundle();
            args.putBoolean(AlertDialogFragment.Args.ARG_CANCELABLE, cancelable);
            if (title != null) {
                args.putString(AlertDialogFragment.Args.ARG_TITLE, title);
            }
            if (message != null) {
                args.putString(AlertDialogFragment.Args.ARG_MESSAGE, message);
            }
            if (iconResId != 0) {
                args.putInt(Args.ARG_ICON_RES_ID, iconResId);
            }
            if (customViewResId != 0) {
                args.putInt(ARG_CUSTOM_VIEW_RES_ID, customViewResId);
            }
            if (buttonPositive != null) {
                args.putString(AlertDialogFragment.Args.ARG_BUTTON_POSITIVE, buttonPositive);
            }
            if (buttonNeutral != null) {
                args.putString(Args.ARG_BUTTON_NEUTRAL, buttonNeutral);
            }
            if (buttonNegative != null) {
                args.putString(Args.ARG_BUTTON_NEGATIVE, buttonNegative);
            }
            return newInstance(args);
        }
    }

    public interface EventListener {

        void onDialogButtonClick(AlertDialogFragment fragment, int which);

        boolean onDialogKey(AlertDialogFragment fragment, int keyCode, KeyEvent event);

        void onDialogCancel(AlertDialogFragment fragment);

        void onDialogDismiss(AlertDialogFragment fragment);
    }

    public interface AlertID {

        int getId();
    }

}
