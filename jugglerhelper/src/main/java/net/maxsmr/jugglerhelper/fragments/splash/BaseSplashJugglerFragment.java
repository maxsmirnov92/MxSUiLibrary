package net.maxsmr.jugglerhelper.fragments.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import org.jetbrains.annotations.Nullable;
import android.view.View;

import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment;


public abstract class BaseSplashJugglerFragment extends BaseJugglerFragment {

    private static final String ARG_EXPIRED_TIME = BaseSplashJugglerFragment.class.getName() + ".ARG_EXPIRED_TIME";

    private final Handler navigateHandler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                onSplashTimeout();
            }
        }
    };

    private long expiredTime = 0;
    private long startTime = 0;

    protected abstract long getSplashTimeout();

    protected abstract boolean allowResetOnRootClick();

    protected abstract void onSplashTimeout();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expiredTime = savedInstanceState != null ? savedInstanceState.getLong(ARG_EXPIRED_TIME) : expiredTime;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (allowResetOnRootClick()) {

            View rootView = getView();

            if (rootView == null) {
                throw new RuntimeException("root view was not inflated");
            }

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateHandler.removeCallbacks(navigateRunnable);
                    onSplashTimeout();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        long timeout = getSplashTimeout();

        if (timeout < 0) {
            throw new IllegalArgumentException("incorrect splash timeout");
        }

        startTime = System.currentTimeMillis() - expiredTime;
        navigateHandler.postDelayed(navigateRunnable, timeout - expiredTime);
    }

    @Override
    public void onStop() {
        super.onStop();
        navigateHandler.removeCallbacks(navigateRunnable);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_EXPIRED_TIME, expiredTime = (startTime > 0 ? System.currentTimeMillis() - startTime : 0));
    }

}