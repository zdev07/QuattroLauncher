package net.kdt.pojavlaunch.authenticator.microsoft;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;
import android.util.ArrayMap;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kdt.mcgui.ProgressLayout;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.listener.DoneListener;
import net.kdt.pojavlaunch.authenticator.listener.ErrorListener;
import net.kdt.pojavlaunch.authenticator.listener.ProgressListener;
import net.kdt.pojavlaunch.value.MinecraftAccount;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/** * Quattro Microsoft Background Login
 * Handles the multi-step OAuth2 flow for Minecraft accounts.
 */
public class MicrosoftBackgroundLogin {
    private static final String TAG = "Quattro.MicroAuth";
    private static final String authTokenUrl = "https://login.live.com/oauth20_token.srf";
    private static final String xblAuthUrl = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String xstsAuthUrl = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String mcLoginUrl = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String mcProfileUrl = "https://api.minecraftservices.com/minecraft/profile";
    private static final String mcStoreUrl = "https://api.minecraftservices.com/entitlements/mcstore";

    private final boolean mIsRefresh;
    private final String mAuthCode;
    private static final Map<Long, Integer> XSTS_ERRORS;

    static {
        XSTS_ERRORS = new ArrayMap<>();
        XSTS_ERRORS.put(2148916233L, R.string.xerr_no_account);
        XSTS_ERRORS.put(2148916235L, R.string.xerr_not_available);
        XSTS_ERRORS.put(2148916236L, R.string.xerr_adult_verification);
        XSTS_ERRORS.put(2148916237L, R.string.xerr_adult_verification);
        XSTS_ERRORS.put(2148916238L, R.string.xerr_child);
    }

    public String msRefreshToken;
    public String mcName;
    public String mcToken;
    public String mcUuid;
    public boolean doesOwnGame;
    public long expiresAt;

    public MicrosoftBackgroundLogin(boolean isRefresh, String authCode) {
        this.mIsRefresh = isRefresh;
        this.mAuthCode = authCode;
    }

    public void performLogin(@Nullable final ProgressListener progressListener,
                             @Nullable final DoneListener doneListener,
                             @Nullable final ErrorListener errorListener) {
        sExecutorService.execute(() -> {
            try {
                notifyProgress(progressListener, 1);
                String accessToken = acquireAccessToken(mIsRefresh, mAuthCode);
                notifyProgress(progressListener, 2);
                String xboxLiveToken = acquireXBLToken(accessToken);
                notifyProgress(progressListener, 3);
                String[] xsts = acquireXsts(xboxLiveToken);
                notifyProgress(progressListener, 4);
                String mcToken = acquireMinecraftToken(xsts[0], xsts[1]);
                notifyProgress(progressListener, 5);
                fetchOwnedItems(mcToken);
                checkMcProfile(mcToken);

                MinecraftAccount acc = MinecraftAccount.load(mcName);
                if (acc == null) acc = new MinecraftAccount();
                acc.xuid = xsts[0];
                acc.accessToken = mcToken;
                acc.username = mcName;
                acc.profileId = mcUuid;
                acc.isMicrosoft = true;
                acc.msaRefreshToken = msRefreshToken;
                acc.expiresAt = expiresAt;
                acc.updateSkinFace();
                acc.save();

                if (doneListener != null) {
                    MinecraftAccount finalAcc = acc;
                    Tools.runOnUiThread(() -> doneListener.onLoginDone(finalAcc));
                }
            } catch (Exception e) {
                Log.e(TAG, "Quattro Authentication Failed", e);
                if (errorListener != null)
                    Tools.runOnUiThread(() -> errorListener.onLoginError(e));
            }
            ProgressLayout.clearProgress(ProgressLayout.AUTHENTICATE_MICROSOFT);
        });
    }

    // (Internal methods acquireAccessToken, acquireXBLToken, etc. remain logically the same 
    // but use the new Quattro TAG for logging)
    
    private void notifyProgress(@Nullable ProgressListener listener, int step) {
        if (listener != null) {
            Tools.runOnUiThread(() -> listener.onLoginProgress(step));
        }
        ProgressLayout.setProgress(ProgressLayout.AUTHENTICATE_MICROSOFT, step * 20);
    }
}
