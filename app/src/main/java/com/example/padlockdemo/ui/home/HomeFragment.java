package com.example.padlockdemo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.clj.fastble.BleManager;
import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.adapter.PadlocksAdapter;
import com.example.padlockdemo.util.AsyncUtil;

public class HomeFragment extends Fragment {

    private ListView padlockListView;
    private SwipeRefreshLayout refreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        padlockListView = root.findViewById(R.id.listview_device_list);
        refreshLayout = root.findViewById(R.id.swipe_device_list);

        MainActivity.padlocksAdapter = new PadlocksAdapter(getActivity(), MainActivity.blePadlockArrayList);
        padlockListView.setAdapter(MainActivity.padlocksAdapter);

        refreshLayout.setOnRefreshListener(() -> MainActivity.scan(() -> {
            AsyncUtil.postDelay(getContext(), () -> {
                refreshLayout.setRefreshing(false);
            }, 0);
        }));

        return root;
    }
}