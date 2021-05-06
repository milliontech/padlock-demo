package com.example.padlockdemo.util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.example.padlockdemo.model.BluetoothPadlock;
import com.example.padlockdemo.model.Command;

import java.util.UUID;
import java.util.function.Function;

public class PadlockUtil {

    public static String serviceUuid = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String notifyUuid = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static String writeUuid = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String name = "HeartLock";

    public static void unlock(Context context, BluetoothGatt bluetoothGatt, BluetoothPadlock padlock,
                              Function<String, Boolean> successFunc, Function<String, Boolean> failFunc) {

        AsyncUtil.postDelay(context, () -> {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUuid));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(writeUuid));

            Command command = Command.unlockCmd;
            command.setToken(StringUtil.StrToHexbyte(padlock.getToken()));

            byte[] plainData = command.getCommandData();
            byte[] encryptData = AesUtil.encrypt(plainData);

            if (BluetoothUtil.writeData(bluetoothGatt, characteristic, encryptData)) {
                if (successFunc != null)
                    successFunc.apply(command.getCommandString());
            } else {
                if (failFunc != null)
                    failFunc.apply(command.getCommandString());
            }
        }, 1000);
    }
}
