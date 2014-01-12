package com.lithidsw.findex.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.info.FileInfo;
import com.lithidsw.findex.loader.ImageLoader;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.FileUtils;
import com.lithidsw.findex.utils.MrToast;

import java.util.ArrayList;
import java.util.List;

public class FilePageAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context mContext;
    private List<FileInfo> mFiles = new ArrayList<FileInfo>();
    ImageLoader imageLoader;
    boolean isTrash;
    private MrToast mrToast;
    private int mLastPosition;
    private int mThemeStyle;
    private SharedPreferences mPrefs;

    public FilePageAdapter(Context context, ArrayList<FileInfo> list, boolean is) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
        mFiles = list;
        isTrash = is;
        mrToast = new MrToast(mContext);
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context, 50, resItem(R.attr.file_default));
        mLastPosition = -1;
        if (mPrefs.getBoolean(C.PREF_TOGGLE_DARK, false)) {
            mThemeStyle = R.style.AppThemeDark;
        } else {
            mThemeStyle = R.style.AppTheme;
        }
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
            vi = inflater.inflate(R.layout.main_frag_item, null);
        }

        if (vi != null) {
            final int pos = position;
            final String filename = mFiles.get(position).name;
            final String tag = mFiles.get(position).type;
            final String path = mFiles.get(position).path;
            final long modified = mFiles.get(position).modified;
            final long size = mFiles.get(position).size;

            TextView textView = (TextView) vi.findViewById(R.id.file_name);
            textView.setText(filename);

            TextView textView2 = (TextView) vi.findViewById(R.id.file_size);
            textView2.setText(FileUtils.humanReadableByteCount(size, false));

            ImageView imageView = (ImageView) vi.findViewById(R.id.file_icon);
            ImageView imageView1 = (ImageView) vi.findViewById(R.id.file_image);

            if (path.endsWith(".apk")) {
                imageLoader.DisplayImage(path, imageView, null);
                imageLoader.DisplayImage(path, imageView1, null);
            } else {
                if (tag.contains(C.TAG_PICTURES)) {
                    imageLoader.DisplayImage(path, imageView1, null);
                    imageView.setImageResource(resItem(R.attr.file_picture));
                } else if (tag.contains(C.TAG_VIDEO)) {
                    imageLoader.DisplayImage(path, imageView1, null);
                    imageView.setImageResource(resItem(R.attr.file_video));
                } else if (tag.contains(C.TAG_SOUNDS)) {
                    imageView.setImageResource(resItem(R.attr.file_sound));
                    imageView1.setImageResource(resItem(R.attr.file_default));
                } else {
                    imageView.setImageResource(resItem(R.attr.file_default));
                    imageView1.setImageResource(resItem(R.attr.file_default));
                }
            }

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

    private int resItem(int item) {
        Resources.Theme theme =  mContext.getTheme();
        if (theme != null) {
            TypedArray a = mContext.getTheme().obtainStyledAttributes(mThemeStyle, new int[] {item});
            if (a != null) {
                int attributeResourceId = a.getResourceId(0, 0);
                a.recycle();
                return attributeResourceId;
            }
        }

        return 0;
    }
}
