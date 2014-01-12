package com.lithidsw.findex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lithidsw.findex.R;

import java.util.ArrayList;
import java.util.List;

public class InfoTagListAdapter extends BaseAdapter {

    private View vi;
    private static LayoutInflater inflater = null;
    private Context mContext;
    private List<String[]> mCustomTags = new ArrayList<String[]>();
    private boolean[] mBools;

    public InfoTagListAdapter(Context context, ArrayList<String[]> list, boolean[] bools) {
        mContext = context;
        mCustomTags = list;
        mBools = bools;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        try {
            return mCustomTags.size();
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
        vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.file_info_tag_list_item, null);
        }

        if (vi != null) {
            String name = mCustomTags.get(position)[0];
            String value = mCustomTags.get(position)[2];
            TextView textView = (TextView) vi.findViewById(R.id.tag_name);
            TextView textView1 = (TextView) vi.findViewById(R.id.tag_value);
            CheckBox checkBox = (CheckBox) vi.findViewById(R.id.tag_checkbox);
            textView.setText(name);
            textView1.setText(value);
            if (mBools[position]) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        }

        return vi;
    }
}
