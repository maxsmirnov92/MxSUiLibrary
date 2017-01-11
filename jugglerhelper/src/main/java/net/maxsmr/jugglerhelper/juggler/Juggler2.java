package net.maxsmr.jugglerhelper.juggler;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import net.maxsmr.jugglerhelper.activities.base.BaseJugglerActivity;

import me.ilich.juggler.Juggler;
import me.ilich.juggler.Log;
import me.ilich.juggler.Transition;
import me.ilich.juggler.change.Add;
import me.ilich.juggler.change.Item;
import me.ilich.juggler.change.NewActivityAdd;
import me.ilich.juggler.change.Remove;
import me.ilich.juggler.change.StateChanger;
import me.ilich.juggler.grid.Cell;
import me.ilich.juggler.gui.JugglerActivity;
import me.ilich.juggler.states.State;


// TODO fix Juggler
public class Juggler2 extends Juggler {

    private static final int NOT_SET = -1;

    public static final String DATA_NEW_ACTIVITY_RESULT_INTENT = "new_activity_result_intent";
    public static final String DATA_NEW_ACTIVITY_RESULT_REQUEST_CODE = "new_activity_result_request_code";

    private StateChanger stateChanger = new StateChanger();
    private me.ilich.juggler.Juggler.StateHolder currentStateHolder = new me.ilich.juggler.Juggler.StateHolder();
    @LayoutRes
    private int layoutId = NOT_SET;
    private transient BaseJugglerActivity activity = null;
    private transient FragmentManager.OnBackStackChangedListener onBackStackChangedListener = null;

    @Override
    public boolean backState() {
        if (activity == null) {
            throw new NullPointerException("activity == null");
        }
        final boolean b;
        State state = currentStateHolder.get();
        if (state == null) {
            b = false;
        } else {
            Transition transition = state.getBackTransition();
            if (transition == null) {
                b = false;
            } else {
                State st = transition.execute(activity, stateChanger);
                currentStateHolder.set(st);
                b = currentStateHolder.get() != null;
            }
        }
        return b;
    }

    @Override
    public boolean upState() {
        if (activity == null) {
            throw new NullPointerException("activity == null");
        }
        final boolean b;
        State state = currentStateHolder.get();
        if (state == null) {
            b = false;
        } else {
            Transition transition = state.getUpTransition();
            if (transition == null) {
                b = false;
            } else {
                state = transition.execute(activity, stateChanger);
                currentStateHolder.set(state);
                b = state != null;
            }
        }
        return b;
    }

    @Override
    public void restore() {
        currentStateHolder.set(stateChanger.restore(activity));
    }

    public void activateCurrentState() {
        State state = currentStateHolder.get();
        if (state != null) {
            state.onActivate(activity);
        }
    }

    @Override
    public void state(@Nullable Remove.Interface remove) {
        doState(remove, null);
    }

    @Override
    public void state(@Nullable Add.Interface add) {
        doState(null, add);
    }

    @Override
    public void state(@Nullable Remove.Interface remove, @Nullable Add.Interface add) {
        doState(remove, add);
    }

    private void doState(@Nullable Remove.Interface remove, @Nullable Add.Interface add) {
        Bundle bundle = new Bundle();
        State state = currentStateHolder.get();
        if (state != null) {
            state.onDeactivate(activity);
        }
        if (remove != null) {
            remove.remove(activity, stateChanger.getItems(), currentStateHolder, bundle);
        }
        if (add != null) {
            add.add(activity, stateChanger.getItems(), currentStateHolder, bundle);
        }
        Intent newActivityIntent = bundle.getParcelable(DATA_NEW_ACTIVITY_INTENT);
        Intent newActivityResultIntent = bundle.getParcelable(DATA_NEW_ACTIVITY_RESULT_INTENT);
        int enterAnimation = bundle.getInt(DATA_ANIMATION_START_ENTER, 0);
        int exitAnimation = bundle.getInt(DATA_ANIMATION_START_EXIT, 0);
        int finishEnterAnimation = bundle.getInt(DATA_ANIMATION_FINISH_ENTER, 0);
        int finishExitAnimation = bundle.getInt(DATA_ANIMATION_FINISH_EXIT, 0);
        if (newActivityIntent != null) {
            newActivityIntent.putExtra(DATA_ANIMATION_FINISH_ENTER, finishEnterAnimation);
            newActivityIntent.putExtra(DATA_ANIMATION_FINISH_EXIT, finishExitAnimation);
            activity.startActivity(newActivityIntent);
            activity.overridePendingTransition(enterAnimation, exitAnimation);
        } else if (newActivityResultIntent != null) {
            int requestCode = bundle.getInt(DATA_NEW_ACTIVITY_RESULT_REQUEST_CODE, -1);
            if (requestCode == -1) {
                throw new IllegalStateException(DATA_NEW_ACTIVITY_RESULT_INTENT + " is specified, but request code is not");
            }
            newActivityResultIntent.putExtra(DATA_ANIMATION_FINISH_ENTER, finishEnterAnimation);
            newActivityResultIntent.putExtra(DATA_ANIMATION_FINISH_EXIT, finishExitAnimation);
            activity.startActivityForResult(newActivityResultIntent, requestCode);
        }
        boolean closeCurrentActivity = bundle.getBoolean(DATA_CLOSE_CURRENT_ACTIVITY, false);
        if (closeCurrentActivity) {
            activity.finish();
            activity.overridePendingTransition(finishEnterAnimation, finishExitAnimation);
        }
    }

    public void setActivity(BaseJugglerActivity activity) {
        if (activity != null) {
            activity.getSupportFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);
        }
        this.activity = activity;
        onBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                State state = currentStateHolder.get();
                if (state != null) {
                    state.onActivate(Juggler2.this.activity);
                }
            }
        };
        this.activity.getSupportFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);
    }

    public void onPostCreate(Bundle savedInstanceState) {
        State state = currentStateHolder.get();
        if (state != null) {
            state.onPostCreate(activity, savedInstanceState);
        }
    }

    /**
     * @return true if current state process back press
     * false if not
     */
    @Override
    public boolean onBackPressed() {
        final boolean b;
        State state = currentStateHolder.get();
        b = state != null && state.onBackPressed(activity);
        return b;
    }

    @Override
    public boolean onUpPressed() {
        final boolean b;
        State state = currentStateHolder.get();
        b = state != null && state.onUpPressed(activity);
        return b;
    }

    @VisibleForTesting
    public int getStackLength() {
        return stateChanger.getStackLength();
    }

    public boolean hasLayoutId() {
        return layoutId != NOT_SET;
    }

    @LayoutRes
    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
    }

    public void dump() {
        Log.v(this, "*** begin Juggler dump ***");
        int backstackSize = activity.getSupportFragmentManager().getBackStackEntryCount();
        Log.v(this, "activity = " + activity);
        Log.v(this, "backstack size = " + backstackSize);
        for (int i = 0; i < backstackSize; i++) {
            FragmentManager.BackStackEntry backStackEntry = activity.getSupportFragmentManager().getBackStackEntryAt(i);
            Log.v(this, i + ") " + backStackEntry.getId() + " " + backStackEntry.getName() + " " + backStackEntry);
        }
        Log.v(this, "stack size = " + stateChanger.getItems().size());
        for (int i = 0; i < stateChanger.getItems().size(); i++) {
            Item item = stateChanger.getItems().get(i);
            Log.v(this, i + ") " + item);
        }
        State state = currentStateHolder.get();
        Log.v(this, "currentState = " + state);
        if (state != null) {
            Log.v(this, "grid size = " + state.getGrid().getCells().size());
            for (int i = 0; i < state.getGrid().getCells().size(); i++) {
                Cell cell = state.getGrid().getCells().get(i);
                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(cell.getContainerId());
                Log.v(this, i + ") " + cell.getContainerId() + " " + cell.getType() + " " + fragment);
            }
        }
        Log.v(this, "*** end Juggler dump ***");
    }


    public static Add.Interface newActivity(State state) {
        return new NewActivityAdd(state);
    }

    public static Add.Interface newActivity(State state, Class<? extends JugglerActivity> activityClass) {
        return new NewActivityAdd(state, activityClass);
    }

    public static Add.Interface newActivity(State state, Class<? extends JugglerActivity> activityClass, @AnimRes int enterAnimationId, @AnimRes int exitAnimationId) {
        return new NewActivityAdd(state, activityClass, enterAnimationId, exitAnimationId);
    }
}
