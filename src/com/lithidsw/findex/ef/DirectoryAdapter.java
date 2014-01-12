package com.lithidsw.findex.ef;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.info.DirPickerInfo;

import java.util.ArrayList;
import java.util.List;

public class DirectoryAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context mContext;
    private List<DirPickerInfo> mFiles = new ArrayList<DirPickerInfo>();
    private int mLastPosition;


    public DirectoryAdapter(Context context, ArrayList<DirPickerInfo> list) {
        mContext = context;
        mFiles = list;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLastPosition = -1;
    }

    @Override
    public int getCount() {
        try {
            return mFiles.size();
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
            vi = inflater.inflate(R.layout.directory_list_item, null);
        }

        if (vi != null) {
            final String filename = mFiles.get(position).name;

            TextView textView = (TextView) vi.findViewById(R.id.dir_name);
            textView.setText(filename);

            TranslateAnimation animation;
            if (position > mLastPosition) {

                animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f
                );

                animation.setDuration(500);
                vi.startAnimation(animation);
                mLastPosition = position;
            }

            if (position == (mFiles.size()-1)) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                }, 500);
            }
        }
        return vi;
    }

}
