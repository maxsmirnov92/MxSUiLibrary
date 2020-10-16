package net.maxsmr.jugglerhelper.fragments.toolbar

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes

abstract class BaseCustomJugglerToolbarFragment : BaseJugglerToolbarFragment() {

    @IdRes
    protected open val toolbarTitleId: Int = 0

    @IdRes
    protected open val toolbarLogoId: Int = 0

    override fun applyTitle(title: CharSequence?) {
        with(toolbarTitleId) {
            val titleView = if (this != 0) view?.findViewById<TextView>(this) else null
            if (titleView != null) {
                super.applyTitle("")
                titleView.text = title
                titleView.visibility = if (TextUtils.isEmpty(title)) View.GONE else View.VISIBLE
            } else {
                super.applyTitle(title)
            }
        }
    }

    override fun applyLogo(logo: Drawable?) {
        with(toolbarLogoId) {
            val logoView = if (this != 0) view?.findViewById<ImageView>(this) else null
            if (logoView != null) {
                super.applyLogo(null)
                logoView.setImageDrawable(logo)
                logoView.visibility = if (logo == null) View.GONE else View.VISIBLE
            } else {
                super.applyLogo(logo)
            }
        }
    }
}
