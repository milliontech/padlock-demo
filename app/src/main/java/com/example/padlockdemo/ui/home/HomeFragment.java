package com.example.padlockdemo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.adapter.PadlocksAdapter;

public class HomeFragment extends Fragment {

    private ListView padlockListView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        padlockListView = root.findViewById(R.id.deviceList);
        MainActivity.padlocksAdapter = new PadlocksAdapter(getActivity(), MainActivity.blePadlockArrayList);
        padlockListView.setAdapter(MainActivity.padlocksAdapter);

        return root;
    }
}