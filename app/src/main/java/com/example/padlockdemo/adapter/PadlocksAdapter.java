package com.example.padlockdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.padlockdemo.R;
import com.example.padlockdemo.manager.BleRequestManager;
import com.example.padlockdemo.model.BlePadlock;
import com.example.padlockdemo.model.BleRequest;
import com.example.padlockdemo.model.Command;

import java.util.ArrayList;

public class PadlocksAdapter extends ArrayAdapter<BlePadlock> {

    private boolean animate = false;

    private static class ViewHolder {
        CheckBox selected;
        ImageView status;
        TextView id;
        TextView name;
        TextView macAddress;
        TextView unlockTimes;
        TextView power;
        TextView version;
        Button connectButton;
        Button unlockButton;
        ImageView lockIcon;
    }

    public PadlocksAdapter(Context context, ArrayList<BlePadlock> padlocks) {
        super(context, R.layout.item_padlock, padlocks);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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
            viewHolder.unlockTimes = (TextView) convertView.findViewById(R.id.textview_unlock_times);
            viewHolder.power = (TextView) convertView.findViewById(R.id.textview_power);
            viewHolder.version = (TextView)  convertView.findViewById(R.id.textview_version);
            viewHolder.connectButton = (Button) convertView.findViewById(R.id.button_padlock_connect);
            viewHolder.unlockButton = (Button) convertView.findViewById(R.id.button_padlock_unlock);
            viewHolder.lockIcon = (ImageView) convertView.findViewById(R.id.imageview_item_lock_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        int color = ContextCompat.getColor(getContext(), R.color.light_grey);
        if (padlock.getDevice() != null && padlock.isConnected()) {
            color = ContextCompat.getColor(getContext(), R.color.light_green);
        }
        viewHolder.status.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);

        if (animate) {
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
        }
        if (padlock.isProcessing()) {
            convertView.setBackgroundColor(Color.YELLOW);
            convertView.getBackground().setAlpha(255/2);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
            convertView.getBackground().setAlpha(255/1);
        }

        viewHolder.id.setText(padlock.getId());
        viewHolder.name.setText(String.format("%s [ %s ]", padlock.getName(), padlock.getModel()));
        viewHolder.macAddress.setText(padlock.getMacAddress());
        viewHolder.unlockTimes.setText(String.valueOf(padlock.getUnlockTimes()));
        viewHolder.power.setText(String.valueOf(padlock.getPower()) + "%");
        viewHolder.version.setText(padlock.getVersion());
        viewHolder.connectButton.setEnabled(padlock.getDevice() != null);
        viewHolder.unlockButton.setEnabled(padlock.getDevice() != null);

        viewHolder.selected.setOnCheckedChangeListener((compoundButton, b) -> padlock.setSelected(b));

        viewHolder.lockIcon.setImageResource(
                padlock.isLocked() ? R.drawable.lock : R.drawable.lock_open
        );
        viewHolder.lockIcon.setColorFilter(
                padlock.isLocked() ? Color.BLACK : Color.RED
        );

        viewHolder.unlockButton.setOnClickListener(view -> {
            BleRequestManager.getInstance()
                    .add(new BleRequest(getContext(), padlock, Command.unlock, () -> notifyDataSetChanged()));

        });
        viewHolder.name.setOnLongClickListener(view -> {
            Command command = Command.setWorkMode;
            command.setData1(new byte[] { Command.DATA_IDLE_MODE });
            BleRequestManager.getInstance()
                    .add(new BleRequest(getContext(), padlock, command, () -> notifyDataSetChanged()));

            return true;
        });

        return convertView;
    }
}
