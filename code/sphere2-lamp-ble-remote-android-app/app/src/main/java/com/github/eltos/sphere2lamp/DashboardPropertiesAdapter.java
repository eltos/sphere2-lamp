package com.github.eltos.sphere2lamp;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.ColorView;
import eltos.simpledialogfragment.color.SimpleColorWheelDialog;

public class DashboardPropertiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SimpleDialog.OnDialogResultListener {


    public static final String COLOR_PICKER_DIALOG = "adapter_color_picker_dialog";
    private static final String UUID_KEY = "uuid";
    private final List<Sphere2LampProperties.Property<?>> mData = new ArrayList<>();
    private Fragment mFragment;


    public void setData(List<Sphere2LampProperties.Property<?>> data){
        mData.clear();
        mData.addAll(data);
    }

    public void registerFragment(Fragment fragment){
        mFragment = fragment;
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public Sphere2LampProperties.Property<?> getItem(int position){
        return mData.get(position);
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
            ((SwitchViewHolder) viewHolder).bind((Sphere2LampProperties.SwitchProperty) getItem(position));

        } else if (viewHolder instanceof SliderViewHolder){
            ((SliderViewHolder) viewHolder).bind((Sphere2LampProperties.ByteProperty) getItem(position));

        } else if (viewHolder instanceof NumberViewHolder){
            ((NumberViewHolder) viewHolder).bind((Sphere2LampProperties.ByteProperty) getItem(position));

        } else if (viewHolder instanceof MenuViewHolder) {
            ((MenuViewHolder) viewHolder).bind((Sphere2LampProperties.MenuProperty) getItem(position));

        } else if (viewHolder instanceof ColorViewHolder){
            ((ColorViewHolder) viewHolder).bind((Sphere2LampProperties.ColorProperty) getItem(position));

        } else {
            ((RawViewHolder) viewHolder).bind(getItem(position));

        }
    }


    public static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public BaseViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
        }
    }


    public static class RawViewHolder extends BaseViewHolder {
        EditText editText;
        Button button;
        public RawViewHolder(View itemView) {
            super(itemView);
            this.editText = itemView.findViewById(R.id.value);
            this.button = itemView.findViewById(R.id.set_button);
        }
        public void bind(Sphere2LampProperties.Property<?> item){
            name.setText(item.characteristic.getUuid().toString()); // item.name

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


    public static class SwitchViewHolder extends BaseViewHolder {
        SwitchCompat switch1;
        public SwitchViewHolder(View itemView) {
            super(itemView);
            this.switch1 = itemView.findViewById(R.id.switch1);
        }
        public void bind(Sphere2LampProperties.SwitchProperty item){
            name.setText(item.name);
            switch1.setOnCheckedChangeListener(null);
            switch1.setChecked(item.get());
            switch1.setOnCheckedChangeListener((compoundButton, b) -> item.set(b));
        }
    }


    public static class SliderViewHolder extends BaseViewHolder {
        TextView info;
        SeekBar slider;
        public SliderViewHolder(View itemView) {
            super(itemView);
            this.info = itemView.findViewById(R.id.info);
            this.slider = itemView.findViewById(R.id.slider);
        }
        private String percentage(Sphere2LampProperties.ByteProperty item){
            return String.format(Locale.getDefault(), "%.0f %%", 100f*(item.get() & 0xFF)/255);
        }
        public void bind(Sphere2LampProperties.ByteProperty item){
            name.setText(item.name);
            info.setText(percentage(item));
            slider.setMax(255);
            slider.setProgress(item.get() & 0xFF);
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser){
                        item.set((byte) Math.max(Math.min(progress, 255), 0));
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


    public static class NumberViewHolder extends BaseViewHolder {
        EditText input;
        private Sphere2LampProperties.ByteProperty item;
        public NumberViewHolder(View itemView) {
            super(itemView);
            this.input = itemView.findViewById(R.id.value);
        }
        public void bind(Sphere2LampProperties.ByteProperty item){
            name.setText(item.name);
            this.item = item;
            input.removeTextChangedListener(mListener);
            input.setText(String.format(Locale.getDefault(), "%d",item.get() & 0xFF));
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
                        if (item != null) {
                            item.set((byte) v);
                        }
                    }
                } catch (NumberFormatException e){
                    input.setError(input.getResources().getString(R.string.invalid_format));
                }
            }
        };

    }


    public static class MenuViewHolder extends BaseViewHolder {
        AppCompatSpinner spinner;
        public MenuViewHolder(View itemView) {
            super(itemView);
            this.spinner = itemView.findViewById(R.id.value);
        }
        public void bind(Sphere2LampProperties.MenuProperty item){
            name.setText(item.name);
            MenuAdapter adapter = new MenuAdapter(spinner.getContext(), item.choices);
            spinner.setAdapter(adapter);
            spinner.setSelection(adapter.getPosition(item.get()));
            SpinnerChangedListener callback = new SpinnerChangedListener() {
                @Override
                public void onNewSelection(int position) {
                    item.set(adapter.getItem(position));
                }
            };
            spinner.setOnTouchListener(callback);
            spinner.setOnItemSelectedListener(callback);
        }
        private abstract static class SpinnerChangedListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
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

        private static class MenuAdapter extends ArrayAdapter<Sphere2LampProperties.MenuProperty.Menu> {
            public MenuAdapter(@NonNull Context context, @NonNull Sphere2LampProperties.MenuProperty.Menu[] objects) {
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




    public class ColorViewHolder extends BaseViewHolder {
        ColorView color;
        public ColorViewHolder(View itemView) {
            super(itemView);
            this.color = itemView.findViewById(R.id.color);
        }
        public void bind(Sphere2LampProperties.ColorProperty item){
            name.setText(item.name);
            color.setColor(item.get());
            color.setOutlineWidth(color.getResources().getDimensionPixelSize(R.dimen.color_circle_border));
            //color.setOutlineColor(ColorView.isColorDark(item.get()) ? 0xFFFFFFFF : 0xFF000000);
            color.setOutlineColor(ColorView.isColorDark(item.get()) ? ColorView.getLightRippleColor(item.get()) : ColorView.getDarkRippleColor(item.get()));
            color.setOnClickListener(v -> {
                Bundle extras = new Bundle();
                extras.putString(UUID_KEY, item.characteristic.getUuid().toString());
                SimpleColorWheelDialog.build()
                        .color(item.get())
                        .extra(extras)
                        .show(mFragment, COLOR_PICKER_DIALOG);
            });
        }
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (DashboardPropertiesAdapter.COLOR_PICKER_DIALOG.equals(dialogTag) && which == BUTTON_POSITIVE){
            UUID uuid = UUID.fromString(extras.getString(UUID_KEY));
            for (Sphere2LampProperties.Property<?> property : mData) {
                if (uuid.equals(property.characteristic.getUuid()) && property instanceof Sphere2LampProperties.ColorProperty){
                    ((Sphere2LampProperties.ColorProperty) property).set(extras.getInt(SimpleColorWheelDialog.COLOR));
                    notifyDataSetChanged();
                    return true;
                }
            }
        }
        return false;
    }


}
