package com.example.padlockdemo.adapter;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.model.BluetoothPadlock;
import com.example.padlockdemo.util.AsyncUtil;
import com.example.padlockdemo.util.PadlockUtil;

import java.util.ArrayList;

public class PadlocksAdapter extends ArrayAdapter<BluetoothPadlock> {

    private static class ViewHolder {
        TextView id;
        TextView name;
        TextView macAddress;
        TextView lastCommand;
        Button connectButton;
    }

    public PadlocksAdapter(Context context, ArrayList<BluetoothPadlock> padlocks) {
        super(context, R.layout.item_padlock, padlocks);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BluetoothPadlock padlock = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_padlock, parent, false);
            viewHolder.id = (TextView) convertView.findViewById(R.id.textview_id);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textview_name);
            viewHolder.macAddress = (TextView) convertView.findViewById(R.id.textview_mac_address);
            viewHolder.lastCommand = (TextView) convertView.findViewById(R.id.textview_last_command);
            viewHolder.connectButton = (Button) convertView.findViewById(R.id.button_connect);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.id.setText(padlock.getId());
        viewHolder.name.setText(padlock.getName());
        viewHolder.macAddress.setText(padlock.getMacAddress());
        viewHolder.connectButton.setEnabled(padlock.getBluetoothDevice() != null);
        viewHolder.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                padlock.getBluetoothDevice().connectGatt(getContext(), false, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);

                        switch (newState) {
                            case BluetoothProfile.STATE_CONNECTED:
                                AsyncUtil.postDelay(getContext(), () -> {
                                    viewHolder.connectButton.setEnabled(false);
                                    notifyDataSetChanged();
                                }, 0);
                                MainActivity.showMessage("Connected to GATT server.");
                                gatt.discoverServices();
                                break;
                            case BluetoothProfile.STATE_DISCONNECTED:
                                MainActivity.showMessage("Disconnected from GATT server.");
                            default:
                                AsyncUtil.postDelay(getContext(), () -> {
                                    viewHolder.connectButton.setEnabled(true);
                                    notifyDataSetChanged();
                                }, 0);
                                break;
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);

                        switch (status) {
                            case BluetoothGatt.GATT_SUCCESS:
                                MainActivity.showMessage("Unlocking device...");
                                PadlockUtil.unlock(getContext(), gatt, padlock, (data) -> {
                                    MainActivity.showMessage("Success");
                                    gatt.disconnect();
                                    AsyncUtil.postDelay(getContext(), () -> {
                                        viewHolder.lastCommand.setText(data);
                                        notifyDataSetChanged();
                                        MainActivity.clipboardManager.setText(data);
                                    }, 0);
                                    return true;
                                }, (data) -> {
                                    MainActivity.showMessage("Fail");
                                    gatt.disconnect();
                                    AsyncUtil.postDelay(getContext(), () -> {
                                        viewHolder.lastCommand.setText(data);
                                        notifyDataSetChanged();
                                        MainActivity.clipboardManager.setText(data);
                                    }, 0);
                                    return true;
                                });
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        });

        return convertView;
    }
}
