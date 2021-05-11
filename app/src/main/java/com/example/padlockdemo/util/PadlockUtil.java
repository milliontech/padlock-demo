package com.example.padlockdemo.util;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.padlockdemo.model.BlePadlock;
import com.example.padlockdemo.model.Command;
import com.example.padlockdemo.model.PadlockReceiver;
import com.google.gson.Gson;

import java.util.function.Function;

public class PadlockUtil {
    private final static String TAG = PadlockUtil.class.getSimpleName();

    public static String serviceUuid = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String writeUuid = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static String notifyUuid = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String name = "HeartLock";

    private static void sendRequest(Context context, BlePadlock padlock, Command command,
                                    Function<byte[], Boolean> successFunc, Function<String, Boolean> failFunc) {
        command.setToken(StringUtil.StrToHexbyte(padlock.getToken()));
        Log.d(TAG, String.format("Sending command [%s] to [%s]...", command.getCommandString(), padlock.getMacAddress()));

        Log.d(TAG, String.format("Padlock [%s] connect...", padlock.getMacAddress()));
        BleManager.getInstance().connect(padlock.getDevice(), new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, String.format("Padlock [%s] onStartConnect", padlock.getMacAddress()));
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                padlock.setConnected(false);
                Log.d(TAG, String.format("Padlock [%s] onConnectFail", padlock.getMacAddress()));
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                padlock.setConnected(true);
                context.sendBroadcast(new Intent(context, PadlockReceiver.class).setAction("onConnectSuccess"));
                Log.d(TAG, String.format("Padlock [%s] onConnectSuccess", padlock.getMacAddress()));

                final boolean[] success = {false};

                Log.d(TAG, String.format("Padlock [%s] notify...", padlock.getMacAddress()));
                BleManager.getInstance().notify(
                        padlock.getDevice(),
                        PadlockUtil.serviceUuid,
                        PadlockUtil.notifyUuid,
                        new BleNotifyCallback() {
                            @Override
                            public void onNotifySuccess() {
                                Log.d(TAG, String.format("Padlock [%s] onNotifySuccess", padlock.getMacAddress()));
                            }

                            @Override
                            public void onNotifyFailure(BleException exception) {
                                Log.d(TAG, String.format("Padlock [%s] onNotifyFailure [%s]", padlock.getMacAddress(), exception.getDescription()));
                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                if (success[0] && successFunc != null) {
                                    Gson gson = new Gson();
                                    Log.d(TAG, String.format("Padlock before [%s]", gson.toJson(padlock)));
                                    successFunc.apply(data);

                                    if (!padlock.isAutoDisconnect())
                                        BleManager.getInstance().disconnect(padlock.getDevice());

                                    Log.d(TAG, String.format("Padlock after [%s]", gson.toJson(padlock)));
                                } else if (!success[0]) {
                                    Log.d(TAG, String.format("Padlock [%s] onCharacteristicChanged not success", padlock.getMacAddress()));
                                }
                            }
                        }
                );

                AsyncUtil.postDelay(context, () -> {
                    Log.d(TAG, String.format("Padlock [%s] write [%s]...", padlock.getMacAddress(), command.getCommandString()));
                    BleManager.getInstance().write(
                            padlock.getDevice(),
                            PadlockUtil.serviceUuid,
                            PadlockUtil.writeUuid,
                            command.getCommandData(),
                            new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                    success[0] = true;
                                    Log.d(TAG, String.format("Padlock [%s] onWriteSuccess [%s]", padlock.getMacAddress(), command.getCommandString()));
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    if (failFunc != null)
                                        failFunc.apply(exception.getDescription());

                                    if (!padlock.isAutoDisconnect())
                                        BleManager.getInstance().disconnect(padlock.getDevice());

                                    Log.d(TAG, String.format("Padlock [%s] onWriteFailure [%s]", padlock.getMacAddress(), exception.getDescription()));
                                }
                            });
                }, 200);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                padlock.setConnected(false);
                context.sendBroadcast(new Intent(context, PadlockReceiver.class).setAction("onDisConnected"));
                Log.d(TAG, String.format("Padlock [%s] onDisConnected", padlock.getMacAddress()));
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
