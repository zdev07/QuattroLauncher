package org.lwjgl.glfw;

import android.content.*;
import android.util.Log;
import android.view.Choreographer;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import net.kdt.pojavlaunch.*;
import net.kdt.pojavlaunch.customcontrols.gamepad.direct.DirectGamepadEnableHandler;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import dalvik.annotation.optimization.CriticalNative;

/**
 * Quattro Callback Bridge
 * Interfaces the Java UI with the Native Minecraft/JVM process.
 */
public class CallbackBridge {
    private static final String TAG = "Quattro.Bridge";
    public static final Choreographer sChoreographer = Choreographer.getInstance();
    private static boolean isGrabbing = false;
    private static final ArrayList<GrabListener> grabListeners = new ArrayList<>();
    private static @Nullable WeakReference<DirectGamepadEnableHandler> sDirectGamepadEnableHandler;
    
    public static final int CLIPBOARD_COPY = 2000;
    public static final int CLIPBOARD_PASTE = 2001;
    public static final int CLIPBOARD_OPEN = 2002;
    
    public static volatile int windowWidth, windowHeight;
    public static volatile int physicalWidth, physicalHeight;
    public static float mouseX, mouseY;
    public volatile static boolean holdingAlt, holdingCapslock, holdingCtrl, holdingNumlock, holdingShift;

    public static final ByteBuffer sGamepadButtonBuffer;
    public static final FloatBuffer sGamepadAxisBuffer;
    public static boolean sGamepadDirectInput = false;

    static {
        try {
            System.loadLibrary("pojavexec");
            Log.i(TAG, "Quattro Native Bridge initialized successfully.");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "CRITICAL: Quattro Native Bridge failed to load!", e);
        }
        sGamepadButtonBuffer = nativeCreateGamepadButtonBuffer();
        sGamepadAxisBuffer = createGamepadAxisBuffer();
    }

    public static void putMouseEventWithCoords(int button, float x, float y) {
        putMouseEventWithCoords(button, true, x, y);
        sChoreographer.postFrameCallbackDelayed(l -> putMouseEventWithCoords(button, false, x, y), 33);
    }
    
    public static void putMouseEventWithCoords(int button, boolean isDown, float x, float y) {
        sendCursorPos(x, y);
        sendMouseKeycode(button, CallbackBridge.getCurrentMods(), isDown);
    }

    public static void sendCursorPos(float x, float y) {
        mouseX = x; mouseY = y;
        nativeSendCursorPos(mouseX, mouseY);
    }

    public static void sendKeycode(int keycode, char keychar, int scancode, int modifiers, boolean isDown) {
        if(keycode != 0) nativeSendKey(keycode, scancode, isDown ? 1 : 0, modifiers);
        if(isDown && keychar != '\u0000') {
            nativeSendCharMods(keychar, modifiers);
            nativeSendChar(keychar);
        }
    }

    public static void sendMouseButton(int button, boolean status) {
        nativeSendMouseButton(button, status ? 1 : 0, CallbackBridge.getCurrentMods());
    }

    public static void sendScroll(double xoffset, double yoffset) {
        nativeSendScroll(xoffset, yoffset);
    }

    @SuppressWarnings("unused")
    @Keep
    public static @Nullable String accessAndroidClipboard(int type, String copy) {
        switch (type) {
            case CLIPBOARD_COPY:
                MainActivity.GLOBAL_CLIPBOARD.setPrimaryClip(ClipData.newPlainText("Quattro_Clipboard", copy));
                return null;
            case CLIPBOARD_PASTE:
                if (MainActivity.GLOBAL_CLIPBOARD.hasPrimaryClip()) {
                    return MainActivity.GLOBAL_CLIPBOARD.getPrimaryClip().getItemAt(0).getText().toString();
                }
                return "";
            case CLIPBOARD_OPEN:
                MainActivity.openLink(copy);
                return null;
            default: return null;
        }
    }

    public static int getCurrentMods() {
        int currMods = 0;
        if (holdingAlt) currMods |= LwjglGlfwKeycode.GLFW_MOD_ALT;
        if (holdingCapslock) currMods |= LwjglGlfwKeycode.GLFW_MOD_CAPS_LOCK;
        if (holdingCtrl) currMods |= LwjglGlfwKeycode.GLFW_MOD_CONTROL;
        if (holdingNumlock) currMods |= LwjglGlfwKeycode.GLFW_MOD_NUM_LOCK;
        if (holdingShift) currMods |= LwjglGlfwKeycode.GLFW_MOD_SHIFT;
        return currMods;
    }

    @Keep @CriticalNative public static native void nativeSetUseInputStackQueue(boolean useInputStackQueue);
    @Keep @CriticalNative private static native boolean nativeSendChar(char codepoint);
    @Keep @CriticalNative private static native boolean nativeSendCharMods(char codepoint, int mods);
    @Keep @CriticalNative private static native void nativeSendKey(int key, int scancode, int action, int mods);
    @Keep @CriticalNative private static native void nativeSendCursorPos(float x, float y);
    @Keep @CriticalNative private static native void nativeSendMouseButton(int button, int action, int mods);
    @Keep @CriticalNative private static native void nativeSendScroll(double xoffset, double yoffset);
    @Keep @CriticalNative private static native void nativeSendScreenSize(int width, int height);
    public static native void nativeSetWindowAttrib(int attrib, int value);
    private static native ByteBuffer nativeCreateGamepadButtonBuffer();
    private static native ByteBuffer nativeCreateGamepadAxisBuffer();

    public static FloatBuffer createGamepadAxisBuffer() {
        return nativeCreateGamepadAxisBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
    }
}
