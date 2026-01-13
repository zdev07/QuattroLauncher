package net.kdt.pojavlaunch.authenticator.listener;

import net.kdt.pojavlaunch.value.MinecraftAccount;

/** Called when the Quattro login is complete and the account is received. */
public interface DoneListener {
    void onLoginDone(MinecraftAccount account);
}

/** Called when there is a complete failure during Quattro authentication. */
public interface ErrorListener {
    void onLoginError(Throwable errorMessage);
}

/** Called when a Quattro login step is started. */
public interface ProgressListener {
    void onLoginProgress(int step);
}
