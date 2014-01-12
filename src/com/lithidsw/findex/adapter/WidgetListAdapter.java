package com.lithidsw.findex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lithidsw.findex.R;

import java.util.ArrayList;
import java.util.List;

public class WidgetListAdapter extends BaseAdapter {

    private View vi;
    private static LayoutInflater inflater = null;
    private Context mContext;
    private List<String> mTags = new ArrayList<String>();

    public WidgetListAdapter(Context context, ArrayList<String> list) {
        mContext = context;
        mTags = list;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        try {
            return mTags.size();
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
            vi = inflater.inflate(R.layout.widget_config_list_item, null);
        }

        if (vi != null) {
            String name = mTags.get(position);
            TextView textView = (TextView) vi.findViewById(R.id.tag_name);
            textView.setText(name);
        }

        return vi;
    }
}
