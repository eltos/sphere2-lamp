package com.github.eltos.sphere2lamp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.Toast;

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
    private boolean mAutoStartScan = true;

    public static BleDeviceListDialog build(){
        return new BleDeviceListDialog()
                .choiceMode(SINGLE_CHOICE)
                .choiceMin(1)
                .emptyText(R.string.no_devices)
                .pos(R.string.connect)
                .neut(R.string.scan);
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
        getActivity().registerReceiver(mBluetoothBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScan();
        getActivity().unregisterReceiver(mBluetoothBroadcastReceiver);
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

        if (mAutoStartScan && mDeviceList.size() == 0) {
            // auto start for few seconds
            startScan();
            new Handler().postDelayed(this::stopScan, 5_000);
        } else {
            stopScan();
        }
    }


    public void startScan(){
        mAutoStartScan = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()){
            mAutoStartScan = true;
            bluetoothAdapter.enable(); //startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            Toast.makeText(getContext(), R.string.enabling_bluetooth, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBleScanner == null) {
            mBleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

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
        if (mBleScanner != null) {
            mBleScanner.stopScan(mScanCallback);
        }

        mAutoStartScan = false;
        mScanning = false;
        mListAdapter.notifyDataSetChanged();

        neut(R.string.scan);
        if (getNeutralButton() != null) {
            getNeutralButton().setText(R.string.scan);
        }

    }

    public boolean isScanning(){
        return mBleScanner != null && mScanning;
    }


    final BroadcastReceiver mBluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        stopScan();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (mAutoStartScan) startScan();
                        break;

                }

            }
        }
    };


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
            return super.getCount() + (isScanning() ? 1 : 0);
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

        final AdvancedFilter mFilter = new AdvancedFilter(true, true){
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
