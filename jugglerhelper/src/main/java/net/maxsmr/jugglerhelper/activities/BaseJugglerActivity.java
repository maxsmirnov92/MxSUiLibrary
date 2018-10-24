package net.maxsmr.jugglerhelper.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.MotionEvent;

import net.maxsmr.commonutils.logger.BaseLogger;
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder;
import net.maxsmr.jugglerhelper.fragments.BaseJugglerFragment;

import java.util.List;

import me.ilich.juggler.gui.JugglerActivity;


public class BaseJugglerActivity extends JugglerActivity {

    private static final BaseLogger logger = BaseLoggerHolder.getInstance().getLogger(BaseJugglerActivity.class);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logger.d("onActivityResult(), activity=" + getClass().getSimpleName() + ", requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);

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
        return BaseJugglerFragment.findFragmentById(getSupportFragmentManager(), id);
    }

    @Nullable
    public Fragment findFragmentByTag(String tag) {
        return BaseJugglerFragment.findFragmentByTag(getSupportFragmentManager(), tag);
    }

    @Nullable
    public <F extends Fragment> F findFragmentByClass(Class<F> clazz) {
        return BaseJugglerFragment.findFragmentByClass(getSupportFragmentManager(), clazz);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f != null && !f.isDetached()) {
                f.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

}
