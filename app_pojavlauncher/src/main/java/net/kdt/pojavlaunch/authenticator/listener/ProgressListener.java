package net.kdt.pojavlauncher.authenticator.listener;

public interface ProgressListener {
    void onProgress(String message, int progress, int max);
}
