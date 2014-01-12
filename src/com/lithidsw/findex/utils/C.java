package com.lithidsw.findex.utils;

import android.os.Build;

public class C {
    public static final String PREF = "com.lithidsw.findex_preferences";
    public static final String THIS = "com.lithidsw.findex";
    public static final String DEFAULT_THEME = "AppThemeStock";
    public static final String ID = "6E3A5B3D7F5A94C5A6FF00BD6717027A";
    public static final String PREF_TOGGLE_DARK = "pref_toggle_dark";
    public static final String PREF_FIRST_RUN = "pref_first_run";
    public static final String PREF_TOGGLE_GRID = "pref_toggle_grid";
    public static final String PREF_EXCLUDE_FOLDERS = "pref_exclude_folders";
    public static final String PREF_THEME = "pref_theme";
    public static final String PREF_COLOR = "pref_color";

    public static final String ACTION_INDEX_COMPLETE = "com.lithidsw.findex.ACTION_INDEX_COMPLETE";
    public static final String ACTION_REFRESH = "com.lithidsw.findex.ACTION_REFRESH";

    public static final String TAG_DOCUMENTS = "Documents";
    public static final String TAG_DOWNLOADS = "Downloads";
    public static final String TAG_PICTURES = "Pictures";
    public static final String TAG_SOUNDS = "Sounds";
    public static final String TAG_VIDEO = "Videos";

    public static final int NUM_MAIN_TITLES = 6;
    public static final long MIL_24_HOURS = 86400000;
    public static final long MIL_30_DAYS = MIL_24_HOURS * 30;

    public static final int CURRENT_SDK = Build.VERSION.SDK_INT;
    public static final int SDK_19 = Build.VERSION_CODES.KITKAT;
}
