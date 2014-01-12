package com.lithidsw.findex;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.adapter.InfoTagListAdapter;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.info.FileInfo;
import com.lithidsw.findex.loader.ImageLoader;
import com.lithidsw.findex.utils.DateBuilder;
import com.lithidsw.findex.utils.FileStartActivity;
import com.lithidsw.findex.utils.FileUtils;
import com.lithidsw.findex.utils.MrToast;

import java.io.File;
import java.util.ArrayList;

public class FileInfoActivity extends Activity  {

    private Context mContext;
    private EditText editText;
    private ListView mListView;
    private InfoTagListAdapter mAdapter;
    private TextView mNoListText;
    private TextView mFileName;
    private LinearLayout mFileNameLayout;

    private FileInfo mFileInfo;
    private String filename;
    private String path;

    private ArrayList<String[]> mTags = new ArrayList<String[]>();
    private boolean[] mBools;
    private MrToast mrToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_info_activity);
        mContext = this.getApplicationContext();
        mrToast = new MrToast(mContext);
        ImageLoader mImageLoader = new ImageLoader(mContext, 400, R.drawable.loader);
        mTags = new DBUtils(mContext).getCustomTags();
        mNoListText = (TextView) findViewById(R.id.info_tags_no_list);
        mFileName = (TextView) findViewById(R.id.file_name);
        editText = (EditText) findViewById(R.id.rename_edit);
        mFileNameLayout = (LinearLayout) findViewById(R.id.file_name_layout);
        mFileNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFileName.setVisibility(View.GONE);
                editText.setVisibility(View.VISIBLE);
            }
        });
        mListView = (ListView) findViewById(R.id.info_tags_list);
        setupActionBar();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mFileInfo = (FileInfo) bundle.getSerializable("fileinfo");
            if (mFileInfo != null) {
                filename = mFileInfo.name;
                path = mFileInfo.path;
                updateList();

                ImageView mImageView = (ImageView) findViewById(R.id.info_image);
                if (mFileInfo.type.contains("Picture")) {
                    mImageView.setVisibility(View.VISIBLE);
                    mImageLoader.DisplayImage(path, mImageView, null);
                }

                mFileName.setText(filename);
                editText.setText(filename);

                TextView textView = (TextView) findViewById(R.id.file_dialog_date);
                textView.setText(DateBuilder.getFullDate(mContext, mFileInfo.modified));
                TextView textView1 = (TextView) findViewById(R.id.file_dialog_path);
                textView1.setText(new File(path).getParent());
                TextView textView2 = (TextView) findViewById(R.id.file_dialog_size);
                textView2.setText(FileUtils.humanReadableByteCount(mFileInfo.size, false));
                TextView textView3 = (TextView) findViewById(R.id.file_dialog_tags);
                textView3.setText(FileUtils.getPrettyTag(mFileInfo.type));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Editable editable = editText.getText();
        if (editable != null) {
            if (!filename.equals(editable.toString())) {
                if (editable.toString().length() > 0) {
                    File file = new File(path);
                    if (file.exists()) {
                        File file1 = new File(file.getParent()+"/"+editable.toString());
                        if (file.renameTo(file1)) {
                            new DBUtils(mContext).updateFileName(path,
                                    file1.getAbsolutePath(), filename, file1.getName());
                        }
                    }
                }
            }
            new DBUtils(mContext).updateFileTags(mBools, path);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_add_tag_info:
                startActivity(new Intent(mContext, AddTagActivity.class));
                return true;
            case R.id.action_open_info:
                FileStartActivity.go(mContext, mrToast, mFileInfo.path);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateList() {
        mTags = new DBUtils(mContext).getCustomTags();
        if (mTags.size() > 0) {
            mBools = new DBUtils(mContext).getIsTagged(path);
            mAdapter = new InfoTagListAdapter(mContext, mTags, mBools);
            mListView.setAdapter(mAdapter);
            mNoListText.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            int list_h = (int) (mTags.size() * (42 * getResources().getDisplayMetrics().density));
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            if (params != null) {
                params.height = list_h;
                mListView.requestLayout();

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mBools[position] = !mBools[position];
                        mAdapter.notifyDataSetChanged();
                    }
                });

                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
}
