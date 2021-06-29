package com.github.eltos.sphere2lamp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import eltos.simpledialogfragment.list.AdvancedAdapter;
import eltos.simpledialogfragment.list.CustomListDialog;

import static android.bluetooth.le.ScanSettings.SCAN_MODE_BALANCED;

public class BleDeviceListDialog extends CustomListDialog<BleDeviceListDialog> {

    public static final String BLUETOOTH_DEVICE = "bluetooth_device";
    public static final String BLUETOOTH_DEVICE_ADDRESS = "bluetooth_device_address";

    private static final String BLUETOOTH_DEVICE_LIST = "bluetooth_device_list";
    private BleDeviceListAdapter mListAdapter;
    private BluetoothLeScanner mBleScanner;
    private final ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();
    private final HashMap<BluetoothDevice, String> mCachedDeviceNames = new HashMap<>();
    private boolean mScanning = false;

    public static BleDeviceListDialog build(){
        return new BleDeviceListDialog()
                .choiceMode(SINGLE_CHOICE)
                .emptyText(R.string.no_devices)
                .pos(R.string.select)
                .neut();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListAdapter = new BleDeviceListAdapter();

        if (savedInstanceState != null){
            mDeviceList.addAll(savedInstanceState.getParcelableArrayList(BLUETOOTH_DEVICE_LIST));
            for (BluetoothDevice device : mDeviceList) {
                mCachedDeviceNames.put(device, device.getName());
            }
            updateList();
        }

        // create bluetooth adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBleScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }

    }

    @Override
    protected void onDialogShown() {
        super.onDialogShown();
        getNeutralButton().setOnClickListener(view -> {
            if (isScanning()) {
                stopScan();
            } else {
                startScan();
            }
        });

        if (mDeviceList.size() == 0) {
            // auto start for few seconds
            startScan();
            new Handler().postDelayed(this::stopScan, 5_000);
        } else {
            stopScan();
        }
    }


    public void startScan(){
        if (mBleScanner != null){
            ScanSettings settings = new ScanSettings.Builder().setScanMode(SCAN_MODE_BALANCED).build();
            mBleScanner.startScan(null, settings, mScanCallback);

            mScanning = true;
            mDeviceList.clear();
            updateList();

            neut(R.string.stop);
            if (getNeutralButton() != null) {
                getNeutralButton().setText(R.string.stop);
            }
        }
    }

    public void stopScan(){
        if (mBleScanner != null){
            mBleScanner.stopScan(mScanCallback);

            mScanning = false;
            mListAdapter.notifyDataSetChanged();

            neut(R.string.scan);
            if (getNeutralButton() != null) {
                getNeutralButton().setText(R.string.scan);
            }
        }
    }

    public boolean isScanning(){
        return mBleScanner != null && mScanning;
    }


    public void updateList() {
        // sort
        Collections.sort(mDeviceList, (device1, device2) -> {
            // devices without name last
            String name1 = mCachedDeviceNames.get(device1),
                    name2 = mCachedDeviceNames.get(device2);
            if (name1 == null) return 1;
            if (name2 == null) return -1;
            return name1.compareTo(name2);
        });
        // update adapter while keeping checked states
        ArrayList<Long> checkedItemIds = mListAdapter.getCheckedItemIds();
        mListAdapter.setData(mDeviceList, device -> (long) device.getAddress().hashCode());
        mListAdapter.setItemsCheckedFromIds(checkedItemIds);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    protected AdvancedAdapter<BluetoothDevice> onCreateAdapter() {
        return mListAdapter;
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (!mDeviceList.contains(result.getDevice())) {
                // add to list
                mDeviceList.add(result.getDevice());
                mCachedDeviceNames.put(result.getDevice(), result.getDevice().getName());
                updateList();
            }
        }
    };


    @Override
    @CallSuper
    protected boolean callResultListener(int which, Bundle extras) {
        stopScan();
        return super.callResultListener(which, null);
    }

    @Override
    protected Bundle onResult(int which) {
        Bundle result = super.onResult(which);
        if (result.containsKey(SELECTED_SINGLE_POSITION)) {
            BluetoothDevice selectedDevice = mDeviceList.get(result.getInt(SELECTED_SINGLE_POSITION));
            result.putParcelable(BLUETOOTH_DEVICE, selectedDevice);
            result.putString(BLUETOOTH_DEVICE_ADDRESS, selectedDevice.getAddress());
        }
        return result;
    }



    public class BleDeviceListAdapter extends AdvancedAdapter<BluetoothDevice> {

        @Override
        public int getCount() {
            return super.getCount()+1;
        }

        @Override
        public BluetoothDevice getItem(int filteredPosition) {
            return filteredPosition < super.getCount() ? super.getItem(filteredPosition) : null;
        }

        @Override
        public long getItemId(int filteredPosition) {
            return filteredPosition < super.getCount() ? super.getItemId(filteredPosition) : -1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position < super.getCount() ? 0 : 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position >= super.getCount()) {
                // placeholder
                if (convertView == null){
                    convertView = inflate(R.layout.bluetooth_device_list_item_loading, parent, false);
                }
                convertView.findViewById(R.id.loading).setVisibility(isScanning() ? View.VISIBLE : View.GONE);
                return convertView;
            }

            if (convertView == null){
                convertView = inflate(R.layout.bluetooth_device_list_item, parent, false);
            }

            TextView text = convertView.findViewById(android.R.id.text1);
            TextView hint = convertView.findViewById(android.R.id.text2);
            Checkable box = convertView.findViewById(R.id.box);

            BluetoothDevice device = getItem(position);
            text.setText(mCachedDeviceNames.get(device) == null ? getString(R.string.unknown) : highlight(mCachedDeviceNames.get(device), getContext()));
            hint.setText(highlight(device.getAddress(), getContext()));
            box.setChecked(isItemChecked(position));

            return super.getView(position, convertView, parent);
        }

        AdvancedFilter mFilter = new AdvancedFilter(true, true){
            @Override
            protected boolean matches(BluetoothDevice device, @NonNull CharSequence constraint) {
                return matches(device.getName()) || matches(device.getAddress());
            }
        };

        @Override
        public AdvancedFilter getFilter() {
            return mFilter;
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BLUETOOTH_DEVICE_LIST, mDeviceList);
        super.onSaveInstanceState(outState);
    }
}
