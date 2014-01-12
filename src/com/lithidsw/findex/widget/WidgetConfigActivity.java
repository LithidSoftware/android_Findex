package com.lithidsw.findex.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.adapter.WidgetListAdapter;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.utils.C;

import java.util.ArrayList;

public class WidgetConfigActivity extends Activity  {

    private SharedPreferences mPrefs;
    private Context mContext;
    private WidgetListAdapter mAdapter;
    private ListView mListView;
    private LinearLayout mProgress;
    private LinearLayout mListLayout;
    private ArrayList<String> mTags = new ArrayList<String>();
    private int mAppWidgetId;

    private DBUtils dbUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        setTheme(getResources().getIdentifier(
                mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME),
                "style",
                C.THIS)
        );

        setContentView(R.layout.widget_config_activity);
        mContext = this;
        dbUtils = new DBUtils(mContext);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        setupActionBar();

        mListLayout = (LinearLayout) findViewById(R.id.widget_config_list_view);
        mListView = (ListView) findViewById(R.id.widget_config_list);
        mProgress = (LinearLayout) findViewById(R.id.widget_config_list_progress);
        new WidgetTagLoader().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_CANCELED, resultValue);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_CANCELED, resultValue);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setIcon(getResources().getDrawable(R.drawable.ic_title_icon));
        }
    }

    private void updateList() {
        if (mTags.size() > 0) {
            mAdapter = new WidgetListAdapter(mContext, mTags);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    WidgetInfo widgetInfo = new WidgetInfo();
                    widgetInfo.id = mAppWidgetId;
                    widgetInfo.position = position;
                    widgetInfo.value = mTags.get(position);
                    dbUtils.addWidget(widgetInfo);
                    Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                            WidgetConfigActivity.this, WidgetProvider.class);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {mAppWidgetId});
                    sendBroadcast(intent);

                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                }
            });
            mListLayout.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        }
    }

    class WidgetTagLoader extends AsyncTask<String, String, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... params) {
            return dbUtils.getWidgetConfigTags();
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            mTags.clear();
            mTags.addAll(list);
            updateList();
        }
    }
}
