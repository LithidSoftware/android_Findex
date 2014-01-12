package com.lithidsw.findex;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.adapter.DrawerListAdapter;
import com.lithidsw.findex.adapter.StorageListAdapter;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.service.IndexService;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.MrToast;
import com.lithidsw.findex.utils.StorageOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private SharedPreferences mPrefs;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerLin;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle;
    private String[] mMainTitles;
    private ArrayList<String[]> mCustomTags = new ArrayList<String[]>();
    private DrawerListAdapter mAdapter;

    private ActionBar actionBar;
    private Handler mHandler = new Handler();
    LayoutInflater mLayoutInflater;

    private MrToast mrToast;
    private BroadcastReceiver mReceiver;
    private String mTheme;
    private Boolean mGridToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        mTheme = mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME);
        mGridToggle = mPrefs.getBoolean(C.PREF_TOGGLE_GRID, false);
        setTheme(getResources().getIdentifier(mTheme, "style", C.THIS));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StorageOptions.determineStorageOptions(this);
        mTitle = getTitle();
        mrToast = new MrToast(this);
        mLayoutInflater = getLayoutInflater();
        mMainTitles = this.getResources().getStringArray(R.array.main_titles);
        mAdapter = new DrawerListAdapter(this, mCustomTags);
        mDrawerLin = (LinearLayout) findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.list_drawer);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > C.NUM_MAIN_TITLES) {
                    removeTagDialog(position);
                }
                return true;
            }
        });

        updateStorageStats();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            setupActionBar();
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    R.drawable.ic_drawer,
                    R.string.drawer_open,
                    R.string.drawer_close
            ) {
                public void onDrawerClosed(View view) {
                    if (mDrawerList != null) {
                        int position = mDrawerList.getCheckedItemPosition();
                        String name;
                        if (position < C.NUM_MAIN_TITLES) {
                            name = mMainTitles[position];
                        } else {
                            name = mCustomTags.get(position)[0];
                        }
                        setTitle(name);
                        invalidateOptionsMenu();
                    } else {
                        setTitle(mTitle);
                    }
                }

                public void onDrawerOpened(View drawerView) {
                    setTitle(mTitle);
                    setProgressBarIndeterminateVisibility(false);
                    invalidateOptionsMenu();
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        if (!isMyServiceRunning()) {
            this.startService(new Intent(this, IndexService.class));
        }

        if (savedInstanceState == null) {
            selectItem(0);
        }

        if (!mPrefs.getBoolean(C.PREF_FIRST_RUN, false)) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
        } else {
            new UpdateCustomTags().execute();
        }
    }

    private void updateStorageStats() {
        StorageOptions.determineStorageOptions(this);
        StorageListAdapter mStorageAdapter = new StorageListAdapter(
                this, StorageOptions.paths, StorageOptions.labels
        );
        ListView mStorageList = (ListView) findViewById(R.id.storage_list);
        mStorageList.setAdapter(mStorageAdapter);
    }

    private void setupActionBar() {
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void updateDrawer() {
        int count = 0;
        for (String string : mMainTitles) {
            String[] strings = new String[3];
            strings[0] = string;
            strings[1] = "";
            strings[2] = string.toLowerCase();
            mCustomTags.add(count, strings);
            count++;
        }
        mAdapter.notifyDataSetChanged();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Fragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        String name;
        if (position < C.NUM_MAIN_TITLES) {
            name = mMainTitles[position];
        } else {
            name = mCustomTags.get(position)[0];
        }
        bundle.putString("extra_folder", name);
        bundle.putInt("extra_int", position);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        if (mDrawerList != null) {
            mDrawerList.setItemChecked(position, true);
            if (mDrawerLayout != null) {

                final Runnable runner = new Runnable() {
                    public void run() {
                        mDrawerLayout.closeDrawer(mDrawerLin);
                    }
                };

                mHandler.postDelayed(runner, 500);
            }
        }
        invalidateOptionsMenu();
    }

    private void sortDialog() {
        int sort = mPrefs.getInt("pref_sort", 0);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        String[] items = this.getResources().getStringArray(R.array.sort_entries);
        alertDialogBuilder
                .setTitle("Sort dialog")
                .setSingleChoiceItems(items, sort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPrefs.edit().putInt("pref_sort", which).commit();
                        selectItem(mDrawerList.getCheckedItemPosition());
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void removeTagDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("Delete tag dialog")
                .setMessage("Delete tag " + mCustomTags.get(position)[0])
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new DBUtils(MainActivity.this).deleteTag(mCustomTags.get(position)[0]);
                        new UpdateCustomTags().execute();
                        if (mDrawerList.getCheckedItemPosition() == position) {
                            selectItem(position - 1);
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void refreshTheme() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerLayout != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
                case R.id.action_add_tag:
                    if (mCustomTags.size() < 10) {
                        startActivity(new Intent(MainActivity.this, AddTagActivity.class));
                    } else {
                        mrToast.sendLongMessage("Enable pro to access unlimited amount of tags!");
                    }
                    return true;
                case R.id.action_sort:
                    sortDialog();
                    return true;
                case R.id.action_help:
                    startActivity(new Intent(this, IntroActivity.class));
                    return true;
                case R.id.action_settings:
                    mTheme = mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME);
                    mGridToggle = mPrefs.getBoolean(C.PREF_TOGGLE_GRID, false);
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerLayout != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerLayout != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
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

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String cur = mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME);
        if (!mTheme.equals(cur)) {
            refreshTheme();
        } else {
            updateStorageStats();
            new UpdateCustomTags().execute();
            mAdapter.notifyDataSetChanged();
            if (!isMyServiceRunning()) {
                this.startService(new Intent(this, IndexService.class));
            }
            registerUpdateItemsListener();
        }

        if(mPrefs.getBoolean(C.PREF_TOGGLE_GRID, false) != mGridToggle) {
            selectItem(mDrawerList.getCheckedItemPosition());
        }
    }

    private void registerUpdateItemsListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String mAction = intent.getAction();
                    if (mAction != null && mAction.equals(C.ACTION_INDEX_COMPLETE)) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            };

            IntentFilter mFilter = new IntentFilter(C.ACTION_INDEX_COMPLETE);
            registerReceiver(mReceiver, mFilter);
        }
    }


    class UpdateCustomTags extends AsyncTask<String, String, ArrayList<String[]>> {
        @Override
        protected ArrayList<String[]> doInBackground(String... params) {
            return new DBUtils(MainActivity.this).getCustomTags();
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> list) {
            if (list != null) {
                mCustomTags.clear();
                mCustomTags.addAll(list);
                updateDrawer();
            }
        }
    }
}
