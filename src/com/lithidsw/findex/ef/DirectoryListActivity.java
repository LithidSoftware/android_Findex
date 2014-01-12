package com.lithidsw.findex.ef;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.info.DirPickerInfo;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.StorageOptions;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class DirectoryListActivity extends Activity {

    private SharedPreferences mPrefs;

    private GridView mGridView;
    private DirectoryAdapter mAdapter;
    private TextView mTextView;
    private TextView mTextCurDir;

    ArrayList<DirPickerInfo> mDirs = new ArrayList<DirPickerInfo>();
    String mCurDir;
    int mCurDirInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        setTheme(getResources().getIdentifier(
                mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME),
                "style",
                C.THIS)
        );
        setContentView(R.layout.directory_list_activity);
        setupActionBar();
        StorageOptions.determineStorageOptions(this);
        mTextView = (TextView) findViewById(R.id.no_content_list);
        mTextCurDir = (TextView) findViewById(R.id.cur_dir);
        mGridView = (GridView) findViewById(R.id.dir_list);
        if (mPrefs.getBoolean(C.PREF_TOGGLE_GRID, false)) {
            mGridView.setNumColumns(getResources().getInteger(R.integer.grid_items));
        } else {
            mGridView.setNumColumns(1);
        }
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                new DirList().execute(mDirs.get(i).dir);
            }
        });
        mAdapter = new DirectoryAdapter(this, mDirs);
        mGridView.setAdapter(mAdapter);
        new DirList().execute("/");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dir_picker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_back:
                backDir();
                return true;
            case R.id.action_accept:
                new DBUtils(this).deleteAllCommonPath(mCurDir);
                mPrefs.edit().putString(C.PREF_EXCLUDE_FOLDERS, setCurDir()).commit();
                startActivity(new Intent(DirectoryListActivity.this, DirectoryManager.class));
                return true;
            case R.id.action_manage:
                startActivity(new Intent(DirectoryListActivity.this, DirectoryManager.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String setCurDir() {
        String str = mPrefs.getString(C.PREF_EXCLUDE_FOLDERS, "");
        String string = (String) mTextCurDir.getText();
        return string + "::" + str;
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void backDir() {
        if (mCurDirInt == 0) {
            return;
        }

        if (mCurDirInt == 1) {
            new DirList().execute("/");
        } else {
            mCurDirInt--;
            File file = new File(mCurDir);
            new DirList().execute(file.getParent());
        }
    }

    private void setupStorage() {
        mDirs.clear();
        for (String string : StorageOptions.paths) {
            DirPickerInfo info = new DirPickerInfo();
            info.name = string;
            info.dir = string;
            mDirs.add(info);
        }
    }

    private void updateView() {
        if (mDirs.size() > 0) {
            mTextView.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
        }

        mTextCurDir.setText(mCurDir);
        mAdapter.notifyDataSetChanged();
    }

    private ArrayList<DirPickerInfo> getDirs(String path) {
        if (!path.equals(mCurDir)) {
            if (mCurDir!= null && path.length() > mCurDir.length()) {
                mCurDirInt++;
            }
        }
        mCurDir = path;
        ArrayList<DirPickerInfo> list = new ArrayList<DirPickerInfo>();
        if (!"/".equals(path)) {
            File file = new File(path);
            String[] directories = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            });

            if (directories != null) {
                for (String string : directories) {
                    DirPickerInfo info = new DirPickerInfo();
                    info.name = string;
                    info.dir = path+"/"+string;
                    list.add(info);
                }
            }
        } else {
            mCurDirInt = 0;
            setupStorage();
        }

        return list;
    }

    class DirList extends AsyncTask<String, String, ArrayList<DirPickerInfo>> {
        @Override
        protected ArrayList<DirPickerInfo> doInBackground(String... cur) {
            return getDirs(cur[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<DirPickerInfo> list) {
            if (list.size() > 0) {
                mDirs.clear();
                mDirs.addAll(list);
            }

            updateView();
        }
    }

}
