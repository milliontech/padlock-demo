package com.example.padlockdemo.model;

import android.content.Intent;
import android.util.Log;

import com.clj.fastble.data.BleDevice;
import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.util.BcdUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BlePadlock {
    private final static String TAG = BlePadlock.class.getSimpleName();

    private String model;
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

    public BlePadlock(String model, String id, String serviceUuid, String name, String macAddress, String token, boolean autoDisconnect) {
        this.model = model;
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

    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
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
            case Command.CMD_UNLOCK:
                boolean isLocked = Command.isLocked(data);
                setLocked(isLocked);
                break;
            case Command.CMD_QUERY_POWER_PERCENTAGE:
                setPower(data1[0] &255);
                break;
            case Command.CMD_SET_WORK_MODE:
                if (data1[0] == Command.RESULT_SUCCESS)
                    setWorkMode(new Byte(command.getData1()[0]).intValue());
                break;
            case Command.CMD_QUERY_CURRENT_ALL_STATUS_INFO:
                boolean isLockBeamOpen = data1[0] == Command.RESULT_SUCCESS;
                int workMode = data1[1] &255;
                int powerLevel = data1[2] &255;
                int signalLevel = data1[3] &255;
                String other = String.format("%8s", Integer.toBinaryString(data1[4] & 0xFF)).replace(' ', '0');;
                boolean _LockBeamOpen = "0".equals(other.charAt(7));
                boolean _isBluetoothOn = "1".equals(other.charAt(6));
                boolean _isNfcOn = !"0".equals(other.charAt(2));
                setPower(powerLevel * 20);
                break;
            case Command.CMD_QUERY_UNLOCK_TIMES:
                int times = Integer.parseInt(BcdUtil.BCDtoString(data1));
                setUnlockTimes(times);
                break;
            case Command.CMD_QUERY_SW_VERSION:
                String version = String.format("%s.%s.%s", data1[0], data1[1], data1[2]);
                setVersion(version);
                break;
            case Command.CMD_SET_BUILTIN_CLOCK:
                break;
            case Command.CMD_QUERY_TIME_INFO:
                Date time = Command.getCalendarFromMilliseconds(Command.getMillisecondsFromBytes(data1)).getTime();
                String timeText = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time.getTime());
                if (data[0] == 0x01) { // Clock current time
                    MainActivity.currentActivity
                            .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                                    .setAction("onMessage").putExtra("Message",
                                            String.format("Current :%s",  timeText)));
                } else if (data[0] == 0x02) { // Last locking time
                    MainActivity.currentActivity
                            .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                                    .setAction("onMessage").putExtra("Message",
                                            String.format("Last: %s",  timeText)));
                }
                break;
            case Command.CMD_QUERY_SIM_CARD_INFO:
                String code = BcdUtil.BCDtoString(data1);
                if (data[0] == 0x01) { // ICCID code
                    MainActivity.currentActivity
                            .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                                    .setAction("onMessage").putExtra("Message",
                                            String.format("ICCID: %s",  code)));
                } else if (data[0] == 0x02) { // Last locking time
                    MainActivity.currentActivity
                            .sendBroadcast(new Intent(MainActivity.currentActivity, PadlockReceiver.class)
                                    .setAction("onMessage").putExtra("Message",
                                            String.format("IMEI: %s",  code)));
                }
                break;
            case Command.CMD_QUERY_LOCK_BEAM_STATUS:
                if (getModel() == Command.B101)
                    setLocked(data1[0] == Command.RESULT_SUCCESS);
                else if (getModel() == Command.C102)
                    setLocked(data1[0] != Command.RESULT_SUCCESS);
            default:
                Log.d(TAG, String.format("Not handle command response %s", command));
                break;
        }
    }
}
