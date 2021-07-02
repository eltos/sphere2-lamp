package com.github.eltos.sphere2lamp.properties;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.eltos.sphere2lamp.R;

/**
 * A property representing a value as a single byte
 */
public class ByteValueProperty extends Property<Integer> {
    public ByteValueProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name, boolean asSlider) {
        super(characteristic, order, name, asSlider ? R.layout.dashboard_card_slider : R.layout.dashboard_card_input_numeric);
    }

    public void set(Integer value) {
        setBytes(value.byteValue());
    }

    @NonNull
    public Integer get() {
        return getByte();
    }
}
