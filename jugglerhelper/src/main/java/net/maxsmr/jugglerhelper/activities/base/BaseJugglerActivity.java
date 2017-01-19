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
import net.maxsmr.jugglerhelper.juggler.Juggler2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import me.ilich.juggler.Navigable;
import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.gui.JugglerFragment;
import me.ilich.juggler.states.State;


public class BaseJugglerActivity extends JugglerActivity {

    private static final Logger logger = LoggerFactory.getLogger(BaseJugglerActivity.class);

    private boolean isCommitAllowed = true;

    protected final boolean isCommitAllowed() {
        return isCommitAllowed;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isCommitAllowed = false;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        isCommitAllowed = true;
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
                if (fragment != null && !fragment.isDetached() && fragment.getId() == id) {
                    return fragment;
                }
            }
        }
        return null;
    }
}
