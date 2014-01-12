package com.lithidsw.findex.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.lithidsw.findex.R;
import com.lithidsw.findex.AddTagActivity;
import com.lithidsw.findex.FileInfoActivity;
import com.lithidsw.findex.MainActivity;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.utils.C;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            WidgetInfo widgetInfo = new DBUtils(context).getWidget(appWidgetId);
            Intent svcIntent = new Intent(context, WidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget);
            widget.setTextViewText(R.id.main_header_tag, widgetInfo.value);
            widget.setRemoteAdapter(R.id.list_view, svcIntent);
            clickHeader(context, widget);
            clickAddNewTag(context, widget);
            clickRefresh(context, widget);
            clickTemp(context, widget);
            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void clickTemp(Context context, RemoteViews widget) {
        Intent clickI = new Intent(context, FileInfoActivity.class);
        PendingIntent clickP = PendingIntent
                .getActivity(context, 0,
                        clickI,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        widget.setPendingIntentTemplate(R.id.list_view, clickP);
    }

    private void clickHeader(Context context, RemoteViews widget) {
        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.setAction(Intent.ACTION_MAIN);
        clickIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent clickPI = PendingIntent
                .getActivity(context, 0,
                        clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        widget.setOnClickPendingIntent(R.id.main_header, clickPI);
    }

    private void clickAddNewTag(Context context, RemoteViews widget) {
        Intent clickIntent = new Intent(context, AddTagActivity.class);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent clickPI = PendingIntent
                .getActivity(context, 0,
                        clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        widget.setOnClickPendingIntent(R.id.widget_add_tag, clickPI);
    }

    private void clickRefresh(Context context, RemoteViews widget) {
        Intent clickIntent = new Intent(C.ACTION_REFRESH);

        PendingIntent clickPI = PendingIntent
                .getBroadcast(context, 0,
                        clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        widget.setOnClickPendingIntent(R.id.widget_refresh, clickPI);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        DBUtils dbUtils = new DBUtils(context);
        for (int item : appWidgetIds) {
            dbUtils.deleteWidget(item);
        }
        super.onDeleted(context, appWidgetIds);
    }

}
