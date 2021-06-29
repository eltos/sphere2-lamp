package com.github.eltos.sphere2lamp;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.Locale;
import java.util.Objects;

/**
 * Mapping of BLE characteristics to specific properties of the lamp 
 */
public final class Sphere2LampProperties {
    
    
    public static final String CHARACTERISTIC_ON_OFF =          "19B10001-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_BRIGHTNESS =      "19B10002-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_MODE =            "19B10010-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_BPM =             "19B10011-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_COLOR_PALETTE =   "19B10012-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_TIME_FUNCTION =   "19B10013-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_COLOR =           "19B10014-E8F2-517E-4F6C-D104768A1214";
    
    
    public static final MenuProperty.Menu[] MENU_MODE = new MenuProperty.Menu[]{
            new MenuProperty.Menu(  0, R.string.mode_manual),
            new MenuProperty.Menu(  1, R.string.mode_solid),
            new MenuProperty.Menu(  2, R.string.mode_anim_map),
            new MenuProperty.Menu(  3, R.string.mode_anim_solid),
            new MenuProperty.Menu( 15, R.string.mode_anim_rotating_gradient),
            new MenuProperty.Menu( 16, R.string.mode_anim_polar_gradient),
            new MenuProperty.Menu( 17, R.string.mode_anim_azimuth_gradient),
    };
    
    public static final MenuProperty.Menu[] MENU_COLOR_PALETTE = new MenuProperty.Menu[]{
            new MenuProperty.Menu(  0, R.string.palette_rainbow),
            new MenuProperty.Menu(  1, R.string.palette_red_stripe),
            new MenuProperty.Menu(  2, R.string.palette_party),
    };

    public static final MenuProperty.Menu[] MENU_TIME_FUNCTION = new MenuProperty.Menu[]{
            new MenuProperty.Menu(  0, R.string.time_function_sawtooth),
            new MenuProperty.Menu(  1, R.string.time_function_sawtooth_reverse),
            new MenuProperty.Menu(  2, R.string.time_function_triangular),
            new MenuProperty.Menu(  3, R.string.time_function_sinusoidal),
            new MenuProperty.Menu(  4, R.string.time_function_quadwave),
    };



    @NonNull
    public static Property<?> propertyFor(@NonNull BluetoothGattCharacteristic characteristic) {
        switch (characteristic.getUuid().toString().toUpperCase(Locale.ROOT)) {

            // Well known and supported characteristics

            case CHARACTERISTIC_ON_OFF:
                return new SwitchProperty(characteristic, 1, R.string.switch_on_off, R.layout.dashboard_card_switch);
            
            case CHARACTERISTIC_BRIGHTNESS:
                return new ByteProperty(characteristic, 2, R.string.brightness, R.layout.dashboard_card_slider);

            case CHARACTERISTIC_MODE:
                return new MenuProperty(characteristic, 3, R.string.mode, R.layout.dashboard_card_menu, MENU_MODE);
                
            case CHARACTERISTIC_BPM:
                return new ByteProperty(characteristic, 4, R.string.bpm, R.layout.dashboard_card_input_numeric);
                
            case CHARACTERISTIC_COLOR_PALETTE:
                return new MenuProperty(characteristic, 4, R.string.color_palette, R.layout.dashboard_card_menu, MENU_COLOR_PALETTE);

            case CHARACTERISTIC_TIME_FUNCTION:
                return new MenuProperty(characteristic, 4, R.string.time_function, R.layout.dashboard_card_menu, MENU_TIME_FUNCTION);

            case CHARACTERISTIC_COLOR:
                return new ColorProperty(characteristic, 5, R.string.color, R.layout.dashboard_card_color);
            


            default:
                // Generic placeholder for unknown characteristics
                return new RawProperty(characteristic, R.string.unknown);
        }
    }


    /**
     * Property class
     * decorates a characteristic with a name, type, etc.
     * and provides simplified, type-specific access to get and set the value
     */
    public static abstract class Property<T> {
        public BluetoothGattCharacteristic characteristic;
        public @StringRes int name;
        public @LayoutRes int layout;
        public int order;

        public Property(@NonNull BluetoothGattCharacteristic characteristic, @StringRes int name){
            this(characteristic, Integer.MAX_VALUE, name, R.layout.dashboard_card_raw);
        }

        public Property(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name, @LayoutRes int layout){
            this.characteristic = characteristic;
            this.name = name;
            this.layout = layout;
            this.order = order;
        }

        public void setBytes(@NonNull byte... values) {
            characteristic.setValue(values);
            StringBuilder sb = new StringBuilder(values.length * 2);
            for (byte b : values) sb.append(String.format("%02x ", b));
            Sphere2Lamp.writeProperty(this);
        }

        @NonNull
        public byte[] getBytes() {
            byte[] v = characteristic.getValue();
            return v == null ? new byte[0] : v;
        }

        @NonNull
        public Byte getByte(){
            byte[] value = getBytes();
            return value.length > 0 ? value[0] : 0;
        }

        public abstract void set(T value);

        @NonNull
        public abstract T get();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Property<?> property = (Property<?>) o;
            return Objects.equals(characteristic.getUuid(), property.characteristic.getUuid());
        }

        @Override
        public int hashCode() {
            return characteristic.getUuid().hashCode();
        }
    }

    public static class RawProperty extends Property<byte[]> {
        public RawProperty(@NonNull BluetoothGattCharacteristic characteristic, @StringRes int name) {
            super(characteristic, name);
        }
        public void set(byte[] value){
            setBytes(value);
        }
        @NonNull
        public byte[] get(){
            return getBytes();
        }
    }


    
    public static class ByteProperty extends Property<Byte> {
        public ByteProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name, @LayoutRes int layout) {
            super(characteristic, order, name, layout);
        }
        public void set(Byte value){
            setBytes(value);
        }
        @NonNull
        public Byte get(){
            return getByte();
        }
    }
    
    public static class SwitchProperty extends Property<Boolean> {
        public SwitchProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name, @LayoutRes int layout) {
            super(characteristic, order, name, layout);
        }
        public void set(Boolean on){
            setBytes((byte) (on ? 1 : 0));
        }
        @NonNull
        public Boolean get(){
            return getByte() > 0;
        }
    }
    
    public static class MenuProperty extends Property<MenuProperty.Menu> {
        public static class Menu {
            public final byte value;
            public final @StringRes int name;
            public Menu(final int value, final @StringRes int name) {
                this.value = (byte) value;
                this.name = name;
            }
        }
        public Menu[] choices;        
        public MenuProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name, @LayoutRes int layout, @NonNull Menu[] choices){
            super(characteristic, order, name, layout);
            this.choices = choices;            
        }
        public void set(Menu value){
            setBytes(value.value);
        }
        @NonNull
        public Menu get(){
            byte v = getByte();
            for (Menu choice : choices){
                if (choice.value == v) return choice;
            }
            return new Menu(0, R.string.unknown);
        }
    }

    public static class ColorProperty extends Property<Integer> {
        public ColorProperty(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name, @LayoutRes int layout) {
            super(characteristic, order, name, layout);
        }
        public void set(@ColorInt Integer color){
            byte r = (byte) ((color >> 16) & 0xFF),
                 g = (byte) ((color >> 8) & 0xFF),
                 b = (byte) (color & 0xFF);
            setBytes((byte) 0, r, g, b);
        }
        @NonNull
        @ColorInt
        public Integer get(){
            byte[] rgb = getBytes();
            if (rgb.length == 4) {
                int r = rgb[1] & 0xFF, g = rgb[2] & 0xFF, b = rgb[3] & 0xFF;
                return (0xFF << 24) + (r << 16) + (g << 8) + b;
            }
            return 0;
        }
    }
    
    
    
    
    
    
    
}