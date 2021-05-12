package com.example.padlockdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.example.padlockdemo.adapter.PadlocksAdapter;
import com.example.padlockdemo.manager.BleRequestManager;
import com.example.padlockdemo.model.BlePadlock;
import com.example.padlockdemo.model.BleRequest;
import com.example.padlockdemo.model.Command;
import com.example.padlockdemo.model.PadlockReceiver;
import com.example.padlockdemo.util.AsyncUtil;
import com.example.padlockdemo.util.BluetoothUtil;
import com.example.padlockdemo.util.PadlockUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private final static int REQUEST_ENABLE_BT = 1;

    public static Activity currentActivity;
    public static ArrayList<BlePadlock> blePadlockArrayList;
    public static PadlocksAdapter padlocksAdapter;

    private ClipboardManager clipboardManager;
    private BleManager bleManager;

    private BroadcastReceiver broadcastReceiver;

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

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        currentActivity = this;
        blePadlockArrayList = new ArrayList<>();
        blePadlockArrayList.add(new BlePadlock("868315518395127", PadlockUtil.serviceUuid, PadlockUtil.name, "D5:3B:F9:45:27:58", "f508eebe", false));
        blePadlockArrayList.add(new BlePadlock("868315518395770", PadlockUtil.serviceUuid, PadlockUtil.name, "F1:F2:80:90:81:29", "a716c2f1", true));
        blePadlockArrayList.add(new BlePadlock("868315518397131", PadlockUtil.serviceUuid, PadlockUtil.name, "C3:17:99:9C:49:AF", "9059eea5", true));

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        bleManager = BleManager.getInstance();
        bleManager.init(getApplication());

        if (!bleManager.isSupportBle()) {
            showMessage("BLE is not supported");
        } else {
            bleManager.enableBluetooth();
            bleManager.enableLog(true)
                    .setReConnectCount(1, 5000)
                    .setSplitWriteNum(20)
                    .setConnectOverTime(10000)
                    .setOperateTimeout(5000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        Arrays.stream(PadlockReceiver.intentFilters).forEach(i -> intentFilter.addAction(i));
        broadcastReceiver = new PadlockReceiver();
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ENABLE_BT && BluetoothUtil.checkGrantResults(permissions, grantResults)
            && BluetoothUtil.areLocationServicesEnabled(this)) {
            // Ble has permissions and open
        }
    }

    public static void scan(Runnable successFunc) {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(new UUID[] { UUID.fromString(PadlockUtil.serviceUuid) })
                .setDeviceName(true, PadlockUtil.name)
                .setAutoConnect(false)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                for (BleDevice device : scanResultList) {
                    BlePadlock padlock = BlePadlock.getPadlock(blePadlockArrayList, device);
                    if (padlock != null) {
                        padlock.setDevice(device);
                        padlock.setProcessing(true);

                        BleRequestManager manager = BleRequestManager.getInstance();
                        manager.add(new BleRequest(currentActivity, padlock, Command.queryPowerPercentage, () -> padlocksAdapter.notifyDataSetChanged()));
                        manager.add(new BleRequest(currentActivity, padlock, Command.queryUnlockTimes, () -> padlocksAdapter.notifyDataSetChanged()));
                        manager.add(new BleRequest(currentActivity, padlock, Command.querySoftwareVersion, () -> padlocksAdapter.notifyDataSetChanged()));
                    }
                }
                padlocksAdapter.notifyDataSetChanged();

                if (successFunc != null)
                    successFunc.run();
            }

            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onScanning(BleDevice bleDevice) {
            }
        });
    }

    public static void showMessage(String message) {
        AsyncUtil.postDelay(currentActivity, () -> {
            Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show();
            Log.i(TAG, message);
        }, 0);
    }
}