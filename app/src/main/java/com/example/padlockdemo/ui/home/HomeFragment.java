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
import com.google.zxing.integration.android.IntentIntegrator;

public class HomeFragment extends Fragment {

    private ListView padlockListView;
    private TextView queueSizeTextView;
    private TextView lastCommandTextView;
    private SwipeRefreshLayout refreshLayout;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    private Button unlockAllButton;
    private Button clearAllButton;
    private Button scanButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        padlockListView = root.findViewById(R.id.listview_device_list);
        queueSizeTextView = root.findViewById(R.id.textview_queue_size);
        lastCommandTextView = root.findViewById(R.id.textview_last_command);
        refreshLayout = root.findViewById(R.id.swipe_device_list);
        unlockAllButton = root.findViewById(R.id.button_unlock_all);
        clearAllButton = root.findViewById(R.id.button_clear_all);
        scanButton = root.findViewById(R.id.button_scan);

        MainActivity.padlocksAdapter = new PadlocksAdapter(getActivity(), MainActivity.blePadlockArrayList);
        padlockListView.setAdapter(MainActivity.padlocksAdapter);

        refreshListener = () -> {
            //refreshLayout.setEnabled(false);
            MainActivity.scan(() -> {
                AsyncUtil.postDelay(getContext(), () -> {
                    refreshLayout.setRefreshing(false);
                    //refreshLayout.setEnabled(true);
                }, 0);
            });
        };
        refreshLayout.setOnRefreshListener(refreshListener);

        unlockAllButton.setOnClickListener(view -> MainActivity.blePadlockArrayList.forEach(i -> {
            if (i.isSelected()) {
                BleRequestManager.getInstance()
                        .add(new BleRequest(getContext(), i, Command.unlock, () -> MainActivity.padlocksAdapter.notifyDataSetChanged()));
            }
        }));

        clearAllButton.setOnClickListener(view -> BleRequestManager.getInstance().clear());

        scanButton.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.setOrientationLocked(true);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan a padlock");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshListener.onRefresh();
    }
}