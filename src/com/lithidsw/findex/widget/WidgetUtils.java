package com.lithidsw.findex.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import com.lithidsw.findex.R;
import com.lithidsw.findex.db.DBUtils;

public class WidgetUtils {

    public static void update(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            if (new DBUtils(context).getWidgetCount() > 0) {
                ComponentName name = new ComponentName(context, WidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(name);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view);
            }
        }
    }
}