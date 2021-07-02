package com.github.eltos.sphere2lamp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.github.eltos.sphere2lamp.properties.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Sphere2Lamp {

    public interface Callback {
        Context getContext();
        void onLampConnectionChange();
        void onLampError(@StringRes int id);
        void onLampPropertiesDiscovered();
        void onLampPropertyUpdated(String uuid);
    }

    
    public static UUID UUID_SERVICE = UUID.fromString("19B10000-E8F2-517E-4F6C-D104768A1214");
    

    private static final Set<Callback> mCallbacks = new HashSet<>();

    private static int mState;
    private static BluetoothGatt mGatt;
    private static BluetoothGattService mService;

    private static boolean mQueueIdle = true;
    private static final ConcurrentLinkedQueue<BluetoothGattCharacteristic> mReadQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<BluetoothGattCharacteristic> mWriteQueue = new ConcurrentLinkedQueue<>();

    /**
     * Static class not meant for instantiation
     */
    private Sphere2Lamp(){}



    private interface CallbackCaller {
        void call(Callback callback);
    }
    
    public static void addCallback(Callback callback){
        mCallbacks.add(callback);
    }
    public static void removeCallback(Callback callback){
        mCallbacks.remove(callback);
    }
    private static void callCallbacks(CallbackCaller caller){
        for (Callback callback : mCallbacks) {
            new Handler(callback.getContext().getMainLooper()).post(() -> caller.call(callback));
        }
    }



    public static void connect(Context context, BluetoothDevice device){
        disconnect();
        device.connectGatt(context, false, mConnectCallback);
    }

    public static void disconnect(){
        if (mGatt != null){
            mGatt.disconnect();
        }
        mGatt = null;
        mService = null;
        clearQueue();
    }

    public static boolean isConnecting(){
        return mState == BluetoothProfile.STATE_CONNECTING;
    }

    public static boolean isConnected(){
        return mGatt != null && mState == BluetoothProfile.STATE_CONNECTED;
    }

    public static boolean isReady(){
        return isConnected() && mService != null;
    }



    private synchronized static void queueRead(@NonNull BluetoothGattCharacteristic characteristic){
        for (BluetoothGattCharacteristic c : mReadQueue) {
            if (characteristic.getUuid().equals(c.getUuid())) return; // already queued
        }
        mReadQueue.add(characteristic);
        doQueued();
    }

    private synchronized static void queueWrite(@NonNull BluetoothGattCharacteristic characteristic){
        for (BluetoothGattCharacteristic c : mWriteQueue) {
            if (characteristic.getUuid().equals(c.getUuid())) return; // already queued
        }
        mWriteQueue.add(characteristic);
        doQueued();
    }

    private synchronized static void clearQueue(){
        mReadQueue.clear();
        mWriteQueue.clear();
        mQueueIdle = true;
    }

    private synchronized static void doQueued(){
        if (mGatt != null && mQueueIdle){
            BluetoothGattCharacteristic toWrite = mWriteQueue.poll();
            if (toWrite != null) {
                mQueueIdle = false;
                mGatt.writeCharacteristic(toWrite);
            } else {
                BluetoothGattCharacteristic toRead = mReadQueue.poll();
                if (toRead != null) {
                    mQueueIdle = false;
                    mGatt.readCharacteristic(toRead);
                } else {
                    mQueueIdle = true;
                }
            }
        }
    }





    private static final BluetoothGattCallback mConnectCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            mState = newState;
            
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected
                mGatt = gatt;
                gatt.discoverServices();
            } else {
                mGatt = null;
                mService = null;
                gatt.close();
            }

            if (status != BluetoothGatt.GATT_SUCCESS) callCallbacks(callback -> callback.onLampError(R.string.connection_failed));

            callCallbacks(Callback::onLampConnectionChange);

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mService = gatt.getService(UUID_SERVICE);
            if (mService == null){
                disconnect();
                callCallbacks(callback -> callback.onLampError(R.string.device_not_supported));

            } else {
                // service successfully discovered
                callCallbacks(Callback::onLampPropertiesDiscovered);
                // read out all properties
                readAllProperties();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            if (status != BluetoothGatt.GATT_SUCCESS){
                callCallbacks(callback -> callback.onLampError(R.string.failed_to_set_value));
            }
            // perform pending reads/writes from queue
            mQueueIdle = true;
            doQueued();
        }
        
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            if (status == BluetoothGatt.GATT_SUCCESS){
                callCallbacks(callback -> callback.onLampPropertyUpdated(characteristic.getUuid().toString()));
            }
            // perform pending reads/writes from queue
            mQueueIdle = true;
            doQueued();
        }
    };


    /**
     * Return property for given uuid
     */
    @Nullable
    public static Property<?> getProperty(@NonNull String uuid){
        if (mGatt != null && mService != null) {
            BluetoothGattCharacteristic c = mService.getCharacteristic(UUID.fromString(uuid));
            if (c != null) {
                return Property.wrapCharacteristic(c);
            }
        }
        return null;
    }
    
    /**
     * Return a list of properties the lamp supports
     */
    @NonNull
    public static List<Property<?>> getProperties(){
        List<Property<?>> properties = new ArrayList<>();
        if (mGatt != null && mService != null) {
            for (BluetoothGattCharacteristic characteristic : mService.getCharacteristics()){
                properties.add(Property.wrapCharacteristic(characteristic));
            }
        }
        return properties;        
    }
    
    /**
     * Trigger a write of the characteristic associated with the property
     */
    public static void writeProperty(@NonNull Property<?> property){
        if (mGatt != null) {
            queueWrite(property.characteristic);
        }
    }

    /**
     * Trigger a read of all characteristics
     */
    public static void readAllProperties(){
        if (mGatt != null && mService != null){
            for (BluetoothGattCharacteristic characteristic : mService.getCharacteristics()) {
                queueRead(characteristic);
            }
        }
    }
    
    

}
