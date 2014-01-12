package com.lithidsw.findex.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.FileUtils;

public class StorageListAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context mContext;
    private String[] mPaths;
    private String[] mLabels;
    private String mThemeStyle;
    private SharedPreferences mPrefs;

    public StorageListAdapter(Context context, String[] paths, String[] labels) {
        mContext = context;
        mPaths = paths;
        mPrefs = mContext.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
        mLabels = labels;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mThemeStyle = mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME);
    }

    @Override
    public int getCount() {
        try {
            return mPaths.length;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.activity_main_sdcard_list_items, null);
        }

        if (vi != null) {
            String str_path = mPaths[position];

            Log.e("", "Path: " + str_path);

            long[] prog = FileUtils.getStorageData(str_path);
            int total = (int) (prog[0] / 10000);
            int avail = (int) (prog[1] / 10000);

            ProgressBar progressBar = (ProgressBar) vi.findViewById(R.id.progress);
            progressBar.setMax(total);
            progressBar.setProgress(avail);

            LinearLayout header = (LinearLayout) vi.findViewById(R.id.storage_info);
            if (mLabels != null) {
                String str_label = mLabels[position];
                TextView textViewTitle = (TextView) vi.findViewById(R.id.text_title);
                setTypeFace(textViewTitle);
                textViewTitle.setText(str_label);

                TextView textViewAvail = (TextView) vi.findViewById(R.id.text_avail);
                setTypeFace(textViewAvail);
                textViewAvail.setText(String.valueOf(FileUtils.humanReadableByteCount(prog[1], false)));

                TextView textViewTotal = (TextView) vi.findViewById(R.id.text_total);
                setTypeFace(textViewTotal);
                textViewTotal.setText(String.valueOf(FileUtils.humanReadableByteCount(prog[0], false)));

                header.setVisibility(View.VISIBLE);
            } else {
                header.setVisibility(View.GONE);
            }
        }

        return vi;
    }

    private void setTypeFace(TextView textView) {
        textView.setTypeface(
                Typeface.createFromAsset(mContext.getAssets(), "Roboto-Light.ttf")
        );
    }
}
