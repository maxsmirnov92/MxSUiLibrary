package net.maxsmr.jugglerhelper.juggler;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;

import net.maxsmr.jugglerhelper.juggler.Juggler2;

import java.util.Stack;

import me.ilich.juggler.change.Add;
import me.ilich.juggler.change.Item;
import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.states.State;

@Deprecated
public class NewActivityResultAdd implements Add.Interface {

    private final State state;

    private final int requestCode;

    private final Class<? extends JugglerActivity> activityClass;

    @AnimRes
    private final int enterAnimationId;
    @AnimRes
    private final int exitAnimationId;

    public NewActivityResultAdd(State state, int requestCode) {
        this.state = state;
        this.requestCode = requestCode;
        this.activityClass = null;
        this.enterAnimationId = 0;
        this.exitAnimationId = 0;
    }

    public NewActivityResultAdd(State state, int requestCode, Class<? extends JugglerActivity> activityClass) {
        this.state = state;
        this.requestCode = requestCode;
        this.activityClass = activityClass;
        this.enterAnimationId = 0;
        this.exitAnimationId = 0;
    }

    public NewActivityResultAdd(State state, int requestCode, Class<? extends JugglerActivity> activityClass, @AnimRes int enterAnimationId, @AnimRes int exitAnimationId) {
        this.state = state;
        this.requestCode = requestCode;
        this.activityClass = activityClass;
        this.enterAnimationId = enterAnimationId;
        this.exitAnimationId = exitAnimationId;
    }

    @Override
    public Item add(JugglerActivity activity, Stack<Item> items, Juggler2.StateHolder currentStateHolder, Bundle bundle) {
        Intent intent = bundle.getParcelable(Juggler2.DATA_NEW_ACTIVITY_RESULT_INTENT);
        if (intent == null) {
            intent = new Intent();
        }
        if (activityClass == null) {
            intent.setComponent(new ComponentName(activity, JugglerActivity.class));
        } else {
            intent.setComponent(new ComponentName(activity, activityClass));
        }
        bundle.putParcelable(Juggler2.DATA_NEW_ACTIVITY_RESULT_INTENT, intent);
        bundle.putInt(Juggler2.DATA_NEW_ACTIVITY_RESULT_REQUEST_CODE, requestCode);
        bundle.putInt(Juggler2.DATA_ANIMATION_START_ENTER, enterAnimationId);
        bundle.putInt(Juggler2.DATA_ANIMATION_START_EXIT, exitAnimationId);
        JugglerActivity.state(activity, state, intent);
        return null;
    }

}
