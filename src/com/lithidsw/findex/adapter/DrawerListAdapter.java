package com.lithidsw.findex.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.ItemCountLoader;

import java.util.ArrayList;
import java.util.List;

public class DrawerListAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context mContext;
    private List<String[]> mCustomTags = new ArrayList<String[]>();

    public DrawerListAdapter(Context context, ArrayList<String[]> list) {
        mContext = context;
        mCustomTags = list;
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
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.activity_main_drawer_item, null);
        }

        if (vi != null) {
            String name = mCustomTags.get(position)[0];
            TextView textViewTitle = (TextView) vi.findViewById(R.id.text_title);
            LinearLayout header = (LinearLayout) vi.findViewById(R.id.header_title);
            if (position == C.NUM_MAIN_TITLES + 1) {
                header.setVisibility(View.VISIBLE);
            } else {
                header.setVisibility(View.GONE);
            }

            textViewTitle.setTypeface(
                    Typeface.createFromAsset(mContext.getAssets(), "Roboto-Light.ttf")
            );
            textViewTitle.setText(name);

            new ItemCountLoader(
                    mContext,
                    (TextView) vi.findViewById(R.id.text_title_count),
                    position).execute(name);
        }

        return vi;
    }
}
