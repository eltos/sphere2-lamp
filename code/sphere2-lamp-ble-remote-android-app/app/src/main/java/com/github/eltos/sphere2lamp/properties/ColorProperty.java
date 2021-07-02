package com.github.eltos.sphere2lamp.properties;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.eltos.sphere2lamp.R;

/**
 * A property representing a color with 4 bytes (0, r, g, b)
 */
public class ColorProperty extends Property<Integer> {

    public ColorProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name) {
        super(characteristic, order, name, R.layout.dashboard_card_color);
    }

    public void set(@ColorInt Integer color) {
        byte r = (byte) ((color >> 16) & 0xFF),
                g = (byte) ((color >> 8) & 0xFF),
                b = (byte) (color & 0xFF);
        setBytes((byte) 0, r, g, b);
    }

    @NonNull
    @ColorInt
    public Integer get() {
        byte[] rgb = getBytes();
        if (rgb.length == 4) {
            int r = rgb[1] & 0xFF, g = rgb[2] & 0xFF, b = rgb[3] & 0xFF;
            return (0xFF << 24) + (r << 16) + (g << 8) + b;
        }
        return 0;
    }
}
