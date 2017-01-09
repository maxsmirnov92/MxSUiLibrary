package net.maxsmr.jugglerhelper.activities.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.jugglerhelper.fragments.base.BaseJugglerFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import me.ilich.juggler.Navigable;
import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.gui.JugglerFragment;
import me.ilich.juggler.states.State;
import ru.gokidgo.gui.juggler.Juggler2;


public class BaseJugglerActivity extends JugglerActivity {

    private static final Logger logger = LoggerFactory.getLogger(BaseJugglerActivity.class);

    private static final String STATE_JUGGLER = "state_juggler";
    private static final String EXTRA_STATE = "extra_state";

    public static Intent state(Context context, State<?> state, @Nullable Intent intent) {
        if (intent == null) {
            intent = new Intent(context, JugglerActivity.class);
        }
        intent.putExtra(EXTRA_STATE, state);
        return intent;
    }

    private boolean isCommitAllowed = true;

    private Juggler2 juggler2;
    private int animationFinishEnter;
    private int animationFinishExit;

    protected final boolean isCommitAllowed() {
        return isCommitAllowed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        animationFinishEnter = getIntent().getIntExtra(Juggler2.DATA_ANIMATION_FINISH_ENTER, 0);
        animationFinishExit = getIntent().getIntExtra(Juggler2.DATA_ANIMATION_FINISH_EXIT, 0);
        if (savedInstanceState == null) {
            juggler2 = createJuggler();
        } else {
            juggler2 = (Juggler2) savedInstanceState.getSerializable(STATE_JUGGLER);
            if (juggler2 == null) {
                throw new RuntimeException("savedInstanceState should contains Juggler instance");
            }
        }
        juggler2.setActivity(this);
        super.onCreate(savedInstanceState);
    }

    protected Juggler2 createJuggler() {
        return new Juggler2();
    }

    protected State createState() {
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isCommitAllowed = false;
        outState.putSerializable(STATE_JUGGLER, juggler2);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        isCommitAllowed = true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        juggler2.activateCurrentState();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        juggler2.onPostCreate(savedInstanceState);
    }

    @VisibleForTesting
    public Navigable navigateTo() {
        return juggler2;
    }

    @Override
    public void onBackPressed() {
        boolean b = juggler2.onBackPressed();
        if (!b) {
            b = juggler2.backState();
            if (!b) {
                finish();
                overridePendingTransition(animationFinishEnter, animationFinishExit);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        boolean b = juggler2.onUpPressed();
        if (!b) {
            b = juggler2.upState();
            if (!b) {
                b = super.onSupportNavigateUp();
                if (!b) {
                    finish();
                    overridePendingTransition(animationFinishEnter, animationFinishExit);
                }
            }
        }
        return b;
    }

    @VisibleForTesting
    public Juggler2 getJuggler() {
        return juggler2;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logger.debug("onActivityResult(), this=" + this + ", requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && !fragment.isDetached()) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        logger.debug("onTouchEvent(), event=" + event);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                    ((BaseJugglerFragment) f).onTouchEvent(event);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        logger.debug("onKeyDown(), keyCode=" + keyCode + ", event=" + event);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f instanceof BaseJugglerFragment && !f.isDetached()) {
                    ((BaseJugglerFragment) f).onKeyDown(keyCode, event);
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Nullable
    @SuppressWarnings("unchecked")
    public <F extends JugglerFragment> F findFragment(Class<F> fragmentClass) {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && !fragment.isDetached() && fragmentClass.isAssignableFrom(fragment.getClass())) {
                    return (F) fragment;
                }
            }
        }
        return null;
    }

    @Nullable
    public Fragment findFragmentByTag(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && !fragment.isDetached() && CompareUtils.stringsEqual(fragment.getTag(), tag, false)) {
                    return fragment;
                }
            }
        }
        return null;
    }

    @Nullable
    public Fragment findFragmentById(int id) {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && !fragment.isDetached() && CompareUtils.objectsEqual(fragment.getId(), id)) {
                    return fragment;
                }
            }
        }
        return null;
    }
}
