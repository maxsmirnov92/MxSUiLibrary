package net.maxsmr.jugglerhelper.fragments.base.alert;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import net.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter;
import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.jugglerhelper.fragments.base.BaseListJugglerFragment;

import java.util.List;


public abstract class BaseListJugglerAlertsFragment<I, Adapter extends BaseRecyclerViewAdapter<I, ?>> extends BaseListJugglerFragment<I, Adapter> implements AlertDialogFragment.EventListener {

    public static final String TAG_ALERT_FRAGMENT = BaseListJugglerAlertsFragment.class.getSimpleName() + ".TAG_ALERT_FRAGMENT";

    @NonNull
    private String alertTag = TAG_ALERT_FRAGMENT;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alertTag = getAlertTag();
        listenAlert();
    }

    @NonNull
    protected String getAlertTag() {
        return TAG_ALERT_FRAGMENT;
    }

    protected String getAlertTitle() {
        CharSequence title = getActivity() != null? getActivity().getTitle() : null;
        return title != null? title.toString() : null;
    }

    protected boolean isAlertShowing() {
        return findChildFragmentByTag(alertTag) != null;
    }

    protected void listenAlert() {
        Fragment alertFragment = findChildFragmentByTag(alertTag);
        if (alertFragment != null && alertFragment instanceof AlertDialogFragment) {
            ((AlertDialogFragment) alertFragment).setEventListener(this);
        }
    }

    protected AlertDialogFragment reshowAlert(String message) {
        return reshowAlert(message, android.R.string.ok);
    }

    protected AlertDialogFragment reshowAlert(String message, @StringRes int positiveButton) {
        return reshowAlert(message, getString(positiveButton));
    }

    protected AlertDialogFragment reshowAlert(String message, String positiveButton) {
        AlertDialogFragment.Builder builder = new AlertDialogFragment.Builder();
        builder.setCancelable(true);
        builder.setTitle(getAlertTitle());
        builder.setMessage(message);
        builder.setButtons(positiveButton, null, null);
        return reshowAlert(builder);
    }

    protected AlertDialogFragment reshowAlert(@NonNull AlertDialogFragment.Builder builder) {
        hideAlert();
        AlertDialogFragment fragment = builder.build();
        fragment.show(getChildFragmentManager(), alertTag);
        fragment.setEventListener(this);
        return fragment;
    }

    protected AlertDialogFragment showAlert(String message) {
        return showAlert(message, android.R.string.ok);
    }

    protected AlertDialogFragment showAlert(String message, @StringRes int positiveButton) {
        return showAlert(message, getString(positiveButton));
    }

    protected AlertDialogFragment showAlert(String message, String positiveButton) {
        if (!isAlertShowing()) {
            return reshowAlert(message, positiveButton);
        }
        return null;
    }

    @Nullable
    protected AlertDialogFragment showAlert(@NonNull AlertDialogFragment.Builder builder) {
        if (!isAlertShowing()) {
            return reshowAlert(builder);
        }
        return null;
    }

    protected void hideAlert() {
        Fragment alertFragment = findChildFragmentByTag(alertTag);
        if (alertFragment != null && alertFragment instanceof AlertDialogFragment) {
            ((AlertDialogFragment) alertFragment).dismiss();
        }
    }

    @Override
    public void onDialogButtonClick(AlertDialogFragment fragment, int which) {
        fragment.dismiss();
    }

    @Override
    public boolean onDialogKey(AlertDialogFragment fragment, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onDialogCancel(AlertDialogFragment fragment) {

    }

    @Override
    public void onDialogDismiss(AlertDialogFragment fragment) {

    }
}
