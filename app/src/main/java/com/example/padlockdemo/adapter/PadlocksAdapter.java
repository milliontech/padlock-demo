package com.example.padlockdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.model.BlePadlock;
import com.example.padlockdemo.model.Command;
import com.example.padlockdemo.util.PadlockUtil;

import java.util.ArrayList;

public class PadlocksAdapter extends ArrayAdapter<BlePadlock> {

    private static class ViewHolder {
        CheckBox selected;
        ImageView status;
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
            viewHolder.status = (ImageView) convertView.findViewById(R.id.imageview_item_padlock_status);
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

        int color = ContextCompat.getColor(getContext(), R.color.light_grey);
        if (padlock.getDevice() != null && padlock.isConnected()) {
            color = ContextCompat.getColor(getContext(), R.color.light_green);
        }
        viewHolder.status.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);

        if (viewHolder.status.getAnimation() == null) {
            Animation animation = new AlphaAnimation(1, 0);
            animation.setDuration(1000);
            animation.setInterpolator(new LinearInterpolator());
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.REVERSE);
            viewHolder.status.setAnimation(animation);
        }
        if (padlock.isProcessing()) {
            viewHolder.status.getAnimation().start();
        } else {
            viewHolder.status.getAnimation().cancel();
        }

        viewHolder.id.setText(padlock.getId());
        viewHolder.name.setText(padlock.getName());
        if (padlock.getDevice() != null && !padlock.isProcessing()) {
            //viewHolder.name.setText(padlock.getName() + (padlock.isLocked() ? " (Locked)" : " (Unlocked)") );
        }
        viewHolder.macAddress.setText(padlock.getMacAddress());
        viewHolder.connectButton.setEnabled(padlock.getDevice() != null);
        viewHolder.unlockButton.setEnabled(padlock.getDevice() != null && padlock.isLocked());

        viewHolder.unlockButton.setOnClickListener(view -> {
            padlock.setProcessing(true);
            PadlockUtil.unlock(
                    getContext(),
                    padlock,
                    data -> {
                        if (Command.isRequestSuccess(data)) {
                            MainActivity.showMessage("Unlock successfully!");
                        }
                        padlock.setProcessing(false);
                        notifyDataSetChanged();
                        return true;
                    },
                    error -> {
                        MainActivity.showMessage(error);
                        padlock.setProcessing(false);
                        return true;
                    });
        });

        return convertView;
    }
}
