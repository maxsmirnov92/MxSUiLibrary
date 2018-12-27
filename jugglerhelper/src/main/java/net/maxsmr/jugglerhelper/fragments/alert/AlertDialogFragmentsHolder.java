package net.maxsmr.jugglerhelper.fragments.alert;

import android.support.annotation.MainThread;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;

import net.maxsmr.commonutils.data.Observable;
import net.maxsmr.commonutils.data.Predicate;
import net.maxsmr.commonutils.logger.BaseLogger;
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * class for showing/hiding and restoring {@linkplain AlertDialogFragment} linked
 * to specified {@linkplain FragmentManager} alerts
 */
@MainThread
public class AlertDialogFragmentsHolder implements AlertDialogFragment.EventListener {

    protected final BaseLogger logger = BaseLoggerHolder.getInstance().getLogger(getLoggerClass());

    /**
     * tags to use in this holder
     */
    @NotNull
    protected final List<String> tags = new ArrayList<>();

    /**
     * active alerts, added to specified {@linkplain FragmentManager}
     * note that Set may be not actual (for example, if removing/adding directly to manager in other place)
     * so invalidate manually by {@linkplain AlertDialogFragmentsHolder#hideAlert(String)}
     */
    @NotNull
    protected final Set<AlertDialogFragment> activeAlerts = new LinkedHashSet<>();

    /**
     * alerts to show when commits will be allowed
     * remembering tags because instance may not contain target tag
     */
    @NotNull
    protected final Map<String, Pair<AlertDialogFragment, Boolean>> targetAlertsToShow = new LinkedHashMap<>();

    /**
     * alerts tags to hide when commits will be allowed
     */
    @NotNull
    protected final Set<String> targetAlertsToHide = new LinkedHashSet<>();

    private final AlertEventsObservable eventsObservable = new AlertEventsObservable();

    @Nullable
    protected FragmentManager fragmentManager;

    @NotNull
    private ShowRule showRule = ShowRule.MULTI;

    private boolean rememberRejectedFragments = true;

    private boolean isCommitAllowed = true;

    public AlertDialogFragmentsHolder(@NotNull Collection<String> tags) {
        this(null, tags);
    }

    public AlertDialogFragmentsHolder(@Nullable FragmentManager fragmentManager, @NotNull Collection<String> tags) {
        this.tags.addAll(Predicate.Methods.filter(tags, element -> !TextUtils.isEmpty(element)));
        if (this.tags.isEmpty()) {
            throw new IllegalArgumentException("No valid tags specified");
        }
        setFragmentManager(fragmentManager);
    }

    public void registerEventListener(@NotNull AlertDialogFragment.EventListener listener) {
        eventsObservable.registerObserver(listener);
    }

    public void unregisterEventListener(@NotNull AlertDialogFragment.EventListener listener) {
        eventsObservable.unregisterObserver(listener);
    }

    public void setFragmentManager(@Nullable FragmentManager fragmentManager) {
        if (this.fragmentManager != fragmentManager) {
            this.fragmentManager = fragmentManager;
            if (fragmentManager != null) {
                restoreDialogsFromFragmentManager();
                handleTargetFragmentsToHide();
                handleTargetFragmentsToShow();
            } else {
                activeAlerts.clear();
            }
        }
    }

    /**
     * allow showing single or multiple {@linkplain AlertDialogFragment} instances at time
     */
    @NotNull
    public ShowRule getShowRule() {
        return showRule;
    }

    public void setShowRule(@NotNull ShowRule showRule) {
        this.showRule = showRule;
        if (showRule == ShowRule.SINGLE) {
            List<AlertDialogFragment> copyList = new ArrayList<>(activeAlerts);
            Iterator<AlertDialogFragment> it = copyList.iterator();
            while (it.hasNext() && getShowingAlertsCount() > 1) {
                String tag = it.next().getTag();
                if (!TextUtils.isEmpty(tag)) {
                    Pair<Boolean, AlertDialogFragment> hideResult = hideAlert(tag);
                    if (rememberRejectedFragments && hideResult.first != null && hideResult.first) {
                        targetAlertsToShow.put(tag, new Pair<>(hideResult.second, false));
                    }
                }
            }
        } else {
            handleTargetFragmentsToShow();
        }
    }

    /**
     * store rejected by {@linkplain ShowRule#SINGLE} fragments in scheduled for showing when
     * mode will be changed to {@linkplain ShowRule#MULTI}
     */
    public boolean isRememberRejectedFragments() {
        return rememberRejectedFragments;
    }

    public void setRememberRejectedFragments(boolean rememberRejectedFragments) {
        this.rememberRejectedFragments = rememberRejectedFragments;
        if (rememberRejectedFragments && showRule == ShowRule.MULTI) {
            handleTargetFragmentsToShow();
        }
    }

    public boolean isCommitAllowed() {
        return isCommitAllowed;
    }

    /**
     * client code must notify that transactions to {@linkplain FragmentManager} are allowed from now
     */
    public void onResumed() {
        logger.d("onResumed");
        isCommitAllowed = true;
        if (fragmentManager != null) {
            handleTargetFragmentsToHide();
            handleTargetFragmentsToShow();
        }
    }

    /**
     * client code must notify that transactions to {@linkplain FragmentManager} are restricted from now
     */
    public void onSuspended() {
        logger.d("onSuspended");
        isCommitAllowed = false;
    }

    @Override
    public void onDialogCreated(@NotNull AlertDialogFragment fragment, @NotNull AlertDialog dialog) {
        eventsObservable.notifyDialogCreated(fragment, dialog);
    }

    @Override
    public void onDialogButtonClick(@NotNull AlertDialogFragment fragment, int which) {
        eventsObservable.notifyDialogButtonClick(fragment, which);
    }

    @Override
    public boolean onDialogKey(@NotNull AlertDialogFragment fragment, int keyCode, KeyEvent event) {
        return eventsObservable.notifyDialogKeyPressed(fragment, keyCode, event);
    }

    @Override
    public void onDialogCancel(@NotNull AlertDialogFragment fragment) {
        eventsObservable.notifyDialogCancel(fragment);
    }

    @Override
    public void onDialogDismiss(@NotNull AlertDialogFragment fragment) {
        eventsObservable.notifyDialogDismiss(fragment);
        activeAlerts.remove(fragment);
        targetAlertsToHide.remove(fragment.getTag());
        if (fragmentManager != null) {
            if (showRule == ShowRule.SINGLE && !targetAlertsToShow.isEmpty()) {
                // add first scheduled fragment, because of mode
                final Map.Entry<String, Pair<AlertDialogFragment, Boolean>> e = new ArrayList<>(targetAlertsToShow.entrySet()).get(0);
                final Pair<AlertDialogFragment, Boolean> p = e.getValue();
                if (p != null && p.first != null && p.second != null) {
                    showAlert(e.getKey(), p.first, p.second);
                }
            }
        }
    }

    public boolean isAnyAlertShowing() {
        return getShowingAlertsCount() > 0;
    }

    public boolean isAlertShowing(String tag) {
        return getShowingAlertByTag(tag) != null;
    }

    public int getShowingAlertsCount() {
        return Predicate.Methods.filter(activeAlerts, element -> element != null && element.isAdded()).size();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <F extends AlertDialogFragment> F getShowingAlertByTag(String tag) {
        checkTag(tag);
        return (F) Predicate.Methods.find(activeAlerts,
                element -> element != null && element.isAdded() && tag.equals(element.getTag()));
    }

    @MainThread
    protected boolean showAlert(String tag, @NotNull AlertDialogFragment fragment) {
        return showAlert(tag, fragment, true);
    }

    /**
     * @param tag      new tag for adding for specified fragment
     * @param fragment created instance
     * @param reshow   if fragment for specified tag is already showing, it will be re-showed
     * @return true if successfully showed, false - otherwise (also when showing was scheduled)
     */
    @SuppressWarnings("ConstantConditions")
    @MainThread
    protected boolean showAlert(String tag, @NotNull AlertDialogFragment fragment, boolean reshow) {
        logger.d("showAlert: tag=" + tag + ", reshow=" + reshow);

        checkFragmentManager();
        checkTag(tag);

        if (!reshow && isAlertShowing(tag)) {
            return false;
        }

        //noinspection ConstantConditions
        if (!hideAlert(tag).first) {
            return false;
        }

        if (showRule == ShowRule.SINGLE && isAnyAlertShowing()) {
            logger.w("Not adding fragment for tag '" + tag + "', because show rule is '" + ShowRule.SINGLE.name()
                    + "' and some dialogs are showing");
            if (isRememberRejectedFragments()) {
                logger.w("Saving fragment for tag '" + "' to show it later...");
                targetAlertsToShow.put(tag, new Pair<>(fragment, reshow));
            }
            return false;
        }

        if (isCommitAllowed()) {
            fragment.setEventListener(this);
            try {
                fragment.show(fragmentManager, tag);
            } catch (Exception e) {
                logger.e("An Exception occurred during show(): " + e.getMessage(), e);
                return false;
            }
            activeAlerts.add(fragment);
            targetAlertsToShow.remove(tag);
            return true;
        }

        logger.w("Transaction commits are not allowed, schedule showing");
        targetAlertsToShow.put(tag, new Pair<>(fragment, reshow));
        return false;
    }

    /**
     * @return Pair:
     * - true if fragment for specified tag was successfully hided or already not showing,
     * false - otherwise (also when showing was scheduled)
     * - {@linkplain AlertDialogFragment} instance non-null if was added to {@linkplain FragmentManager} before, false otherwise
     */
    @NotNull
    @MainThread
    protected Pair<Boolean, AlertDialogFragment> hideAlert(String tag) {
        logger.d("hideAlert: tag=" + tag);

        checkFragmentManager();

        final AlertDialogFragment fragment = getShowingAlertByTag(tag);

        boolean result = true;
        boolean isDismissed = fragment == null;

        if (fragment != null) {
            if (isCommitAllowed()) {
                try {
                    fragment.dismiss();
                } catch (Exception e) {
                    logger.e("An Exception occurred during dismiss(): " + e.getMessage(), e);
                    result = false;
                }
                isDismissed = result;
            } else {
                logger.w("Transaction commits are not allowed, schedule hiding");
                targetAlertsToHide.add(tag);
                result = false;
            }
        }

        if (isDismissed) {
            Iterator<AlertDialogFragment> it = activeAlerts.iterator();
            while (it.hasNext()) {
                AlertDialogFragment f = it.next();
                if (f != null && tag.equals(f.getTag())) {
                    it.remove();
                }
            }
        }

        if (result) {
            targetAlertsToHide.remove(tag);
        }

        return new Pair<>(result, fragment);
    }

    @SuppressWarnings("ConstantConditions")
    @MainThread
    protected void restoreDialogsFromFragmentManager() {
        checkFragmentManager();
        activeAlerts.clear();
        for (String tag : tags) {
            if (!TextUtils.isEmpty(tag)) {
                AlertDialogFragment fragment = (AlertDialogFragment) fragmentManager.findFragmentByTag(tag);
                if (fragment != null) {
                    activeAlerts.add(fragment);
                    fragment.setEventListener(this);
                }
            }
        }
    }

    @MainThread
    protected void handleTargetFragmentsToShow() {
        for (Map.Entry<String, Pair<AlertDialogFragment, Boolean>> e : targetAlertsToShow.entrySet()) {
            Pair<AlertDialogFragment, Boolean> p = e.getValue();
            if (p != null && p.first != null && p.second != null) {
                String tag = e.getKey();
                showAlert(tag, p.first, p.second);
            }
        }
    }

    @MainThread
    protected void handleTargetFragmentsToHide() {
        for (String tag : targetAlertsToHide) {
            hideAlert(tag);
        }
    }

    protected Class<? extends AlertDialogFragmentsHolder> getLoggerClass() {
        return getClass();
    }

    private void checkFragmentManager() {
        if (fragmentManager == null) {
            throw new IllegalStateException(FragmentManager.class.getSimpleName() + " is not specified");
        }
    }

    private void checkTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("Tag must be non-empty");
        }
        if (!tags.contains(tag)) {
            throw new IllegalArgumentException("Tag '" + tag + "' is not declared in holder");
        }
    }

    public enum ShowRule {
        /**
         * only one fragment in back stack is allowed
         */
        SINGLE,
        /**
         * fragments count is not limited
         */
        MULTI
    }

    protected static class AlertEventsObservable extends Observable<AlertDialogFragment.EventListener> {

        void notifyDialogCreated(AlertDialogFragment fragment, AlertDialog dialog) {
            synchronized (observers) {
                for (AlertDialogFragment.EventListener l : observers) {
                    l.onDialogCreated(fragment, dialog);
                }
            }
        }

        void notifyDialogButtonClick(AlertDialogFragment fragment, int which) {
            synchronized (observers) {
                for (AlertDialogFragment.EventListener l : observers) {
                    l.onDialogButtonClick(fragment, which);
                }
            }
        }

        boolean notifyDialogKeyPressed(AlertDialogFragment fragment, int keyCode, KeyEvent event) {
            boolean handled = false;
            synchronized (observers) {
                for (AlertDialogFragment.EventListener l : observers) {
                    handled |= l.onDialogKey(fragment, keyCode, event);
                }
            }
            return handled;
        }

        void notifyDialogCancel(AlertDialogFragment fragment) {
            synchronized (observers) {
                for (AlertDialogFragment.EventListener l : observers) {
                    l.onDialogCancel(fragment);
                }
            }
        }

        void notifyDialogDismiss(AlertDialogFragment fragment) {
            synchronized (observers) {
                for (AlertDialogFragment.EventListener l : observers) {
                    l.onDialogDismiss(fragment);
                }
            }
        }
    }
}
