package com.github.eltos.sphere2lamp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.eltos.sphere2lamp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

import eltos.simpledialogfragment.SimpleDialog;

public class MainActivity extends AppCompatActivity implements BleDeviceListDialog.OnDialogResultListener, Sphere2Lamp.Callback {

    private static final String SELECT_BLE_DEVICE = "SELECT_BLE_DEVICE";
    private static final String PERMISSION_INFO_DIALOG = "PERMISSION_INFO_DIALOG";
    private static final int REQUEST_PERMISSIONS = 1873;
    private static final String PREF_DEVICE_ADDRESS = "PREF_DEVICE_ADDRESS";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        
        Sphere2Lamp.addCallback(this);

        // get bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // auto connect
        if (!Sphere2Lamp.isConnected()) {
            connect(true);
        }
    }

    @Override
    protected void onDestroy() {
        Sphere2Lamp.removeCallback(this);
        //Sphere2Lamp.disconnect();
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_connect).setVisible(!Sphere2Lamp.isConnected());
        menu.findItem(R.id.action_disconnect).setVisible(Sphere2Lamp.isConnected());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_connect) {
            connect(true);
        } else if (id == R.id.action_disconnect) {
            Sphere2Lamp.disconnect();
        } else if (id == R.id.action_select_device) {
            connect(false);
        } else if (id == R.id.action_refresh) {
            Sphere2Lamp.readAllProperties();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }




    public void connect(boolean connectLastDevice){
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show();

        } else {
            if (ensurePermissions()){
                SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
                String address = pref.getString(PREF_DEVICE_ADDRESS, null);
                
                if (connectLastDevice && address != null && BluetoothAdapter.checkBluetoothAddress(address)) {
                    // enable bluetooth and connect to known address
                    if (!mBluetoothAdapter.isEnabled()){ mBluetoothAdapter.enable(); }
                    Sphere2Lamp.connect(this, mBluetoothAdapter.getRemoteDevice(address));

                } else {
                    // no known address, discover devices and let the user select one
                    Sphere2Lamp.disconnect();
                    BleDeviceListDialog.build()
                            .title(R.string.select_bluetooth_device)
                            //.filterable(true)
                            .show(this, SELECT_BLE_DEVICE);
                }
            }
        }
    }

    
    // callbacks

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onLampConnectionChange() {
        invalidateOptionsMenu();
    }
    
    @Override
    public void onLampError(@StringRes int id) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLampPropertiesDiscovered() {
    }

    @Override
    public void onLampPropertiesUpdated() {
    }
    
    



    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        // Permission explain dialog
        if (PERMISSION_INFO_DIALOG.equals(dialogTag) && which == BUTTON_POSITIVE){
            // open settings so user can manually grant permission
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }

        // Bluetooth device selection dialog
        if (SELECT_BLE_DEVICE.equals(dialogTag) && which == BUTTON_POSITIVE){
            BluetoothDevice device = extras.getParcelable(BleDeviceListDialog.BLUETOOTH_DEVICE);
            // save address so we can directly connect in the future
            SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(PREF_DEVICE_ADDRESS, device.getAddress());
            editor.apply();
            // and connect to the device
            connect(true);
            return true;
        }

        return false;
    }



    /*
       Android permissions
     */
    private boolean ensurePermissions() {
        String[] missing = getMissingPermissions();
        if (missing.length > 0){
            ActivityCompat.requestPermissions(this, missing, REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    private @NonNull String[] getRequiredPermissions(){
        try {
            return getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID,
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    private @NonNull String[] getMissingPermissions(){
        List<String> missing = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                missing.add(permission);
            }
        }
        return missing.toArray(new String[0]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_PERMISSIONS == requestCode){
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted){
                // try to connect again
                connect(true);
            } else {
                // explain why we need permissions
                SimpleDialog.build()
                        .title(R.string.permission_required)
                        .msg(R.string.explain_permissions)
                        .pos(R.string.settings)
                        .neut()
                        .show(this, PERMISSION_INFO_DIALOG);
            }

        }
    }
}