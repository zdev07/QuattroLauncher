package net.kdt.pojavlaunch.colorselector;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.Nullable;
import com.kdt.SideDialogView;
import net.kdt.pojavlaunch.R;

/**
 * Quattro Color Selector Dialog
 */
public class ColorSelector extends SideDialogView implements HueSelectionListener, RectangleSelectionListener, AlphaSelectionListener, TextWatcher {
    private static final int ALPHA_MASK = ~(0xFF << 24);
    private HueView mHueView;
    private SVRectangleView mLuminosityIntensityView;
    private AlphaView mAlphaView;
    private ColorSideBySideView mColorView;
    private EditText mTextView;

    private ColorSelectionListener mColorSelectionListener;
    private final float[] mHueTemplate = new float[]{0, 1, 1};
    private final float[] mHsvSelected = new float[]{360, 1, 1};
    private int mAlphaSelected = 0xff;
    private ColorStateList mTextColors;
    private boolean mWatch = true;
    private boolean mAlphaEnabled = true;

    public ColorSelector(Context context, ViewGroup parent, @Nullable ColorSelectionListener colorSelectionListener) {
        super(context, parent, R.layout.dialog_color_selector);
        this.mColorSelectionListener = colorSelectionListener;
    }

    @Override
    protected void onInflate() {
        super.onInflate();
        mHueView = mDialogContent.findViewById(R.id.color_selector_hue_view);
        mLuminosityIntensityView = mDialogContent.findViewById(R.id.color_selector_rectangle_view);
        mAlphaView = mDialogContent.findViewById(R.id.color_selector_alpha_view);
        mColorView = mDialogContent.findViewById(R.id.color_selector_color_view);
        mTextView = mDialogContent.findViewById(R.id.color_selector_hex_edit);
        
        runColor(Color.RED);
        mHueView.setHueSelectionListener(this);
        mLuminosityIntensityView.setRectSelectionListener(this);
        mAlphaView.setAlphaSelectionListener(this);
        mTextView.addTextChangedListener(this);
        mTextColors = mTextView.getTextColors();
        
        mAlphaView.setVisibility(mAlphaEnabled ? View.VISIBLE : View.GONE);

        // Styling for Quattro Side Dialog
        View contentParent = mDialogContent.findViewById(R.id.side_dialog_scrollview);
        if (contentParent != null) {
            ViewGroup dialogLayout = (ViewGroup) mDialogContent.getParent();
            dialogLayout.setElevation(11);
            dialogLayout.setTranslationZ(11);
        }
    }

    public void show(boolean fromRight, int previousColor) {
        appear(fromRight);
        runColor(previousColor);
        dispatchColorChange();
    }

    @Override
    public void onHueSelected(float hue) {
        mHsvSelected[0] = mHueTemplate[0] = hue;
        mLuminosityIntensityView.setColor(Color.HSVToColor(mHueTemplate), true);
        dispatchColorChange();
    }

    protected void dispatchColorChange() {
        int color = Color.HSVToColor(mAlphaSelected, mHsvSelected);
        mColorView.setColor(color);
        mWatch = false;
        mTextView.setText(String.format("%08X", color));
        if (mColorSelectionListener != null) mColorSelectionListener.onColorSelected(color);
    }

    public void setAlphaEnabled(boolean alphaEnabled) {
        this.mAlphaEnabled = alphaEnabled;
        if (mAlphaView != null) {
            mAlphaView.setVisibility(alphaEnabled ? View.VISIBLE : View.GONE);
            mAlphaView.setAlpha(255);
        }
    }
}
