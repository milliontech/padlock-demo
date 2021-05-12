package com.example.padlockdemo.model;

import com.clj.fastble.data.BleDevice;
import com.example.padlockdemo.util.BcdUtil;

import java.util.List;
import java.util.Optional;

public class BlePadlock {
    private String id;
    private String serviceUuid;
    private String name;
    private String macAddress;
    private String token;
    private boolean autoDisconnect;
    private BleDevice device;
    private int unlockTimes;
    private int power;
    private String version;
    private int workMode;

    private boolean processing;
    private boolean connected;
    private boolean locked;
    private boolean selected;

    public BlePadlock(String id, String serviceUuid, String name, String macAddress, String token, boolean autoDisconnect) {
        this.id = id;
        this.serviceUuid = serviceUuid;
        this.name = name;
        this.macAddress = macAddress;
        this.token = token;
        this.autoDisconnect = autoDisconnect;

        this.connected = false;
        this.locked = false;
        this.selected = false;
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
    public boolean isAutoDisconnect() {
        return autoDisconnect;
    }
    public void setAutoDisconnect(boolean autoDisconnect) {
        this.autoDisconnect = autoDisconnect;
    }

    public BleDevice getDevice() {
        return device;
    }
    public void setDevice(BleDevice device) {
        this.device = device;
    }
    public int getUnlockTimes() {
        return unlockTimes;
    }
    public void setUnlockTimes(int unlockTimes) {
        this.unlockTimes = unlockTimes;
    }

    public int getPower() {
        return power;
    }
    public void setPower(int power) {
        this.power = power;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public int getWorkMode() {
        return workMode;
    }
    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    public boolean isProcessing() {
        return processing;
    }
    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
    public boolean isConnected() {
        return connected;
    }
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static BlePadlock getPadlock(List<BlePadlock> padlocks, BleDevice device) {
        Optional<BlePadlock> padlock = padlocks.stream()
                .filter(i -> i.getMacAddress().equals(device.getMac()))
                .findFirst();

        return padlock.isPresent() ? padlock.get() : null;
    }

    public void handleResponse(Command command, byte[] data) {
        byte[] data1 = Command.getResponseData(data);

        switch (command.getCmd()) {
            case Command.CMD_QUERY_POWER_PERCENTAGE:
                setPower(data1[0] &255);
                break;
            case Command.CMD_SET_WORK_MODE:
                if (data1[0] == Command.RESULT_SUCCESS)
                    setWorkMode(new Byte(command.getData1()[0]).intValue());
                break;
            case Command.CMD_QUERY_UNLOCK_TIMES:
                int times = Integer.parseInt(BcdUtil.BCDtoString(data1));
                setUnlockTimes(times);
                break;
            case Command.CMD_QUERY_SW_VERSION:
                String version = String.format("%s.%s.%s", data1[0], data1[1], data1[2]);
                setVersion(version);
                break;
            default:
                break;
        }
    }
}
