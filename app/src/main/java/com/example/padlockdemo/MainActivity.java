package com.example.padlockdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.appeaser.sublimenavigationviewlibrary.OnNavigationMenuEventListener;
import com.appeaser.sublimenavigationviewlibrary.SublimeBaseMenuItem;
import com.appeaser.sublimenavigationviewlibrary.SublimeNavigationView;
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
import com.example.padlockdemo.util.StringUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    public static SublimeNavigationView sublimeNavigationView;

    private final static int REQUEST_ENABLE_BT = 1;

    public static Activity currentActivity;
    public static ArrayList<BlePadlock> blePadlockArrayList;
    public static PadlocksAdapter padlocksAdapter;

    private BleManager bleManager;

    private BroadcastReceiver broadcastReceiver;

    public static boolean isCheckPower = false;
    public static boolean isCheckUnlockTimes = false;
    public static boolean isCheckStatus = false;
    public static boolean isCheckVersion = false;

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

        sublimeNavigationView = findViewById(R.id.navigation_view);
        sublimeNavigationView.setNavigationMenuEventListener(new OnNavigationMenuEventListener() {
            @Override
            public boolean onNavigationMenuEvent(Event event, SublimeBaseMenuItem menuItem) {
                if (menuItem.getTitle().equals("Power"))
                    isCheckPower = event == Event.CHECKED;
                else if (menuItem.getTitle().equals("Unlock Times"))
                    isCheckUnlockTimes = event == Event.CHECKED;
                else if (menuItem.getTitle().equals("Lock Status"))
                    isCheckStatus = event == Event.CHECKED;
                else if (menuItem.getTitle().equals("Software Version"))
                    isCheckVersion = event == Event.CHECKED;
                MainActivity.savePreferences(MainActivity.currentActivity);
                return false;
            }
        });

        currentActivity = this;
        blePadlockArrayList = new ArrayList<>();
        blePadlockArrayList.add(new BlePadlock(Command.B101, "868315518397131", PadlockUtil.serviceUuid, PadlockUtil.name, "C3:17:99:9C:49:AF", "9059eea5", true));
        blePadlockArrayList.add(new BlePadlock(Command.B101, "868315518395770", PadlockUtil.serviceUuid, PadlockUtil.name, "F1:F2:80:90:81:29", "a716c2f1", true));
        blePadlockArrayList.add(new BlePadlock(Command.C102, "868315518395127", PadlockUtil.serviceUuid, PadlockUtil.name, "D5:3B:F9:45:27:58", "f508eebe", false));

        bleManager = BleManager.getInstance();
        bleManager.init(getApplication());

        if (!bleManager.isSupportBle()) {
            showMessage("BLE is not supported");
        } else {
            if (!bleManager.isBlueEnable()) {
                bleManager.enableBluetooth();
            }

            //https://github.com/Jasonchenlijian/FastBle#initialization-configuration
            bleManager.enableLog(true)
                    .setReConnectCount(10, 1000)
                    .setConnectOverTime(5000)
                    .setOperateTimeout(5000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadPreferences(this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() != null) {
                String hex = result.getContents();
                String text = StringUtil.hexToStr(hex);
                Optional<BlePadlock> padlock = blePadlockArrayList.stream().filter(i -> text.contains(i.getId())).findFirst();
                if (padlock.isPresent()) {
                    BleRequestManager.getInstance()
                            .add(new BleRequest(this, padlock.get(), Command.unlock, () -> MainActivity.padlocksAdapter.notifyDataSetChanged()));
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static void loadPreferences(Activity activity) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        MainActivity.isCheckPower = preferences.getBoolean(activity.getString(R.string.preference_power), false);
        MainActivity.isCheckUnlockTimes = preferences.getBoolean(activity.getString(R.string.preference_unlock_times), false);
        MainActivity.isCheckStatus = preferences.getBoolean(activity.getString(R.string.preference_status), false);
        MainActivity.isCheckVersion = preferences.getBoolean(activity.getString(R.string.preference_version), false);

        sublimeNavigationView.getMenu().getMenuItem(R.id.command_option_power).setChecked(MainActivity.isCheckPower);
        sublimeNavigationView.getMenu().getMenuItem(R.id.command_option_unlock_times).setChecked(MainActivity.isCheckUnlockTimes);
        sublimeNavigationView.getMenu().getMenuItem(R.id.command_option_status).setChecked(MainActivity.isCheckStatus);
        sublimeNavigationView.getMenu().getMenuItem(R.id.command_option_version).setChecked(MainActivity.isCheckVersion);
    }

    public static void savePreferences(Activity activity) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(activity.getString(R.string.preference_power), MainActivity.isCheckPower);
        editor.putBoolean(activity.getString(R.string.preference_unlock_times), MainActivity.isCheckUnlockTimes);
        editor.putBoolean(activity.getString(R.string.preference_status), MainActivity.isCheckStatus);
        editor.putBoolean(activity.getString(R.string.preference_version), MainActivity.isCheckVersion);
        editor.apply();
    }

    public static void scan(Runnable successFunc) {

        //https://github.com/Jasonchenlijian/FastBle#configuration-scan-rules

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(new UUID[] { UUID.fromString(PadlockUtil.serviceUuid) })
                .setDeviceName(true, PadlockUtil.name)
                .setAutoConnect(false)
                .setScanTimeOut(5000)
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
                        if (isCheckPower)
                            manager.add(new BleRequest(currentActivity, padlock, Command.queryPowerPercentage, () -> padlocksAdapter.notifyDataSetChanged()));
                        if (isCheckUnlockTimes)
                            manager.add(new BleRequest(currentActivity, padlock, Command.queryUnlockTimes, () -> padlocksAdapter.notifyDataSetChanged()));
                        if (isCheckStatus)
                            manager.add(new BleRequest(currentActivity, padlock, Command.queryLockStatus, () -> padlocksAdapter.notifyDataSetChanged()));
                        if (isCheckVersion)
                            manager.add(new BleRequest(currentActivity, padlock, Command.querySoftwareVersion, () -> padlocksAdapter.notifyDataSetChanged()));

                        if (padlock.getModel() == Command.C102) {
                            // Set work mode - Idle
                            //manager.add(new BleRequest(currentActivity, padlock, Command.setWorkMode.setData1(Command.BYTES_ZERO), null));
                            // Set built-in clock
                            //manager.add(new BleRequest(currentActivity, padlock, Command.setBuiltinClock.setData1(Command.getBytesFromMilliseconds(System.currentTimeMillis())), null));
                            // Current all status info
                            //manager.add(new BleRequest(currentActivity, padlock, Command.queryCurrentAllStatusInfo, null));
                            // Last locking time
                            //manager.add(new BleRequest(currentActivity, padlock, Command.queryTimeInfo.setData1(Command.BYTES_ZERO), null));
                            // Clock current time
                            //manager.add(new BleRequest(currentActivity, padlock, Command.queryTimeInfo.setData1(Command.BYTES_ONE), null));
                            // ICCID code
                            //manager.add(new BleRequest(currentActivity, padlock, Command.querySimCardInfo.setData1(Command.BYTES_ZERO), null));
                            // IMEI code
                            //manager.add(new BleRequest(currentActivity, padlock, Command.querySimCardInfo.setData1(Command.BYTES_ONE), null));
                        }
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