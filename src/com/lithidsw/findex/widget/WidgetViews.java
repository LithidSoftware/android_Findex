package com.lithidsw.findex.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.lithidsw.findex.R;
import com.lithidsw.findex.FileInfoActivity;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.info.FileInfo;
import com.lithidsw.findex.loader.ImageLoader;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.FileUtils;

import java.util.ArrayList;

public class WidgetViews implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private ArrayList<FileInfo> mFiles = new ArrayList<FileInfo>();

    private int mThemeStyle;
    private int mAppWidgetId;
    private WidgetInfo mWidgetInfo;

    private AppWidgetManager mAppManager;

    private ImageLoader imageLoader;

    public WidgetViews(Context context, int id) {
        Log.e("Fearch", "Got id: " + id);
        mContext = context;
        mAppWidgetId = id;
        mWidgetInfo = new DBUtils(context).getWidget(mAppWidgetId);
        Log.e("Fearch", "Added id: " + mWidgetInfo.id + " Pos: " + mWidgetInfo.position + " Value: " + mWidgetInfo.value);
        mThemeStyle = R.style.AppThemeDark;
        mAppManager = AppWidgetManager.getInstance(mContext);
        imageLoader = new ImageLoader(context, 100, R.drawable.loader);
        onDataSetChanged();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        updateConvoList();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mFiles != null) {
            return mFiles.size();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

        final String filename = mFiles.get(position).name;
        final String tag = mFiles.get(position).type;
        final String path = mFiles.get(position).path;
        final long modified = mFiles.get(position).modified;
        final long size = mFiles.get(position).size;

        WidgetLoadStub widgetLoadStub = new WidgetLoadStub();
        widgetLoadStub.views = row;
        widgetLoadStub.id = mAppWidgetId;
        widgetLoadStub.manager = mAppManager;

        try {
            row.setTextViewText(R.id.file_name, filename);
            row.setTextViewText(R.id.file_size, FileUtils.humanReadableByteCount(size, false));
            if (path.endsWith(".apk")) {
                row.setImageViewResource(R.id.file_icon, resItem(R.attr.file_default));
                imageLoader.DisplayImage(path, null, widgetLoadStub);
            } else {
                if (tag.contains(C.TAG_PICTURES)) {
                    row.setImageViewResource(R.id.file_icon, resItem(R.attr.file_picture));
                    imageLoader.DisplayImage(path, null, widgetLoadStub);
                } else if (tag.contains(C.TAG_VIDEO)) {
                    row.setImageViewResource(R.id.file_icon, resItem(R.attr.file_video));
                    imageLoader.DisplayImage(path, null, widgetLoadStub);
                } else if (tag.contains(C.TAG_SOUNDS)) {
                    row.setImageViewResource(R.id.file_icon, resItem(R.attr.file_sound));
                    row.setImageViewResource(R.id.file_image, resItem(R.attr.file_sound));
                } else {
                    row.setImageViewResource(R.id.file_icon, resItem(R.attr.file_default));
                    row.setImageViewResource(R.id.file_image, resItem(R.attr.file_default));
                }
            }

            Intent in = new Intent(mContext, FileInfoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("fileinfo", mFiles.get(position));
            in.putExtras(bundle);
            in.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            row.setOnClickFillInIntent(R.id.listview_click, in);

            return(row);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void updateConvoList() {
        mFiles.clear();

        if (mWidgetInfo.value.equalsIgnoreCase("downloads")) {
            mFiles.addAll(new DBUtils(mContext).getDownloadFiles(""));
        } else if (mWidgetInfo.value.equalsIgnoreCase("all")) {
            mFiles.addAll(new DBUtils(mContext).getAllFiles(""));
        } else if (mWidgetInfo.value.equalsIgnoreCase("trash bin")) {
            mFiles.addAll(new DBUtils(mContext).getAllTrashFiles(""));
        } else {
            if (mWidgetInfo.position < C.NUM_MAIN_TITLES) {
                mFiles.addAll(new DBUtils(mContext).getMainTagFiles(mWidgetInfo.value, ""));
            } else {
                mFiles.addAll(new DBUtils(mContext).getCustomTagFiles(mWidgetInfo.value, ""));
            }
        }
    }

    private int resItem(int item) {
        Resources.Theme theme =  mContext.getTheme();
        if (theme != null) {
            TypedArray a = mContext.getTheme().obtainStyledAttributes(mThemeStyle, new int[] {item});
            if (a != null) {
                int attributeResourceId = a.getResourceId(0, 0);
                a.recycle();
                return attributeResourceId;
            }
        }

        return 0;
    }
}
