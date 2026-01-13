package com.kdt.mcgui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.res.ResourcesCompat;

import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.listener.DoneListener;
import net.kdt.pojavlaunch.authenticator.listener.ErrorListener;
import net.kdt.pojavlaunch.authenticator.listener.ProgressListener;
import net.kdt.pojavlaunch.authenticator.microsoft.MicrosoftBackgroundLogin;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.extra.ExtraListener;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.spse.extended_view.ExtendedTextView;

/**
 * Quattro Account Selector
 */
public class mcAccountSpinner extends AppCompatSpinner implements AdapterView.OnItemSelectedListener {
    private final List<String> mAccountList = new ArrayList<>(2);
    private MinecraftAccount mSelectecAccount = null;
    private final Paint mLoginBarPaint = new Paint();
    private float mLoginBarWidth = -1;
    private int mLoginStep = 0;

    public mcAccountSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(getResources().getColor(R.color.background_status_bar));
        mLoginBarPaint.setColor(getResources().getColor(R.color.minebutton_color));
        mLoginBarPaint.setStrokeWidth(getResources().getDimensionPixelOffset(R.dimen._2sdp));

        reloadAccounts(true, 0);
        setOnItemSelectedListener(this);

        ExtraCore.addExtraListener(ExtraConstants.MOJANG_LOGIN_TODO, (key, value) -> {
            if(value[1].isEmpty()) {
                MinecraftAccount account = new MinecraftAccount();
                account.username = value[0];
                try { account.save(); } catch (IOException e) { Log.e("Quattro", "Save failed", e); }
                onLoginDone(account);
            }
            return false;
        });

        ExtraCore.addExtraListener(ExtraConstants.MICROSOFT_LOGIN_TODO, (key, value) -> {
            new MicrosoftBackgroundLogin(false, value.getQueryParameter("code"))
                .performLogin(step -> mLoginStep = step, this::onLoginDone, this::onLoginError);
            return false;
        });
    }

    private void onLoginDone(MinecraftAccount account) {
        Toast.makeText(getContext(), "Quattro: Logged in as " + account.username, Toast.LENGTH_SHORT).show();
        if (!mAccountList.contains(account.username)) {
            mAccountList.add(account.username);
        }
        reloadAccounts(false, mAccountList.indexOf(account.username));
    }

    private void onLoginError(Object error) {
        Tools.showError(getContext(), "Quattro Login Error: " + error.toString());
    }

    private void reloadAccounts(boolean fromFiles, int overridePosition) {
        if (fromFiles) {
            mAccountList.clear();
            mAccountList.add("Add Quattro Account");
            File accountFolder = new File(Tools.DIR_ACCOUNT_NEW);
            if (accountFolder.exists() && accountFolder.list() != null) {
                for (String fileName : accountFolder.list()) {
                    mAccountList.add(fileName.replace(".json", ""));
                }
            }
        }
        
        ArrayAdapter<String> adapter = new AccountAdapter(getContext(), R.layout.item_minecraft_account, mAccountList.toArray(new String[0]));
        setAdapter(adapter);
        setSelection(Math.max(0, overridePosition));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
        } else {
            mSelectecAccount = PojavProfile.getCurrentProfileContent(getContext(), mAccountList.get(position));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private class AccountAdapter extends ArrayAdapter<String> {
        public AccountAdapter(Context ctx, int res, String[] items) { super(ctx, res, items); }

        @NonNull @Override
        public View getView(int pos, View conv, @NonNull ViewGroup parent) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.item_minecraft_account, parent, false);
            ExtendedTextView tv = v.findViewById(R.id.account_item);
            tv.setText(getItem(pos));
            v.findViewById(R.id.delete_account_button).setVisibility(GONE);
            return v;
        }
    }
}
