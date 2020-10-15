package net.maxsmr.testapp.juggler

import android.app.Activity
import android.content.Context
import android.content.Intent
import me.ilich.juggler.states.State
import net.maxsmr.jugglerhelper.activities.BaseJugglerActivity
import net.maxsmr.testapp.juggler.main.MainCustomNavigationState
import net.maxsmr.testapp.juggler.main.MainNavigationState

class MainJugglerActivity : BaseJugglerActivity() {

    override fun createState(): State<*> {
        return MainNavigationState()
    }

    companion object {

        fun getIntent(context: Context, targetState: State<*>?): Intent {
            val intent = Intent(context, MainJugglerActivity::class.java)
            return addState(intent, targetState)
        }

        fun startWithState(
                context: Context,
                targetState: State<*>?,
                flags: Int = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK,
                requestCode: Int? = null
        ) {
            getIntent(context, targetState).apply {
                this.flags = flags
                if (context is Activity && requestCode != null) {
                    context.startActivityForResult(this, requestCode)
                } else {
                    context.startActivity(this)
                }
            }
        }

    }
}