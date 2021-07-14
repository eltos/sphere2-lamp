package com.github.eltos.sphere2lamp;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.github.eltos.sphere2lamp.properties.ByteValueProperty;
import com.github.eltos.sphere2lamp.properties.ColorProperty;
import com.github.eltos.sphere2lamp.properties.MenuProperty;
import com.github.eltos.sphere2lamp.properties.Property;
import com.github.eltos.sphere2lamp.properties.SwitchProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.ColorView;
import eltos.simpledialogfragment.color.SimpleColorWheelDialog;

import static androidx.recyclerview.widget.SortedList.INVALID_POSITION;

public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SimpleDialog.OnDialogResultListener {


    public static final String COLOR_PICKER_DIALOG = "adapter_color_picker_dialog";
    private static final String UUID_KEY = "uuid";

    private Fragment mFragment;


    private final SortedList.Callback<Property<?>> mCallback = new SortedList.Callback<Property<?>>() {
        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public int compare(Property<?> a, Property<?> b) {
            return a.compare(b);
        }

        @Override
        public boolean areContentsTheSame(Property<?> a, Property<?> b) {
            return Arrays.equals(a.characteristic.getValue(), b.characteristic.getValue());
        }

        @Override
        public boolean areItemsTheSame(Property<?> a, Property<?> b) {
            return a.equals(b);
        }
    };

    @SuppressWarnings("unchecked")
    private final SortedList<Property<?>> mVisibleProperties = new SortedList<>((Class<Property<?>>) (Class<?>) Property.class, mCallback);
    private final List<Property<?>> mAllProperties = new ArrayList<>();
    private boolean mFilterItems = true;


    public void replaceProperties(List<Property<?>> data){
        mAllProperties.clear();
        mAllProperties.addAll(data);
        filterProperties();
    }



    public void registerFragment(Fragment fragment){
        mFragment = fragment;
    }


    public void setFilterItems(boolean mFilterItems) {
        this.mFilterItems = mFilterItems;
    }


    public void updateProperty(Property<?> property){
        int index = mVisibleProperties.indexOf(property);
        if (index != INVALID_POSITION){
            Log.d("Sphere", "Update "+property.getUuid()+" from "+mVisibleProperties.get(index).getByte()+" to "+property.getByte());
            mVisibleProperties.updateItemAt(index, property);
            notifyItemChanged(index);
            filterProperties();
        }
    }


    public void filterProperties(){
        mVisibleProperties.beginBatchedUpdates();
        // remove properties no longer available
        for (int i = mVisibleProperties.size() - 1; i >= 0; i--) {
            final Property<?> property = mVisibleProperties.get(i);
            if (!mAllProperties.contains(property)) {
                mVisibleProperties.remove(property);
            }
        }
        // add/remove properties depending on whether they should (not) be shown
        for (Property<?> property : mAllProperties) {
            if (!mFilterItems || Property.shouldShowProperty(property.getUuid())){
                mVisibleProperties.add(property);
            } else {
                mVisibleProperties.remove(property);
            }
        }
        mVisibleProperties.endBatchedUpdates();
    }









    @Override
    public int getItemCount() {
        return mVisibleProperties.size();
    }

    public Property<?> getItem(int position){
        return mVisibleProperties.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).layout;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        if (viewType == R.layout.dashboard_card_switch) return new SwitchViewHolder(view);
        if (viewType == R.layout.dashboard_card_slider) return new SliderViewHolder(view);
        if (viewType == R.layout.dashboard_card_input_numeric) return new NumberViewHolder(view);
        if (viewType == R.layout.dashboard_card_menu) return new MenuViewHolder(view);
        if (viewType == R.layout.dashboard_card_color) return new ColorViewHolder(view);
        return new RawViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof SwitchViewHolder){
            ((SwitchViewHolder) viewHolder).bind((SwitchProperty) getItem(position));
        } else if (viewHolder instanceof SliderViewHolder){
            ((SliderViewHolder) viewHolder).bind((ByteValueProperty) getItem(position));
        } else if (viewHolder instanceof NumberViewHolder){
            ((NumberViewHolder) viewHolder).bind((ByteValueProperty) getItem(position));
        } else if (viewHolder instanceof MenuViewHolder) {
            ((MenuViewHolder) viewHolder).bind((MenuProperty) getItem(position));
        } else if (viewHolder instanceof ColorViewHolder){
            ((ColorViewHolder) viewHolder).bind((ColorProperty) getItem(position));
        } else {
            ((RawViewHolder) viewHolder).bind(getItem(position));
        }
    }


    /**
     * Basic view holder class, holding elements common to all card views
     */
    public static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        public BaseViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
        }
    }


    /**
     * Raw view holder with an input field for arbitrary byte array values
     */
    public static class RawViewHolder extends BaseViewHolder {
        final EditText editText;
        final Button button;
        public RawViewHolder(View itemView) {
            super(itemView);
            this.editText = itemView.findViewById(R.id.value);
            this.button = itemView.findViewById(R.id.set_button);
        }
        public void bind(Property<?> item){
            if (item.name == R.string.unknown){
                name.setText(item.getUuid());
            } else {
                name.setText(item.name);
            }

            byte[] value = item.getBytes();
            StringBuilder sb = new StringBuilder(value.length * 2);
            for (byte b : value) sb.append(String.format("%02X", b));
            editText.setText(sb.toString());

            button.setOnClickListener(view -> {
                String text = editText.getText().toString();
                try {
                    if (text.length() == 0 || text.length() > 2 && text.length() % 2 != 0)
                        throw new NumberFormatException();
                    byte[] values = new byte[text.length() / 2];
                    for (int i = 0; i < text.length() / 2; i++) {
                        values[i] = (byte) Short.parseShort(text.substring(2 * i, 2 * i + 2), 16);
                    }
                    item.setBytes(values);
                    editText.setError(null);
                } catch (NumberFormatException e) {
                    editText.setError(editText.getResources().getString(R.string.invalid_format));
                }
            });
        }
    }


    /**
     * View holder with a single switch
     */
    public static class SwitchViewHolder extends BaseViewHolder {
        final SwitchCompat switch1;
        public SwitchViewHolder(View itemView) {
            super(itemView);
            this.switch1 = itemView.findViewById(R.id.switch1);
        }
        public void bind(SwitchProperty item){
            name.setText(item.name);
            switch1.setOnCheckedChangeListener(null);
            switch1.setChecked(item.get());
            switch1.setOnCheckedChangeListener((compoundButton, b) -> item.set(b));
        }
    }


    /**
     * View holder with a slider to adjust values in percent
     */
    public static class SliderViewHolder extends BaseViewHolder {
        final TextView info;
        final SeekBar slider;
        public SliderViewHolder(View itemView) {
            super(itemView);
            this.info = itemView.findViewById(R.id.info);
            this.slider = itemView.findViewById(R.id.slider);
        }
        private String percentage(ByteValueProperty item){
            return String.format(Locale.getDefault(), "%.0f %%", 100f*item.get()/255);
        }
        public void bind(ByteValueProperty item){
            name.setText(item.name);
            info.setText(percentage(item));
            slider.setMax(255);
            slider.setProgress(item.get());
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser){
                        item.set(Math.max(Math.min(progress, 255), 0));
                        info.setText(percentage(item));
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }


    /**
     * View holder with an input field for a numeric value in range 0..255
     */
    public static class NumberViewHolder extends BaseViewHolder {
        final EditText input;
        private ByteValueProperty item;
        public NumberViewHolder(View itemView) {
            super(itemView);
            this.input = itemView.findViewById(R.id.value);
        }
        public void bind(ByteValueProperty item){
            name.setText(item.name);
            this.item = item;
            input.removeTextChangedListener(mListener);
            input.setText(String.format(Locale.getDefault(), "%d",item.get()));
            input.addTextChangedListener(mListener);
        }
        private final TextWatcher mListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int v = Integer.parseInt(s.toString());
                    if (v < 0 || v > 255){
                        input.setError(input.getResources().getString(R.string.not_in_range_0_255));
                    } else {
                        input.setError(null);
                        item.set(v);
                    }
                } catch (NumberFormatException e){
                    input.setError(input.getResources().getString(R.string.invalid_format));
                }
            }
        };

    }


    /**
     * View holder with a drop down menu
     */
    public class MenuViewHolder extends BaseViewHolder {
        final AppCompatSpinner spinner;
        public MenuViewHolder(View itemView) {
            super(itemView);
            this.spinner = itemView.findViewById(R.id.value);
        }
        public void bind(MenuProperty item){
            name.setText(item.name);
            MenuAdapter adapter = new MenuAdapter(spinner.getContext(), item.choices);
            spinner.setAdapter(adapter);
            spinner.setSelection(adapter.getPosition(item.get()));
            SpinnerChangedListener callback = new SpinnerChangedListener() {
                @Override
                public void onNewSelection(int position) {
                    item.set(adapter.getItem(position));
                    filterProperties();
                }
            };
            spinner.setOnTouchListener(callback);
            spinner.setOnItemSelectedListener(callback);
        }
        private abstract class SpinnerChangedListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
            boolean userSelect = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                userSelect = true;
                return false;
            }
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (userSelect) {
                    onNewSelection(pos);
                    userSelect = false;
                }
            }
            public abstract void onNewSelection(int position);
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        }

        private class MenuAdapter extends ArrayAdapter<MenuProperty.Menu> {
            public MenuAdapter(@NonNull Context context, @NonNull MenuProperty.Menu[] objects) {
                super(context, android.R.layout.simple_spinner_dropdown_item, objects);
            }
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setText(getItem(position).name);
                return view;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setText(getItem(position).name);
                return view;
            }
        }

    }


    /**
     * View holder for a single color
     */
    public class ColorViewHolder extends BaseViewHolder {
        final ColorView color;
        public ColorViewHolder(View itemView) {
            super(itemView);
            this.color = itemView.findViewById(R.id.color);
        }
        public void bind(ColorProperty item){
            name.setText(item.name);
            color.setColor(item.get());
            color.setOutlineWidth(color.getResources().getDimensionPixelSize(R.dimen.color_circle_border));
            //color.setOutlineColor(ColorView.isColorDark(item.get()) ? 0xFFFFFFFF : 0xFF000000);
            color.setOutlineColor(ColorView.isColorDark(item.get()) ? ColorView.getLightRippleColor(item.get()) : ColorView.getDarkRippleColor(item.get()));
            color.setOnClickListener(v -> {
                Bundle extras = new Bundle();
                extras.putString(UUID_KEY, item.getUuid());
                SimpleColorWheelDialog.build()
                        .color(item.get())
                        .extra(extras)
                        .show(mFragment, COLOR_PICKER_DIALOG);
            });
        }
    }





    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (DashboardAdapter.COLOR_PICKER_DIALOG.equals(dialogTag) && which == BUTTON_POSITIVE){
            ColorProperty property = (ColorProperty) Sphere2Lamp.getProperty(extras.getString(UUID_KEY));
            if (property != null) {
                property.set(extras.getInt(SimpleColorWheelDialog.COLOR));
                updateProperty(property);
                return true;
            }
        }
        return false;
    }


}
