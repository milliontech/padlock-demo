package com.example.padlockdemo.ui.home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.adapter.PadlocksAdapter;
import com.example.padlockdemo.model.BluetoothPadlock;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    public static PadlocksAdapter padlocksAdapter;
    private ListView padlockListView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        padlockListView = root.findViewById(R.id.deviceList);
        padlocksAdapter = new PadlocksAdapter(getActivity(), MainActivity.bluetoothPadlockList);
        padlockListView.setAdapter(padlocksAdapter);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.bluetoothPadlockList.add(new BluetoothPadlock("868315518395127", MainActivity.serviceUuid, MainActivity.name, "D5:3B:F9:45:27:58", "00000000"));
        MainActivity.bluetoothPadlockList.add(new BluetoothPadlock("868315518395770", MainActivity.serviceUuid, MainActivity.name, "F1:F2:80:90:81:29", "a716c2f1"));
        MainActivity.bluetoothPadlockList.add(new BluetoothPadlock("868315518397131", MainActivity.serviceUuid, MainActivity.name, "C3:17:99:9C:49:AF", "9059eea5"));

        padlocksAdapter.notifyDataSetChanged();
    }
}