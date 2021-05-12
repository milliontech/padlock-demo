package com.example.padlockdemo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.padlockdemo.MainActivity;
import com.example.padlockdemo.R;
import com.example.padlockdemo.adapter.PadlocksAdapter;
import com.example.padlockdemo.manager.BleRequestManager;
import com.example.padlockdemo.model.BleRequest;
import com.example.padlockdemo.model.Command;
import com.example.padlockdemo.util.AsyncUtil;

public class HomeFragment extends Fragment {

    private ListView padlockListView;
    private TextView queueSizeTextView;
    private SwipeRefreshLayout refreshLayout;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    private Button unlockAllButton;
    private Button clearAllButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        padlockListView = root.findViewById(R.id.listview_device_list);
        queueSizeTextView = root.findViewById(R.id.textview_queue_size);
        refreshLayout = root.findViewById(R.id.swipe_device_list);
        unlockAllButton = root.findViewById(R.id.button_unlock_all);
        clearAllButton = root.findViewById(R.id.button_clear_all);

        MainActivity.padlocksAdapter = new PadlocksAdapter(getActivity(), MainActivity.blePadlockArrayList);
        padlockListView.setAdapter(MainActivity.padlocksAdapter);

        refreshListener = () -> {
            refreshLayout.setEnabled(false);
            MainActivity.scan(() -> {
                AsyncUtil.postDelay(getContext(), () -> {
                    refreshLayout.setRefreshing(false);
                    refreshLayout.setEnabled(true);
                }, 0);
            });
        };
        refreshLayout.setOnRefreshListener(refreshListener);

        unlockAllButton.setOnClickListener(view -> MainActivity.blePadlockArrayList.forEach(i -> {
            BleRequestManager.getInstance()
                    .add(new BleRequest(getContext(), i, Command.unlock, () -> MainActivity.padlocksAdapter.notifyDataSetChanged()));
        }));

        clearAllButton.setOnClickListener(view -> BleRequestManager.getInstance().clear());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshListener.onRefresh();
    }
}