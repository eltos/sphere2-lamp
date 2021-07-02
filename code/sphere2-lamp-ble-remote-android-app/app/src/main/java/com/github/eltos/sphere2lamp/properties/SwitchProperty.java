package com.github.eltos.sphere2lamp.properties;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;

import com.github.eltos.sphere2lamp.R;

/**
 * A property that can be switched on (1) and off (0)
 */
public class SwitchProperty extends Property<Boolean> {

    protected SwitchProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, int name) {
        super(characteristic, order, name, R.layout.dashboard_card_switch);
    }

    public void set(Boolean on) {
        setBytes((byte) (on ? 1 : 0));
    }

    @NonNull
    public Boolean get() {
        return getByte() > 0;
    }
}
