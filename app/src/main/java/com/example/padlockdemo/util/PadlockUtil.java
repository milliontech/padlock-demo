package com.example.padlockdemo.util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.model.BlePadlock;
import com.example.padlockdemo.model.Command;

import java.util.UUID;
import java.util.function.Function;

public class PadlockUtil {

    public static String serviceUuid = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String writeUuid = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static String notifyUuid = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String name = "HeartLock";

    private static void sendRequest(Context context, BlePadlock padlock, Command command,
                                    Function<byte[], Boolean> successFunc, Function<String, Boolean> failFunc) {
        BleDevice device = padlock.getDevice();

        BleManager.getInstance().connect(device, new BleGattCallback() {
            @Override
            public void onStartConnect() {

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                final boolean[] success = {false};

                BleManager.getInstance().notify(
                        device,
                        PadlockUtil.serviceUuid,
                        PadlockUtil.notifyUuid,
                        new BleNotifyCallback() {
                            @Override
                            public void onNotifySuccess() {

                            }

                            @Override
                            public void onNotifyFailure(BleException exception) {

                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                if (success[0] && successFunc != null)
                                    successFunc.apply(data);
                            }
                        }
                );

                AsyncUtil.postDelay(context, () -> {
                    command.setToken(StringUtil.StrToHexbyte(padlock.getToken()));

                    BleManager.getInstance().write(
                            device,
                            PadlockUtil.serviceUuid,
                            PadlockUtil.writeUuid,
                            command.getCommandData(),
                            new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                    success[0] = true;
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    if (failFunc != null)
                                        failFunc.apply(exception.getDescription());
                                }
                            });
                }, 500);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {

            }
        });
    }

    public static void unlock(Context context, BlePadlock padlock,
                              Function<byte[], Boolean> successFunc, Function<String, Boolean> failFunc) {
        sendRequest(context, padlock, Command.unlockRequest, successFunc, failFunc);
    }

    public static void queryLockStatus(Context context, BlePadlock padlock,
                                       Function<byte[], Boolean> successFunc, Function<String, Boolean> failFunc) {
        sendRequest(context, padlock, Command.querylockStatusRequest, successFunc, failFunc);
    }
}
