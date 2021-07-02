package com.github.eltos.sphere2lamp.properties;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.eltos.sphere2lamp.R;

/**
 * A property of unknown type
 */
public class RawProperty extends Property<byte[]> {

    public RawProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name) {
        super(characteristic, order, name, R.layout.dashboard_card_raw);
    }

    public void set(byte[] value) {
        setBytes(value);
    }

    @NonNull
    public byte[] get() {
        return getBytes();
    }
}
