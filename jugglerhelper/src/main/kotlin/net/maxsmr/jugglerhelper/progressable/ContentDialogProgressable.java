package net.maxsmr.jugglerhelper.progressable;


import android.content.Context;
import android.view.View;

import androidx.annotation.IdRes;

@Deprecated
public class ContentDialogProgressable extends DialogProgressable {

    public interface IResultCallback {
        /**
         * @return true - if success, false - otherwise
         */
        boolean onResult();
    }

    private final IResultCallback callback;

    private final View data;
    private final View placeholder;

    public ContentDialogProgressable(Context context, View rootView, @IdRes int dataContainerResId, @IdRes int placeholderContainerResId, IResultCallback callback) {
        super(context);
        this.callback = callback;
        this.data = rootView.findViewById(dataContainerResId);
        this.placeholder = rootView.findViewById(placeholderContainerResId);
    }

    @Override
    public void onStart() {
        if (data != null) {
            data.setVisibility(View.GONE);
        }
        if (placeholder != null) {
            placeholder.setVisibility(View.GONE);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        boolean result = callback.onResult();
        if (data != null) {
            data.setVisibility(result ? View.VISIBLE : View.GONE);
        }
        if (placeholder != null) {
            placeholder.setVisibility(result ? View.GONE : View.VISIBLE);
        }
        super.onStop();
    }
}
