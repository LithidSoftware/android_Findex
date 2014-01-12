package com.lithidsw.findex.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.widget.TextView;

import com.lithidsw.findex.db.DBUtils;

public class ItemCountLoader extends AsyncTask<String, String, Integer> {

    Context mContext;
    TextView mTextView;
    DBUtils dbUtils;
    int mPosition;

    public ItemCountLoader(Context context, TextView textView, int pos) {
        mContext = context;
        mTextView = textView;
        dbUtils = new DBUtils(mContext);
        mPosition = pos;
    }

    @Override
    protected Integer doInBackground(String... params) {
        if (params[0].equalsIgnoreCase("downloads")) {
            return dbUtils.getDownloadCount();
        } else if (params[0].equalsIgnoreCase("all")) {
            return dbUtils.getFileCount();
        } else if (params[0].equalsIgnoreCase("trash bin")) {
            return dbUtils.getTrashCount();
        } else {
            if (mPosition < C.NUM_MAIN_TITLES) {
                return dbUtils.getMainTagItemCount(params[0]);
            } else {
                return dbUtils.getCustomTagItemCount(params[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        mTextView.setTypeface(
                Typeface.createFromAsset(mContext.getAssets(), "Roboto-Light.ttf")
        );
        mTextView.setText(String.valueOf(integer));
    }
}
