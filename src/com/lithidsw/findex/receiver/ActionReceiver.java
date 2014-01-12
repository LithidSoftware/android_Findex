package com.lithidsw.findex.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lithidsw.findex.service.IndexService;
import com.lithidsw.findex.utils.C;

import java.util.List;

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent.getAction() != null)) {
            if (intent.getAction().equals(C.ACTION_REFRESH) ||
                    intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                if (!isMyServiceRunning(context)) {
                    context.startService(new Intent(context, IndexService.class));
                }
            }
        }
    }

    private boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> list = manager.getRunningServices(Integer.MAX_VALUE);
            if (list != null) {
                for (ActivityManager.RunningServiceInfo service : list) {
                    if (IndexService.class.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
