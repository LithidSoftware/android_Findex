package com.lithidsw.findex.ef;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.info.DirPickerInfo;
import com.lithidsw.findex.utils.C;

import java.util.ArrayList;

public class DirectoryManager extends Activity {

    private SharedPreferences mPrefs;

    private GridView mGridView;
    private DirectoryAdapter mAdapter;
    private TextView mTextView;

    ArrayList<DirPickerInfo> mDirs = new ArrayList<DirPickerInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        setTheme(getResources().getIdentifier(
                mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME),
                "style",
                C.THIS)
        );
        setContentView(R.layout.directory_manage);
        mTextView = (TextView) findViewById(R.id.no_content_list);
        mGridView = (GridView) findViewById(R.id.dir_list);
        if (mPrefs.getBoolean(C.PREF_TOGGLE_GRID, false)) {
            mGridView.setNumColumns(getResources().getInteger(R.integer.grid_items));
        } else {
            mGridView.setNumColumns(1);
        }
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                removeDir(mDirs.get(i).dir);
            }
        });
        mAdapter = new DirectoryAdapter(this, mDirs);
        mGridView.setAdapter(mAdapter);

        setupDirs();
        setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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
        }
    }

    private void update() {
        mAdapter.notifyDataSetChanged();

        if (mDirs.size() > 0) {
            mGridView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        } else {
            mGridView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setupDirs() {
        String[] folders = mPrefs.getString(C.PREF_EXCLUDE_FOLDERS, "").split("::");
        mDirs.clear();
        for (String string : folders) {
            if (string.length() > 0) {
                DirPickerInfo info = new DirPickerInfo();
                info.dir = string;
                info.name = string;
                mDirs.add(info);
            }
        }

        update();
    }

    private void removeDir(String dir) {
        String[] folders = mPrefs.getString(C.PREF_EXCLUDE_FOLDERS, "").split("::");
        String directories = "";
        for (String string : folders) {
            if (string.length() > 0) {
                if (!string.equals(dir)) {
                    directories = string + "::";
                }
            }
        }

        mPrefs.edit().putString(C.PREF_EXCLUDE_FOLDERS, directories).commit();
        setupDirs();
    }
}
