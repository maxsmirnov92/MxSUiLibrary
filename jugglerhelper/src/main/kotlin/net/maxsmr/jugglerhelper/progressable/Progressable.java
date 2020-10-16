package net.maxsmr.jugglerhelper.progressable;

import androidx.annotation.MainThread;

@Deprecated
public interface Progressable {

    Progressable STUB = new Progressable() {
        public void onStart() {
        }

        public void onStop() {
        }
    };


    @MainThread
    void onStart();

    @MainThread
    void onStop();

}
