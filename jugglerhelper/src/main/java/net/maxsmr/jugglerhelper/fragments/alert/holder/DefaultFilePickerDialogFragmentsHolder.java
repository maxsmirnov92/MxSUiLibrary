package net.maxsmr.jugglerhelper.fragments.alert.holder;

import net.maxsmr.jugglerhelper.fragments.alert.AlertDialogFragment;

import org.jetbrains.annotations.NotNull;

public class DefaultFilePickerDialogFragmentsHolder extends BaseFilePickerDialogFragmentsHolder<AlertDialogFragment.EventListener, BaseAlertDialogFragmentsHolder.AlertEventsObservable<AlertDialogFragment.EventListener>> {

    @NotNull
    @Override
    protected AlertEventsObservable<AlertDialogFragment.EventListener> newAlertEventsObservable() {
        return new AlertEventsObservable<>();
    }

    @Override
    public void onSetEventListener(@NotNull AlertDialogFragment<AlertDialogFragment.EventListener> forFragment) {
        forFragment.setEventListener(this);
    }
}
