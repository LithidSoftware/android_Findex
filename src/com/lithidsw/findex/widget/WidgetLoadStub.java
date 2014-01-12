package com.lithidsw.findex.widget;

import android.appwidget.AppWidgetManager;
import android.widget.RemoteViews;

import java.io.Serializable;

public class WidgetLoadStub implements Serializable {
    public RemoteViews views;
    public int id;
    public AppWidgetManager manager;
}
