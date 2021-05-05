package com.example.padlockdemo.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import java.util.List;

public class BluetoothPadlock {
    private String id;
    private String serviceUuid;
    private String name;
    private String macAddress;
    private String token;
    private BluetoothDevice bluetoothDevice;
    private List<BluetoothGattService> bluetoothGattServices;

    public BluetoothPadlock(String id, String serviceUuid, String name, String macAddress, String token) {
        this.id = id;
        this.serviceUuid = serviceUuid;
        this.name = name;
        this.macAddress = macAddress;
        this.token = token;
    }

    public BluetoothPadlock(String id, String serviceUuid, String name, String macAddress, String token, BluetoothDevice bluetoothDevice) {
        this.id = id;
        this.serviceUuid = serviceUuid;
        this.name = name;
        this.macAddress = macAddress;
        this.token = token;
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceUuid() {
        return serviceUuid;
    }

    public void setServiceUuid(String serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public List<BluetoothGattService> getBluetoothGattServices() {
        return bluetoothGattServices;
    }

    public void setBluetoothGattServices(List<BluetoothGattService> bluetoothGattServices) {
        this.bluetoothGattServices = bluetoothGattServices;
    }
}
