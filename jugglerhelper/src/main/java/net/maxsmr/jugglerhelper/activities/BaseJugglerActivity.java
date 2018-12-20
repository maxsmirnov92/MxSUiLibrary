package net.maxsmr.jugglerhelper.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;

import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment;
import net.maxsmr.jugglerhelper.fragments.FragmentFinder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.states.State;

import static me.ilich.juggler.Juggler.DATA_ANIMATION_FINISH_ENTER;
import static me.ilich.juggler.Juggler.DATA_ANIMATION_FINISH_EXIT;


public class BaseJugglerActivity extends JugglerActivity {

    private Bundle savedInstanceState;

    private boolean isCommitAllowed = false;

    private boolean isResumed = false;

    public boolean isCommitAllowed() {
        return isCommitAllowed;
    }

    public boolean isActivityResumed() {
        return isResumed;
    }

    public boolean isActivityDestroyed() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && !isDestroyed());
    }

    @AnimRes
    protected int getFinishEnterAnimataion() {
        return 0;
    }

    @AnimRes
    protected int getFinishExitAnimataion() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra(DATA_ANIMATION_FINISH_ENTER, getFinishEnterAnimataion());
        getIntent().putExtra(DATA_ANIMATION_FINISH_EXIT, getFinishExitAnimataion());
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        isCommitAllowed = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        savedInstanceState = outState;
        isCommitAllowed = false;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        isCommitAllowed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        logger.debug("onTouchEvent(), event=" + event);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                ((BaseJugglerFragment) f).onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        logger.debug("onKeyDown(), keyCode=" + keyCode + ", event=" + event);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                ((BaseJugglerFragment) f).onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Nullable
    public Fragment findFragmentById(int id) {
        Pair<Integer, Fragment> fragment = FragmentFinder.findFragmentById(getSupportFragmentManager(), id);
        if (fragment != null) {
            return fragment.second;
        }
        return null;
    }

    @Nullable
    public Fragment findFragmentByTag(String tag) {
        Pair<Integer, Fragment> fragment = FragmentFinder.findFragmentByTag(getSupportFragmentManager(), tag);
        if (fragment != null) {
            return fragment.second;
        }
        return null;
    }

    @Nullable
    public <F extends Fragment> F findFragmentByClass(Class<F> clazz) {
        Pair<Integer, F> fragment = FragmentFinder.findFragmentByClass(getSupportFragmentManager(), clazz);
        if (fragment != null) {
            return fragment.second;
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f != null && !f.isDetached()) {
                f.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Nullable
    public State getActivityState() {
        final State<?> state;
        if (getIntent().hasExtra(EXTRA_STATE)) {
            state = (State<?>) getIntent().getSerializableExtra(EXTRA_STATE);
        } else {
            state = createState();
        }
        return state;
    }
}
