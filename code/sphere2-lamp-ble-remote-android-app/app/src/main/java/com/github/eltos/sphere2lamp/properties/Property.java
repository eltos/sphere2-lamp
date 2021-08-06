package com.github.eltos.sphere2lamp.properties;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.eltos.sphere2lamp.R;
import com.github.eltos.sphere2lamp.Sphere2Lamp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Property class decorates a characteristic with a name, type, layout etc.
 * The subclasses provides simplified, type-specific access to get and set the value
 */
public abstract class Property<T> {

    public static final String CHARACTERISTIC_ON_OFF =          "19B10001-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_BRIGHTNESS =      "19B10002-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_DEMO =            "19B10003-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_MODE =            "19B10010-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_BPM =             "19B10011-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_COLOR_PALETTE =   "19B10012-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_TIME_FUNCTION =   "19B10013-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_COLOR =           "19B10014-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_LED_MAP =         "19B10015-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_SET_LED =         "19B10101-E8F2-517E-4F6C-D104768A1214";
    public static final String CHARACTERISTIC_SET_ALL =         "19B10102-E8F2-517E-4F6C-D104768A1214";

    public static final int MODE_MANUAL = 0;
    public static final int MODE_SOLID = 1;
    public static final int MODE_ANIM_MAP = 2;
    public static final int MODE_ANIM_SOLID = 3;
    public static final int MODE_ANIM_ROTATING_GRADIENT = 15;
    public static final int MODE_ANIM_POLAR_GRADIENT = 16;
    public static final int MODE_ANIM_AZIMUTH_GRADIENT = 17;
    public static final int MODE_ANIM_ROTATING_SEARCHLIGHT = 18;
    public static final int MODE_ANIM_ROTATING_SEARCHLIGHT_PALETTE = 19;
    public static final int MODE_ANIM_TWINKLE = 21;
    public static final int MODE_ANIM_GLITTER = 22;
    public static final int MODE_ANIM_SPRINKLE = 23;
    public static final int MODE_ANIM_SPOTLIGHTS = 24;
    public static final int MODE_ANIM_SPOTLIGHTS_PALETTE = 25;
    public static final int MODE_ANIM_POLICE = 26;



    public static final MenuProperty.Menu[] MENU_MODE = new MenuProperty.Menu[]{
            new MenuProperty.Menu(MODE_MANUAL,                              R.string.mode_manual),
            new MenuProperty.Menu(MODE_SOLID,                               R.string.mode_solid),
            new MenuProperty.Menu(MODE_ANIM_SOLID,                          R.string.mode_anim_solid),
            new MenuProperty.Menu(MODE_ANIM_MAP,                            R.string.mode_anim_map),
            new MenuProperty.Menu(MODE_ANIM_ROTATING_GRADIENT,              R.string.mode_anim_rotating_gradient),
            new MenuProperty.Menu(MODE_ANIM_POLAR_GRADIENT,                 R.string.mode_anim_polar_gradient),
            new MenuProperty.Menu(MODE_ANIM_AZIMUTH_GRADIENT,               R.string.mode_anim_azimuth_gradient),
            new MenuProperty.Menu(MODE_ANIM_ROTATING_SEARCHLIGHT,           R.string.mode_anim_rotating_searchlight),
            new MenuProperty.Menu(MODE_ANIM_ROTATING_SEARCHLIGHT_PALETTE,   R.string.mode_anim_rotating_searchlight_palette),
            new MenuProperty.Menu(MODE_ANIM_TWINKLE,                        R.string.mode_anim_twinkle),
            new MenuProperty.Menu(MODE_ANIM_GLITTER,                        R.string.mode_anim_glitter),
            new MenuProperty.Menu(MODE_ANIM_SPRINKLE,                       R.string.mode_anim_sprinkle),
            new MenuProperty.Menu(MODE_ANIM_SPOTLIGHTS,                     R.string.mode_anim_spotlights),
            new MenuProperty.Menu(MODE_ANIM_SPOTLIGHTS_PALETTE,             R.string.mode_anim_spotlights_palette),
            new MenuProperty.Menu(MODE_ANIM_POLICE,                         R.string.mode_anim_police),
    };

    public static final MenuProperty.Menu[] MENU_COLOR_PALETTE = new MenuProperty.Menu[]{
            new MenuProperty.Menu(  0, R.string.palette_rainbow),
            new MenuProperty.Menu(  1, R.string.palette_red_stripe),
            new MenuProperty.Menu(  2, R.string.palette_party),
            new MenuProperty.Menu(  3, R.string.palette_ocean),
            new MenuProperty.Menu(  4, R.string.palette_forest),
            new MenuProperty.Menu(  5, R.string.palette_lava),
            new MenuProperty.Menu(  6, R.string.palette_rgb),
    };

    public static final MenuProperty.Menu[] MENU_TIME_FUNCTION = new MenuProperty.Menu[]{
            new MenuProperty.Menu(  0, R.string.time_function_sawtooth),
            new MenuProperty.Menu(  1, R.string.time_function_sawtooth_reverse),
            new MenuProperty.Menu(  2, R.string.time_function_triangular),
            new MenuProperty.Menu(  3, R.string.time_function_sinusoidal),
            new MenuProperty.Menu(  4, R.string.time_function_quadwave),
    };

    public static final MenuProperty.Menu[] MENU_LED_MAP = new MenuProperty.Menu[]{
            new MenuProperty.Menu(  0, R.string.led_map_pentagon),
            new MenuProperty.Menu(  1, R.string.led_map_heart),
            new MenuProperty.Menu(  2, R.string.led_map_kraken),
            new MenuProperty.Menu(  3, R.string.led_map_kraken_bg),
            new MenuProperty.Menu(  4, R.string.led_map_snake),
            new MenuProperty.Menu(  5, R.string.led_map_spiral),
    };


    public final BluetoothGattCharacteristic characteristic;
    public final @StringRes int name;
    public final @LayoutRes int layout;
    public final int order;

    protected Property(@NonNull BluetoothGattCharacteristic characteristic, int order, @StringRes int name, @LayoutRes int layout) {
        this.characteristic = characteristic;
        this.name = name;
        this.layout = layout;
        this.order = order;
    }

    /**
     * Return a Property object wrapping the given characteristic
     */
    @NonNull
    public static Property<?> wrapCharacteristic(@NonNull BluetoothGattCharacteristic characteristic) {
        switch (characteristic.getUuid().toString().toUpperCase(Locale.ROOT)) {

            // Well known and supported characteristics

            case CHARACTERISTIC_ON_OFF:
                return new SwitchProperty(characteristic, 1, R.string.switch_on_off);
            case CHARACTERISTIC_BRIGHTNESS:
                return new ByteValueProperty(characteristic, 2, R.string.brightness, true);
            case CHARACTERISTIC_DEMO:
                return new SwitchProperty(characteristic, 90, R.string.demo_mode);
            case CHARACTERISTIC_MODE:
                return new MenuProperty(characteristic, 3, R.string.mode, MENU_MODE);
            case CHARACTERISTIC_BPM:
                return new ByteValueProperty(characteristic, 4, R.string.bpm, false);
            case CHARACTERISTIC_COLOR_PALETTE:
                return new MenuProperty(characteristic, 4, R.string.color_palette, MENU_COLOR_PALETTE);
            case CHARACTERISTIC_TIME_FUNCTION:
                return new MenuProperty(characteristic, 4, R.string.time_function, MENU_TIME_FUNCTION);
            case CHARACTERISTIC_COLOR:
                return new ColorProperty(characteristic, 5, R.string.color);
            case CHARACTERISTIC_LED_MAP:
                return new MenuProperty(characteristic, 5, R.string.led_pattern_map, MENU_LED_MAP);
            case CHARACTERISTIC_SET_LED:
                return new RawProperty(characteristic, 91, R.string.single_led_color);
            case CHARACTERISTIC_SET_ALL:
                return new RawProperty(characteristic, 91, R.string.batch_led_color);
            default:
                // Generic placeholder for unknown characteristics
                return new RawProperty(characteristic, 100, R.string.unknown);
        }
    }

    /**
     * Returns if the given property should be shown considering the current mode
     */
    public static boolean shouldShowProperty(String propertyUUID){
        Set<String> show = new HashSet<>();
        // always visible
        show.add(CHARACTERISTIC_ON_OFF);
        show.add(CHARACTERISTIC_BRIGHTNESS);
        show.add(CHARACTERISTIC_MODE);
        // visible for certain modes only
        Property<?> modeProperty = Sphere2Lamp.getProperty(CHARACTERISTIC_MODE);
        switch (modeProperty == null ? 0 : modeProperty.getByte()) {
            case MODE_MANUAL:
                break;
            case MODE_SOLID:
                show.add(CHARACTERISTIC_COLOR);
                break;
            case MODE_ANIM_MAP:
                show.addAll(Arrays.asList(CHARACTERISTIC_BPM, CHARACTERISTIC_COLOR_PALETTE, CHARACTERISTIC_TIME_FUNCTION, CHARACTERISTIC_LED_MAP, CHARACTERISTIC_COLOR));
                break;
            case MODE_ANIM_SOLID:
            case MODE_ANIM_ROTATING_GRADIENT:
            case MODE_ANIM_POLAR_GRADIENT:
            case MODE_ANIM_ROTATING_SEARCHLIGHT_PALETTE:
            case MODE_ANIM_SPOTLIGHTS_PALETTE:
                show.addAll(Arrays.asList(CHARACTERISTIC_BPM, CHARACTERISTIC_COLOR_PALETTE, CHARACTERISTIC_TIME_FUNCTION));
                break;
            case MODE_ANIM_AZIMUTH_GRADIENT:
                show.addAll(Arrays.asList(CHARACTERISTIC_BPM, CHARACTERISTIC_COLOR_PALETTE, CHARACTERISTIC_TIME_FUNCTION, CHARACTERISTIC_COLOR));
                break;
            case MODE_ANIM_ROTATING_SEARCHLIGHT:
            case MODE_ANIM_GLITTER:
            case MODE_ANIM_SPOTLIGHTS:
                show.addAll(Arrays.asList(CHARACTERISTIC_BPM, CHARACTERISTIC_COLOR));
                break;
            case MODE_ANIM_TWINKLE:
            case MODE_ANIM_SPRINKLE:
                show.addAll(Arrays.asList(CHARACTERISTIC_BPM, CHARACTERISTIC_COLOR_PALETTE));
                break;
            case MODE_ANIM_POLICE:
                show.add(CHARACTERISTIC_BPM);
                break;
        }
        return show.contains(propertyUUID.toUpperCase(Locale.ROOT));
    }







    public String getUuid() {
        return characteristic.getUuid().toString().toUpperCase(Locale.ROOT);
    }

    /**
     * Set and write a new bytes value to the wrapped characteristics
     */
    public void setBytes(@NonNull byte... values) {
        characteristic.setValue(values);
        Sphere2Lamp.writeProperty(this);
    }

    /**
     * Get the cached bytes value of the wrapped characteristics
     */
    @NonNull
    public byte[] getBytes() {
        byte[] v = characteristic.getValue();
        return v == null ? new byte[0] : v;
    }

    /**
     * Get the cached value of the first byte (if any) of the wrapped characteristics
     */
    public int getByte() {
        byte[] value = getBytes();
        return value.length > 0 ? value[0] & 0xFF : 0;
    }

    /**
     * Set and write a new value to the wrapped characteristics
     */
    public abstract void set(T value);

    /**
     * Get the cached value of the wrapped characteristics
     */
    @NonNull
    public abstract T get();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property<?> property = (Property<?>) o;
        return Objects.equals(getUuid(), property.getUuid());
    }

    @Override
    public int hashCode() {
        return characteristic.getUuid().hashCode();
    }

    public int compare(@NonNull Property<?> other) {
        return this.order - other.order;
    }
}
