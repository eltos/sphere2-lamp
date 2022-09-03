package com.github.eltos.sphere2lamp;

import static android.bluetooth.le.ScanSettings.SCAN_MODE_BALANCED;

import android.annotation.SuppressLint;
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
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import eltos.simpledialogfragment.list.AdvancedAdapter;
import eltos.simpledialogfragment.list.CustomListDialog;

@SuppressLint("MissingPermission") // bluetooth permissions ensured by activity
public class BleDeviceListDialog extends CustomListDialog<BleDeviceListDialog> {
    @SuppressWarnings("unused")
    public static String TAG = "BleDeviceListDialog";

    public static final String BLUETOOTH_DEVICE = "bluetooth_device";
    public static final String BLUETOOTH_DEVICE_ADDRESS = "bluetooth_device_address";
    public static final String UUID = "uuid";
    private static final String BLUETOOTH_SCAN_RESULT_LIST = "bluetooth_scan_result_list";
    private BleDeviceListAdapter mListAdapter;
    private BluetoothLeScanner mBleScanner;
    private final ArrayList<ScanResult> mScanResultList = new ArrayList<>();
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

    /**
     * Sets a uuid to look for. Devices with a service matching that UUID will be highlighted and
     * listed on top of the list
     * @param uuid The service uuid to look for.
     * @return this instance
     */
    public BleDeviceListDialog serviceUUID(String uuid){
        return setArg(UUID, uuid);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListAdapter = new BleDeviceListAdapter();

        if (savedInstanceState != null){
            mScanResultList.addAll(savedInstanceState.getParcelableArrayList(BLUETOOTH_SCAN_RESULT_LIST));
            updateList();
        }

        // create bluetooth adapter
        requireContext().registerReceiver(mBluetoothBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScan();
        requireContext().unregisterReceiver(mBluetoothBroadcastReceiver);
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

        if (mAutoStartScan && mScanResultList.size() == 0) {
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

        if (mBleScanner == null) {
            mBleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (mBleScanner != null){
            ScanSettings settings = new ScanSettings.Builder().setScanMode(SCAN_MODE_BALANCED).build();
            mBleScanner.startScan(null, settings, mScanCallback);

            mScanning = true;
            mScanResultList.clear();
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

    public boolean hasUUID(ScanResult result){
        return getArgs().containsKey(UUID) && result.getScanRecord().getServiceUuids() != null &&
                result.getScanRecord().getServiceUuids().contains(ParcelUuid.fromString(getArgs().getString(UUID)));
    }


    public void updateList() {
        // sort
        Collections.sort(mScanResultList, (result1, result2) -> {
            // uuid first
            if (hasUUID(result1)) return -1;
            if (hasUUID(result2)) return 1;

            // other devices in alphabetical order by name (if any)
            String name1 = result1.getDevice().getName(),
                    name2 = result2.getDevice().getName();
            if (name1 == null) return 1;
            if (name2 == null) return -1;
            return name1.compareTo(name2);
        });

        // update adapter while keeping checked states
        ArrayList<Long> checkedItemIds = mListAdapter.getCheckedItemIds();
        mListAdapter.setData(mScanResultList, result -> (long) result.getDevice().getAddress().hashCode());
        mListAdapter.setItemsCheckedFromIds(checkedItemIds);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    protected AdvancedAdapter<ScanResult> onCreateAdapter() {
        return mListAdapter;
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // check for duplicates
            for (int i = 0; i < mScanResultList.size(); i++) {
                if (Objects.equals(mScanResultList.get(i).getDevice().getAddress(), result.getDevice().getAddress())){
                    mScanResultList.remove(i);
                    i--;
                }
            }
            // add to list
            mScanResultList.add(result);
            updateList();
            // auto-select if it provides the service
            if (mListAdapter.getCheckedItemIds().isEmpty() && hasUUID(result)){
                mListAdapter.setItemChecked((long) result.getDevice().getAddress().hashCode(), true);
                updatePosButton();
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
            BluetoothDevice selectedDevice = mScanResultList.get(result.getInt(SELECTED_SINGLE_POSITION)).getDevice();
            result.putParcelable(BLUETOOTH_DEVICE, selectedDevice);
            result.putString(BLUETOOTH_DEVICE_ADDRESS, selectedDevice.getAddress());
        }
        return result;
    }



    public class BleDeviceListAdapter extends AdvancedAdapter<ScanResult> {

        @Override
        public int getCount() {
            return super.getCount() + (isScanning() ? 1 : 0);
        }

        @Override
        public ScanResult getItem(int filteredPosition) {
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

            ScanResult result = getItem(position);
            String name = result.getDevice().getName();
            if (name == null) name = getString(R.string.unknown);
            text.setText(highlight(name, getContext()));
            hint.setText(highlight(result.getDevice().getAddress(), getContext()));
            text.setTypeface(null, hasUUID(result) ? Typeface.BOLD : Typeface.NORMAL);
            hint.setTypeface(null, hasUUID(result) ? Typeface.BOLD : Typeface.NORMAL);

            box.setChecked(isItemChecked(position));

            return super.getView(position, convertView, parent);
        }

        final AdvancedFilter mFilter = new AdvancedFilter(true, true){
            @Override
            protected boolean matches(ScanResult result, @NonNull CharSequence constraint) {
                return matches(result.getDevice().getName()) || matches(result.getDevice().getAddress());
            }
        };

        @Override
        public AdvancedFilter getFilter() {
            return mFilter;
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BLUETOOTH_SCAN_RESULT_LIST, mScanResultList);
        super.onSaveInstanceState(outState);
    }
}
