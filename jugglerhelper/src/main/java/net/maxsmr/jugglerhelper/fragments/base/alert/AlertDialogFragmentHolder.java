package net.maxsmr.jugglerhelper.fragments.base.alert;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;

import net.maxsmr.commonutils.data.Observable;
import net.maxsmr.commonutils.data.Predicate;
import net.maxsmr.commonutils.logger.BaseLogger;
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * class for showing/hiding and restoring {@linkplain AlertDialogFragment} linked
 * to specified {@linkplain FragmentManager} alerts;
 * if don't want to lose target fragments to show/hide (which scheduled to show/hide before because of commit not allowed),
 * then store reference to holder in {@linkplain android.arch.lifecycle.ViewModel}
 */
public class AlertDialogFragmentHolder implements AlertDialogFragment.EventListener {

    protected final BaseLogger logger = BaseLoggerHolder.getInstance().getLogger(getLoggerClass());

    /**
     * tags to use in this holder
     */
    @NonNull
    protected final List<String> tags = new ArrayList<>();

    /**
     * active alerts, added to specified {@linkplain FragmentManager}
     * note that Set may be not actual (for example, if removing/adding directly to manager in other place)
     * so invalidate manually by {@linkplain AlertDialogFragmentHolder#hideAlert(String)}
     */
    @NonNull
    protected final Set<AlertDialogFragment> activeAlerts = new LinkedHashSet<>();

    /**
     * alerts to show when commits will be allowed
     * remembering tags because instance may not contain target tag
     */
    @NonNull
    protected final Map<String, Pair<AlertDialogFragment, Boolean>> targetAlertsToShow = new LinkedHashMap<>();

    /**
     * alerts tags to hide when commits will be allowed
     */
    @NonNull
    protected final Set<String> targetAlertsToHide = new LinkedHashSet<>();

    @NonNull
    protected final FragmentManager fragmentManager;

    private final AlertEventsObservable eventsObservable = new AlertEventsObservable();

    @NonNull
    private ShowRule showRule = ShowRule.MULTI;

    private boolean rememberRejectedFragments = true;

    private boolean isCommitAllowed = true;

    public AlertDialogFragmentHolder(@NonNull FragmentManager fragmentManager, String... tags) {
        this.fragmentManager = fragmentManager;
        if (tags != null) {
            this.tags.addAll(Arrays.asList(tags));
        }
        restoreDialogsFromFragmentManager();
    }

    public void registerEventListener(@NonNull AlertDialogFragment.EventListener listener) {
        eventsObservable.registerObserver(listener);
    }

    public void unregisterEventListener(@NonNull AlertDialogFragment.EventListener listener) {
        eventsObservable.unregisterObserver(listener);
    }

    /**
     * allow showing single or multiple {@linkplain AlertDialogFragment} instances at time
     */
    @NonNull
    public ShowRule getShowRule() {
        return showRule;
    }

    public void setShowRule(@NonNull ShowRule showRule) {
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
    public synchronized boolean isRememberRejectedFragments() {
        return rememberRejectedFragments;
    }

    public synchronized void setRememberRejectedFragments(boolean rememberRejectedFragments) {
        this.rememberRejectedFragments = rememberRejectedFragments;
        if (rememberRejectedFragments && showRule == ShowRule.MULTI) {
            handleTargetFragmentsToShow();
        }
    }

    public synchronized boolean isCommitAllowed() {
        return isCommitAllowed;
    }

    /**
     * client code must notify that transactions to {@linkplain FragmentManager} are allowed from now
     */
    public synchronized void onResumed() {
        logger.d("onResumed");
        isCommitAllowed = true;
        handleTargetFragmentsToHide();
        handleTargetFragmentsToShow();
    }

    /**
     * client code must notify that transactions to {@linkplain FragmentManager} are restricted from now
     */
    public synchronized void onSuspended() {
        logger.d("onSuspended");
        isCommitAllowed = false;
    }

    @Override
    public void onDialogCreated(@NonNull AlertDialogFragment fragment, AlertDialog dialog) {
        eventsObservable.notifyDialogCreated(fragment, dialog);
    }

    @Override
    public void onDialogButtonClick(@NonNull AlertDialogFragment fragment, int which) {
        eventsObservable.notifyDialogButtonClick(fragment, which);
    }

    @Override
    public boolean onDialogKey(@NonNull AlertDialogFragment fragment, int keyCode, KeyEvent event) {
        return eventsObservable.notifyDialogKeyPressed(fragment, keyCode, event);
    }

    @Override
    public void onDialogCancel(@NonNull AlertDialogFragment fragment) {
        eventsObservable.notifyDialogCancel(fragment);
    }

    @Override
    public void onDialogDismiss(@NonNull AlertDialogFragment fragment) {
        eventsObservable.notifyDialogDismiss(fragment);
        activeAlerts.remove(fragment);
        targetAlertsToHide.remove(fragment.getTag());
        if (showRule == ShowRule.SINGLE && !targetAlertsToShow.isEmpty()) {
            // add first scheduled fragment, because of mode
            final Map.Entry<String, Pair<AlertDialogFragment, Boolean>> e = new ArrayList<>(targetAlertsToShow.entrySet()).get(0);
            final Pair<AlertDialogFragment, Boolean> p = e.getValue();
            if (p != null && p.first != null && p.second != null) {
                showAlert(e.getKey(), p.first, p.second);
            }
        }
    }

    public boolean isAnyAlertShowing() {
        return getShowingAlertsCount() > 0;
    }

    public synchronized boolean isAlertShowing(String tag) {
        return !TextUtils.isEmpty(tag) &&
                Predicate.Methods.contains(activeAlerts,
                        element -> element != null && element.isAdded() && tag.equals(element.getTag()));
    }

    public synchronized int getShowingAlertsCount() {
        return Predicate.Methods.filter(activeAlerts, element -> element != null && element.isAdded()).size();
    }

    @MainThread
    protected boolean showAlert(String tag, @NonNull AlertDialogFragment fragment) {
        return showAlert(tag, fragment, true);
    }

    /**
     * @param tag      new tag for adding for specified fragment
     * @param fragment created instance
     * @param reshow   if fragment for specified tag is already showing, it will be re-showed
     * @return true if successfully showed, false - otherwise (also when showing was scheduled)
     */
    @MainThread
    protected synchronized boolean showAlert(String tag, @NonNull AlertDialogFragment fragment, boolean reshow) {
        logger.d("showAlert: tag=" + tag + ", reshow=" + reshow);

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
     *   false - otherwise (also when showing was scheduled)
     * - {@linkplain AlertDialogFragment} instance non-null if was added to {@linkplain FragmentManager} before, false otherwise
     */
    @NonNull
    @MainThread
    protected synchronized Pair<Boolean, AlertDialogFragment> hideAlert(String tag) {
        logger.d("hideAlert: tag=" + tag);

        checkTag(tag);

        AlertDialogFragment fragment = Predicate.Methods.find(activeAlerts,
                element -> element != null && element.isAdded() && tag.equals(element.getTag()));

        boolean result = true;
        boolean isDismissed = fragment == null;

        if (fragment != null) {
            isDismissed = false;
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

    protected synchronized void restoreDialogsFromFragmentManager() {
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

    protected synchronized void handleTargetFragmentsToShow() {
        for (Map.Entry<String, Pair<AlertDialogFragment, Boolean>> e : new LinkedHashMap<>(targetAlertsToShow).entrySet()) {
            Pair<AlertDialogFragment, Boolean> p = e.getValue();
            if (p != null && p.first != null && p.second != null) {
                String tag = e.getKey();
                showAlert(tag, p.first, p.second);
            }
        }
    }

    protected synchronized void handleTargetFragmentsToHide() {
        for (String tag : new LinkedHashSet<>(targetAlertsToHide)) {
            hideAlert(tag);
        }
    }

    protected Class<? extends AlertDialogFragmentHolder> getLoggerClass() {
        return AlertDialogFragmentHolder.class;
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
