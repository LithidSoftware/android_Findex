package com.lithidsw.findex.utils;

import android.content.Context;
import android.widget.Toast;

public class MrToast {

    Context mContext;
    Toast mToast;

    public MrToast(Context context) {
        mContext = context;
    }

    public void sendShortMessage(String message) {
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void sendLongMessage(String message) {
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
        mToast.show();
    }
}
