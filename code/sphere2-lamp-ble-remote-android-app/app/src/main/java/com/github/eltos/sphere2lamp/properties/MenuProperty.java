package com.github.eltos.sphere2lamp.properties;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.eltos.sphere2lamp.R;

/**
 * A property representing a menu choice as a single byte
 */
public class MenuProperty extends Property<MenuProperty.Menu> {

    public static class Menu {
        public final byte value;
        public final @StringRes int name;
        public Menu(final int value, final @StringRes int name) {
            this.value = (byte) value;
            this.name = name;
        }
    }

    public final Menu[] choices;

    public MenuProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name, @NonNull Menu[] choices) {
        super(characteristic, order, name, R.layout.dashboard_card_menu);
        this.choices = choices;
    }

    public void set(Menu value) {
        setBytes(value.value);
    }

    @NonNull
    public Menu get() {
        int v = getByte() & 0xFF;
        for (Menu choice : choices) {
            if (choice.value == v) return choice;
        }
        return new Menu(0, R.string.unknown);
    }
}
