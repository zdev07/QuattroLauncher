package net.kdt.pojavlaunch.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import net.kdt.pojavlaunch.R;

public class CustomSeekBarPreference extends Preference {
    private int mValue;
    private int mMax = 100;
    private String mSuffix = "";
    private TextView mValueText;

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Ensure you have a layout file named preference_custom_seekbar.xml in res/layout
        setLayoutResource(R.layout.preference_custom_seekbar);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        SeekBar seekBar = (SeekBar) holder.findViewById(R.id.seekbar_widget);
        mValueText = (TextView) holder.findViewById(android.R.id.summary);

        if (seekBar != null) {
            seekBar.setMax(mMax);
            seekBar.setProgress(mValue);
            updateValueText();

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mValue = progress;
                        updateValueText();
                        persistInt(mValue);
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    public void setValue(int value) {
        mValue = value;
        persistInt(value);
        notifyChanged();
    }

    public void setSuffix(String suffix) {
        mSuffix = suffix;
        updateValueText();
    }

    public void setMaxKeepIncrement(int max) {
        mMax = max;
        notifyChanged();
    }

    private void updateValueText() {
        if (mValueText != null) {
            mValueText.setText(mValue + mSuffix);
        }
        setSummary(mValue + mSuffix);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setValue(getPersistedInt(defaultValue != null ? (Integer) defaultValue : 0));
    }
}
