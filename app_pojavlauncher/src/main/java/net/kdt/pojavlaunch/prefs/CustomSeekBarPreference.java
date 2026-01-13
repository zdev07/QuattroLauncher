package net.kdt.pojavlaunch.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import net.kdt.pojavlaunch.R;

/** Full Rewrite: Quattro Styled Seekbar for Settings */
public class CustomSeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
    private int mValue;
    private int mMin = 0;
    private int mMax = 100;
    private int mIncrement = 1;
    private String mUnit = "";
    private TextView mValueText;

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_custom_seekbar); // Ensure this layout exists in Quattro resources
        
        if (attrs != null) {
            mMin = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "minValue", 0);
            mMax = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "maxValue", 100);
            mIncrement = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "increment", 1);
            mUnit = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "unit");
            if (mUnit == null) mUnit = "";
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mValueText = (TextView) holder.findViewById(R.id.seekbar_value);
        SeekBar seekBar = (SeekBar) holder.findViewById(R.id.seekbar_widget);
        
        seekBar.setMax((mMax - mMin) / mIncrement);
        seekBar.setProgress((mValue - mMin) / mIncrement);
        seekBar.setOnSeekBarChangeListener(this);
        updateLabel();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mValue = mMin + (progress * mIncrement);
            updateLabel();
            persistInt(mValue);
        }
    }

    private void updateLabel() {
        if (mValueText != null) {
            mValueText.setText(mValue + mUnit);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, mMin);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        mValue = getPersistedInt(defaultValue != null ? (Integer) defaultValue : mMin);
    }

    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
}
