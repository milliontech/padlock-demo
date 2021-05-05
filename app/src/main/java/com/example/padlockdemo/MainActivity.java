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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.padlockdemo.model.BluetoothPadlock;
import com.example.padlockdemo.model.Command;
import com.example.padlockdemo.util.AsyncUtil;
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

    private static Activity currentActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;

    private String serviceUuid = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private String notifyUuid = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    private String writeUuid = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    private String name = "HeartLock";

    private List<BluetoothPadlock> bluetoothPadlockList;

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
        bluetoothPadlockList.add(new BluetoothPadlock("868315518395127", serviceUuid, name, "D5:3B:F9:45:27:58", "00000000"));
        //bluetoothPadlockList.add(new BluetoothPadlock("868315518395770", serviceUuid, name, "F1:F2:80:90:81:29", "a716c2f1"));
        //bluetoothPadlockList.add(new BluetoothPadlock("868315518397131", serviceUuid, name, "C3:17:99:9C:49:AF", "9059eea5"));

        if (!requestBlePermissions(this, REQUEST_ENABLE_BT) && areLocationServicesEnabled(this)) {
            startBleService();
        }
    }

    private void startBleService() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showMessage("BLE is not supported");
            finish();
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            scanLeDevice(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ENABLE_BT && checkGrantResults(permissions, grantResults)) {
            if (areLocationServicesEnabled(this)) {
                startBleService();
            }
        }
    }

    private boolean hasBlePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean requestBlePermissions(final Activity activity, int requestCode) {
        if (hasBlePermissions())
            return false;

        ActivityCompat.requestPermissions(activity, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, requestCode);

        return true;
    }

    private boolean checkGrantResults(String[] permissions, int[] grantResults) {
        int granted = 0;

        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        granted++;
                    }
                }
            }
        } else {
            return false;
        }

        return granted == 2;
    }

    private boolean areLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void scanLeDevice(final boolean enable) {
        ScanFilter.Builder builder = new ScanFilter.Builder();

        bluetoothPadlockList.stream().forEach(i -> {
            builder.setDeviceAddress(i.getMacAddress());
        });

        Vector<ScanFilter> filters = new Vector<ScanFilter>();
        filters.add(builder.build());

        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        scanSettingsBuilder.setReportDelay(0);

        if (enable) {
            showMessage("Start scanning BLE devices...");
            mScanning = true;
            mBluetoothLeScanner.startScan(filters, scanSettingsBuilder.build(), scanCallback);
        } else {
            showMessage("Stop scanning BLE devices...");
            mScanning = false;
            mBluetoothLeScanner.stopScan(scanCallback);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addDevice(result.getDevice(), result);
        }
    };

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    showMessage("Connected to GATT server.");
                    if (gatt.discoverServices()) {
                        showMessage("Attempting to start service discovery...");
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    showMessage("Disconnected from GATT server.");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    BluetoothPadlock padlock = getBluetoothPadlock(gatt.getDevice().getAddress());
                    List<BluetoothGattService> bluetoothGattServices = gatt.getServices();
                    padlock.setBluetoothGattServices(bluetoothGattServices);

                    BluetoothGattService service;
                    //service = bluetoothGattServices.stream().filter(i -> i.getUuid().equals(UUID.fromString(serviceUuid))).findFirst().get();
                    service = gatt.getService(UUID.fromString(serviceUuid));
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(writeUuid));

                    //BluetoothGattService readService = bluetoothGattServices.get(0);
                    //BluetoothGattCharacteristic readCharacteristic = readService.getCharacteristics().get(0);
                    //boolean result = gatt.readCharacteristic(readCharacteristic);

                    AsyncUtil.postDelay(currentActivity, () -> {
                        Command command = Command.unlockCmd;
                        command.setToken(StringUtil.StrToHexbyte(padlock.getToken()));
                        //characteristic.setValue(command.getCommandData());

                        showMessage("Sending command: " + command.getCommandString());
                        if (gatt.writeCharacteristic(characteristic)) {
                            showMessage("Sending command Success.");;
                        } else {
                            showMessage("Sending command Fail.");
                        }
                    }, 1000);


                    break;
                default:
                    break;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    private BluetoothPadlock getBluetoothPadlock (String macAddress) {
        return bluetoothPadlockList.stream().filter(i -> i.getMacAddress().equals(macAddress)).findFirst().get();
    }

    private void addDevice(BluetoothDevice device, ScanResult scanResult) {
        BluetoothPadlock padlock = getBluetoothPadlock(device.getAddress());
        padlock.setBluetoothDevice(device);

        device.connectGatt(this, false, bluetoothGattCallback);
    }

    private static void showMessage(String message) {
        AsyncUtil.postDelay(currentActivity, () -> {
            Toast.makeText(currentActivity, message, Toast.LENGTH_LONG).show();
            Log.i(TAG, message);
        }, 0);
    }

}