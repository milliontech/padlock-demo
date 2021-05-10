package com.example.padlockdemo.adapter;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.model.BlePadlock;
import com.example.padlockdemo.model.Command;
import com.example.padlockdemo.util.AsyncUtil;
import com.example.padlockdemo.util.PadlockUtil;
import com.example.padlockdemo.util.StringUtil;

import java.util.ArrayList;
import java.util.UUID;

public class PadlocksAdapter extends ArrayAdapter<BlePadlock> {

    private static class ViewHolder {
        CheckBox selected;
        TextView id;
        TextView name;
        TextView macAddress;
        TextView lastCommand;
        Button connectButton;
        Button unlockButton;
    }

    public PadlocksAdapter(Context context, ArrayList<BlePadlock> padlocks) {
        super(context, R.layout.item_padlock, padlocks);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BlePadlock padlock = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_padlock, parent, false);
            viewHolder.selected = (CheckBox) convertView.findViewById(R.id.checkbox_item_padlock_select);
            viewHolder.id = (TextView) convertView.findViewById(R.id.textview_id);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textview_name);
            viewHolder.macAddress = (TextView) convertView.findViewById(R.id.textview_mac_address);
            viewHolder.lastCommand = (TextView) convertView.findViewById(R.id.textview_last_command);
            viewHolder.connectButton = (Button) convertView.findViewById(R.id.button_padlock_connect);
            viewHolder.unlockButton = (Button) convertView.findViewById(R.id.button_padlock_unlock);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.id.setText(padlock.getId());
        viewHolder.name.setText(padlock.getName());
        if (padlock.getDevice() != null && !padlock.isProccessing()) {
            //viewHolder.name.setText(padlock.getName() + (padlock.isLocked() ? " (Locked)" : " (Unlocked)") );
        }
        viewHolder.macAddress.setText(padlock.getMacAddress());
        viewHolder.connectButton.setEnabled(padlock.getDevice() != null);
        viewHolder.unlockButton.setEnabled(padlock.getDevice() != null);

        viewHolder.unlockButton.setOnClickListener(view -> {
            padlock.setProccessing(true);
            PadlockUtil.unlock(
                    getContext(),
                    padlock,
                    data -> {
                        if (Command.isRequestSuccess(data)) {
                            MainActivity.showMessage("Unlock successfully!");
                        }
                        padlock.setProccessing(false);
                        return true;
                    },
                    error -> {
                        MainActivity.showMessage(error);
                        padlock.setProccessing(false);
                        return true;
                    });
        });

        return convertView;
    }
}
