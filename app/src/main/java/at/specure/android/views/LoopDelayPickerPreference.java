package at.specure.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import com.specure.opennettest.R;

import androidx.preference.PreferenceViewHolder;
import at.specure.android.screens.preferences.preferences.ExtendedDialogPreferenceCompat;

/**
 * Created by michal.cadrik on 1/16/2018.
 */

public class LoopDelayPickerPreference extends ExtendedDialogPreferenceCompat {
    private NumberPicker mPicker;
    private int defaultValue = getContext().getResources().getInteger(R.integer.default_loop_min_delay);
    private int mNumber;

    public LoopDelayPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // required for use in XMLs
    }


    public LoopDelayPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWidgetLayoutResource(R.layout.pref_number_picker_widget);
        setDialogLayoutResource(R.layout.pref_number_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setNeedInputMethod(true);
    }

    @Override
    protected void onInitPreferenceView(PreferenceViewHolder viewHolder) {

    }

    @Override
    protected void onDialogOpened(View rootView) {
        mPicker = rootView.findViewById(R.id.numberPicker);
        mPicker.setMinValue(getContext().getResources().getInteger(R.integer.loop_min_delay));
        mPicker.setMaxValue(3000000);
        mPicker.setValue(getValue());

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            setValue(mPicker.getValue());
        }
    }

    public int getValue() {
        return getPersistedInt(defaultValue);
    }



    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mNumber) : (Integer) defaultValue);
    }


    public void setValue(int value) {

        if (shouldPersist()) {
            persistInt(value);
        }

        if (value != mNumber) {
            mNumber = value;
            notifyChanged();
        }
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return super.onGetDefaultValue(a, getContext().getResources().getInteger(R.integer.default_loop_min_delay));
    }


}