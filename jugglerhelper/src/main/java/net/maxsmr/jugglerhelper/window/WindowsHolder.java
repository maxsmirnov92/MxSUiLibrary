package net.maxsmr.jugglerhelper.window;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.commonutils.data.Predicate;
import net.maxsmr.commonutils.logger.BaseLogger;
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder;

import java.util.*;

/**
 * Class for holding added/removed [View] to/from [WindowManager]
 * Top view is first added
 */
public class WindowsHolder {

    private final BaseLogger logger = BaseLoggerHolder.getInstance().getLogger(getLoggerClass());

    private final WindowManager windowManager;

    @NonNull
    private final ViewFactory viewFactory;


    /**
     * tags to use in this helper
     */
    @NonNull
    private final Set<String> tags = new LinkedHashSet<>();

    /**
     * active views, added to {@linkplain android.view.WindowManager]
     * note that Set may be not actual (for example, if removing/adding directly to manager in other place)
     * so invalidate manually by [AlertDialogFragmentsHolder.hideAlert]
     */
    @NonNull
    private final Set<View> activeViews = new LinkedHashSet<>();

    @Nullable
    private ViewGroup.LayoutParams defaultLayoutParams;


    public WindowsHolder(@NonNull Context context, @NonNull ViewFactory viewFactory, @Nullable Collection<String> tags) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.viewFactory = viewFactory;
        if (windowManager == null) {
            throw new RuntimeException(WindowManager.class.getSimpleName() + " is null");
        }
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    @Nullable
    public ViewGroup.LayoutParams getDefaultLayoutParams() {
        return defaultLayoutParams;
    }

    public void setDefaultLayoutParams(@Nullable ViewGroup.LayoutParams defaultLayoutParams) {
        this.defaultLayoutParams = defaultLayoutParams;
    }

    public boolean isAnyViewAdded() {
        return getAddedViewsCount() > 0;
    }

    public int getAddedViewsCount() {
        return activeViews.size();
    }

    public boolean isViewAdded(String tag) {
        return getAddedViewByTag((tag)) != null;
    }

    @SuppressWarnings("unchecked")
    public <V extends View> V getAddedViewByTag(String tag) {
        checkTag(tag);
        return (V) Predicate.Methods.find(activeViews, v -> CompareUtils.objectsEqual(tag, v.getTag()));
    }

    @MainThread
    protected boolean addView(String tag) {
        return addView(tag, true);
    }

    @MainThread
    protected boolean addView(String tag, boolean reAdd) {
        if (defaultLayoutParams == null) {
            throw new IllegalStateException("Default layout params was not specified");
        }
        return addView(tag, defaultLayoutParams, reAdd);
    }


    @MainThread
    protected boolean addView(String tag, ViewGroup.LayoutParams layoutParams) {
        return addView(tag, layoutParams, true);
    }

    /**
     * @param tag   new tag for adding for specified view
     * @param reAdd if view for specified tag is already showing, it will be re-showed
     * @return true if successfully showed, false - otherwise (also when showing was scheduled)
     */
    @MainThread
    protected boolean addView(String tag, ViewGroup.LayoutParams layoutParams, boolean reAdd) {
        logger.d("addView: tag=" + tag + ", layoutParams=" + layoutParams + ", reAdd=" + reAdd);

        checkTag(tag);

        if (!reAdd && isViewAdded(tag)) {
            return false;
        }

        if (!removeView(tag).first) {
            return false;
        }

        final View view = viewFactory.createViewByTag(tag);
        view.setTag(tag);

        try {
            windowManager.addView(view, layoutParams);
        } catch (Exception e){
            logger.e("An Exception occurred during addView(): " + e.getMessage(), e);
            return false;
        }

        activeViews.add(view);
        return true;
    }


    /**
     * @return Pair:
     * - true if view for specified tag was successfully hided or already not showing,
     * false - otherwise
     * - {@linkplain View} instance non-null if was added to [WindowManager] before, false otherwise
     */
    @MainThread
    @NonNull
    protected Pair<Boolean, View> removeView(String tag) {
        logger.d("removeView: tag=" + tag);

        final View view = getAddedViewByTag(tag);

        boolean result = true;
        boolean isRemoved = view == null;

        if (view != null) {
            try {
                windowManager.removeView(view);
            } catch (Exception e){
                logger.e("An Exception occurred during removeView(): " + e.getMessage(), e);
                result = false;
            }

            isRemoved = result;
        }

        if (isRemoved) {
            Iterator<View> it = activeViews.iterator();
            while (it.hasNext()) {
                final View v = it.next();
                if (CompareUtils.objectsEqual(tag, v.getTag())) {
                    it.remove();
                }
            }
        }

        return new Pair<>(result, view);
    }

    protected Class<? extends WindowsHolder> getLoggerClass() {
        return getClass();
    }

    private void checkTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("Tag must be non-empty");
        }
        if (!tags.isEmpty() && !tags.contains(tag)) {
            throw new IllegalArgumentException("Tag " + tag + " is not declared in holder");
        }
    }

    interface ViewFactory {

        View createViewByTag(String tag);
    }

}
