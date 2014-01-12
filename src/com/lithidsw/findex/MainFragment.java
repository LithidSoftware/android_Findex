package com.lithidsw.findex;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.adapter.FilePageAdapter;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.info.FileInfo;
import com.lithidsw.findex.service.IndexService;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.FileUtils;
import com.lithidsw.findex.utils.MrToast;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    Activity mActivity;
    View mLayout;
    private SharedPreferences mPrefs;
    private GridView mGridView;
    private FilePageAdapter mAdapter;
    private TextView mTextView;

    ArrayList<FileInfo> mFiles = new ArrayList<FileInfo>();
    ArrayList<FileInfo> mSelectedFiles = new ArrayList<FileInfo>();

    private Handler mHandler = new Handler();
    private String mCurrentFolder;
    private int mPosition;
    private MenuItem mSearch;

    private String mCurrentSearch = "";

    private DBUtils dbUtils;
    private FolderLoader mFolderLoader;

    private BroadcastReceiver mReceiver;
    private MrToast mToast;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = super.getActivity();
        dbUtils = new DBUtils(mActivity);
        mToast = new MrToast(mActivity);
        mPrefs = mActivity.getSharedPreferences(C.PREF, Activity.MODE_PRIVATE);
        mLayout = inflater.inflate(R.layout.main_frag, null);
        mCurrentFolder = getArguments().getString("extra_folder", null);
        mPosition = getArguments().getInt("extra_int", 0);
        if (mLayout != null) {

            mGridView = (GridView) mLayout.findViewById(R.id.file_list);
            if (mPrefs.getBoolean(C.PREF_TOGGLE_GRID, false)) {
                mGridView.setNumColumns(mActivity.getResources().getInteger(R.integer.grid_items));
            } else {
                mGridView.setNumColumns(1);
            }
            mAdapter = new FilePageAdapter(mActivity, mFiles, (mPosition == C.NUM_MAIN_TITLES));
            mGridView.setAdapter(mAdapter);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(mActivity, FileInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("fileinfo", mFiles.get(position));
                    intent.putExtras(bundle);
                    mActivity.startActivity(intent);
                    mActivity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
            });

            mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mGridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {}

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.setTitle("Choose threads");
                    MenuInflater inflater = mode.getMenuInflater();
                    if (inflater != null) {
                        inflater.inflate(R.menu.selection_menu, menu);
                        MenuItem mrestore = menu.findItem(R.id.action_restore);
                        MenuItem mtag = menu.findItem(R.id.action_tag);
                        MenuItem mdelete = menu.findItem(R.id.action_delete);

                        if (mPosition == C.NUM_MAIN_TITLES) {
                            if (mtag != null) {
                                mtag.setVisible(false);
                            }
                        } else {
                            if (mrestore != null) {
                                mrestore.setVisible(false);
                            }

                            if (dbUtils.getTagCount() < 1) {
                                if (mtag != null) {
                                    mtag.setVisible(false);
                                }
                            }
                        }

                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    mSelectedFiles.clear();
                    final SparseBooleanArray checked = mGridView.getCheckedItemPositions();
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            if (checked != null) {
                                int size = checked.size();
                                for (int i = 0; i < size; i++) {
                                    int key = checked.keyAt(i);
                                    boolean value = checked.get(key);
                                    if (value) {
                                        mSelectedFiles.add(mFiles.get(key));
                                    }
                                }
                                deleteDialog(mode, mSelectedFiles, false);
                            }
                            return true;
                        case R.id.action_restore:
                            if (checked != null) {
                                int size = checked.size();
                                for (int i = 0; i < size; i++) {
                                    int key = checked.keyAt(i);
                                    boolean value = checked.get(key);
                                    if (value) {
                                        mSelectedFiles.add(mFiles.get(key));
                                    }
                                }
                                deleteDialog(mode, mSelectedFiles, true);
                            }
                            return true;
                        case R.id.action_tag:
                            if (checked != null) {
                                int size = checked.size();
                                for (int i = 0; i < size; i++) {
                                    int key = checked.keyAt(i);
                                    boolean value = checked.get(key);
                                    if (value) {
                                        mSelectedFiles.add(mFiles.get(key));
                                    }
                                }
                                addTagDialog(mode, mSelectedFiles);
                            }
                            return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }
            });
            mTextView = (TextView) mLayout.findViewById(R.id.no_content_list);
            setHasOptionsMenu(true);
            runHandler();
        }
        return mLayout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu.size() > 0) {
            mSearch = menu.findItem(R.id.action_search);
            if (mSearch != null) {
                setupSearch(mSearch);
            }
        }
    }

    private void setupSearch(MenuItem searchItem) {
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                dbUtils = new DBUtils(mActivity);
                mFiles.clear();
                mAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                dbUtils = new DBUtils(mActivity);
                mCurrentSearch = "";
                stopLoader();
                mFolderLoader = (FolderLoader) new FolderLoader().execute(mCurrentFolder, mCurrentSearch);
                return true;
            }
        });
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint("Search: ");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    dbUtils = new DBUtils(mActivity);
                    mFiles.clear();
                    mCurrentSearch = s;
                    stopLoader();
                    mFolderLoader = (FolderLoader) new FolderLoader().execute(mCurrentFolder, mCurrentSearch);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (s.length() < 2) {
                        if (mSearch.isActionViewExpanded()) {
                            dbUtils = new DBUtils(mActivity);
                            mFiles.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                        return true;
                    } else {
                        dbUtils = new DBUtils(mActivity);
                        mFiles.clear();
                        mCurrentSearch = s;
                        stopLoader();
                        mFolderLoader = (FolderLoader) new FolderLoader().execute(mCurrentFolder, mCurrentSearch);
                        return true;
                    }
                }
            });
        }
    }

    private void runHandler() {

        mCurrentSearch = "";
        mFolderLoader = (FolderLoader) new FolderLoader().execute(mCurrentFolder, mCurrentSearch);

        final Runnable runner = new Runnable() {
            public void run() {
                if (mSearch == null) {
                    mHandler.postDelayed(this, 1500);
                } else {
                    if (!mSearch.isActionViewExpanded()) {
                        dbUtils = new DBUtils(mActivity);
                        mCurrentSearch = "";
                        stopLoader();
                        mFolderLoader = (FolderLoader) new FolderLoader().execute(mCurrentFolder, mCurrentSearch);
                        if (isMyServiceRunning()) {
                            mHandler.postDelayed(this, 1500);
                        }
                    }
                }
            }
        };

        if (isMyServiceRunning()) {
            mHandler.postDelayed(runner, 1500);
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
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

    private void addTagDialog(final ActionMode mode, final ArrayList<FileInfo> checkedList) {
        final ArrayList<String[]> list = dbUtils.getCustomTags();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setSingleChoiceItems(list.toArray(new String[list.size()]), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        alertDialogBuilder.setTitle("Add items to tag");
        alertDialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lw = ((AlertDialog)dialog).getListView();
                int checked = lw.getCheckedItemPosition();
                FileUtils.tagFiles(mActivity, list.get(checked)[0], checkedList);
                mode.finish();
                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteDialog(final ActionMode mode, final ArrayList<FileInfo> files, final boolean restore) {
        boolean[] bnames = new boolean[files.size()];
        for (int i = 0; i < files.size(); i++) {
            bnames[i] = true;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        String button;
        if (restore) {
            button = "Restore";
            alertDialogBuilder.setTitle("Restore these items?");
        } else {
            if (mPosition == C.NUM_MAIN_TITLES) {
                button = "Delete";
                alertDialogBuilder.setTitle("Delete these items forever?");
            } else {
                button = "Trash 'em";
                alertDialogBuilder.setTitle("Move these items to trash bin?");
            }
        }

        String[] names = new String[files.size()];
        for (int i=0; i < files.size(); i++) {
            names[i] = files.get(i).name;
        }
        alertDialogBuilder.setMultiChoiceItems(names, bnames, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                mToast.sendShortMessage("Path: " + files.get(which));
            }
        });
        alertDialogBuilder.setPositiveButton(button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SparseBooleanArray checked = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                ArrayList<FileInfo> trash_list = new ArrayList<FileInfo>();
                if (checked != null) {
                    int size = checked.size();
                    for (int i = 0; i < size; i++) {
                        int key = checked.keyAt(i);
                        boolean value = checked.get(key);
                        if (value) {
                            trash_list.add(files.get(key));
                        }
                    }

                    if (restore) {
                        FileUtils.restoreTrash(mActivity, trash_list);
                    } else {
                        if (mPosition == C.NUM_MAIN_TITLES) {
                            FileUtils.deleteFiles(mActivity, trash_list);
                        } else {
                            FileUtils.file2Trash(mActivity, trash_list);
                        }
                    }
                }
                mode.finish();
                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerUpdateItemsListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiver != null) {
            mActivity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void registerUpdateItemsListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String mAction = intent.getAction();
                    if (mAction != null && mAction.equals(C.ACTION_INDEX_COMPLETE)) {
                        stopLoader();
                        mFolderLoader = (FolderLoader) new FolderLoader().execute(mCurrentFolder, mCurrentSearch);
                    }
                }
            };

            IntentFilter mFilter = new IntentFilter(C.ACTION_INDEX_COMPLETE);
            mActivity.registerReceiver(mReceiver, mFilter);
        }
    }

    private void stopLoader() {
        if (mFolderLoader != null
                && mFolderLoader.getStatus() != FolderLoader.Status.FINISHED) {
            mFolderLoader.cancel(true);
            mFolderLoader = null;
        }
    }

    class FolderLoader extends AsyncTask<String, String, ArrayList<FileInfo>> {
        @Override
        protected ArrayList<FileInfo> doInBackground(String... params) {
            if (params[0].equalsIgnoreCase("downloads")) {
                return dbUtils.getDownloadFiles(params[1]);
            } else if (params[0].equalsIgnoreCase("all")) {
                return dbUtils.getAllFiles(params[1]);
            } else if (params[0].equalsIgnoreCase("trash bin")) {
                return dbUtils.getAllTrashFiles(params[1]);
            } else {
                if (mPosition < C.NUM_MAIN_TITLES) {
                    return dbUtils.getMainTagFiles(params[0], params[1]);
                } else {
                    return dbUtils.getCustomTagFiles(params[0], params[1]);
                }
            }
        }

        @Override
        protected void onPostExecute(ArrayList<FileInfo> list) {
            if (list.size() > 0) {
                mFiles.clear();
                mFiles.addAll(list);
                mAdapter.notifyDataSetChanged();
                mGridView.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);
            } else {
                mTextView.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.GONE);
            }
        }
    }
}
