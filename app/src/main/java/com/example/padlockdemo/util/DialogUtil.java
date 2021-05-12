package com.example.padlockdemo.util;

import android.app.ProgressDialog;
import android.content.Context;

public class DialogUtil {

    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Loading...");
        dialog.setCancelable(true);
        return dialog;
    }
}
