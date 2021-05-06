package com.example.padlockdemo;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.padlockdemo.adapter.PadlocksAdapter;
import com.example.padlockdemo.model.BluetoothPadlock;
import com.example.padlockdemo.model.Command;
import com.example.padlockdemo.ui.home.HomeFragment;
import com.example.padlockdemo.util.AesUtil;
import com.example.padlockdemo.util.AsyncUtil;
import com.example.padlockdemo.util.BluetoothUtil;
import com.example.padlockdemo.util.PadlockUtil;
import com.example.padlockdemo.util.StringUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private final static int REQUEST_ENABLE_BT = 1;

    private static Activity currentActivity;
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothLeScanner bluetoothLeScanner;
    public static ArrayList<BluetoothPadlock> bluetoothPadlockList;
    public static ClipboardManager clipboardManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        currentActivity = this;
        bluetoothPadlockList = new ArrayList<>();

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!BluetoothUtil.requestBlePermissions(this, REQUEST_ENABLE_BT) && BluetoothUtil.areLocationServicesEnabled(this)) {
            startBleService();
        }
    }

    public void startBleService() {
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showMessage("BLE is not supported");
            finish();
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            scanLeDevice(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ENABLE_BT && BluetoothUtil.checkGrantResults(permissions, grantResults)) {
            if (BluetoothUtil.areLocationServicesEnabled(this)) {
                startBleService();
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setDeviceName(PadlockUtil.name);
        Vector<ScanFilter> filters = new Vector<ScanFilter>();
        filters.add(builder.build());

        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        scanSettingsBuilder.setReportDelay(0);

        if (enable) {
            showMessage("Start scanning BLE devices...");
            bluetoothLeScanner.startScan(filters, scanSettingsBuilder.build(), scanCallback);
        } else {
            showMessage("Stop scanning BLE devices...");
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addDevice(result.getDevice(), result);
        }
    };

    private BluetoothPadlock getBluetoothPadlock (String macAddress) {
        for (BluetoothPadlock padlock : bluetoothPadlockList) {
            if (padlock.getMacAddress().equals(macAddress))
                return padlock;
        }
        return null;
    }

    private void addDevice(BluetoothDevice device, ScanResult scanResult) {
        BluetoothPadlock padlock = getBluetoothPadlock(device.getAddress());
        if (padlock != null) {
            padlock.setBluetoothDevice(device);
            HomeFragment.padlocksAdapter.notifyDataSetChanged();
        }
    }

    public static void showMessage(String message) {
        AsyncUtil.postDelay(currentActivity, () -> {
            Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show();
            Log.i(TAG, message);
        }, 0);
    }
}