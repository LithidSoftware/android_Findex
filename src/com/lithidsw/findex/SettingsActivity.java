package com.lithidsw.findex;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.lithidsw.findex.R;
import com.lithidsw.findex.ef.DirectoryListActivity;
import com.lithidsw.findex.utils.C;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    private static Context mContext;
    private static Activity sActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences mPrefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        String mTheme = mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME);
        setTheme(getResources().getIdentifier(mTheme, "style", C.THIS));
        super.onCreate(savedInstanceState);
        mContext = this;
        sActivity = this;
        setupActionBar();
        setupSimplePreferencesScreen();
    }

    @Override
    public boolean isValidFragment(String frag) {
        return true;
    }

    private static void refreshTheme(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SimplePreferenceFragment())
                .commit();
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        return !isXLargeTablet(context);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    private static SharedPreferences getShared() {
        return mContext.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
    }

    public static class SimplePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);
            setupExcludeFolders(this);

            PreferenceCategory fakeHeader = new PreferenceCategory(mContext);
            fakeHeader.setTitle(R.string.pref_header_theme);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_theme);
            setUpColorListPreference(this);
            setupDarkToggle(this);

            fakeHeader = new PreferenceCategory(mContext);
            fakeHeader.setTitle(R.string.pref_header_about);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_about);
            setupAbout(this);
        }
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setupExcludeFolders(this);
        }
    }

    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);
            setupAbout(this);
        }
    }

    public static class ThemePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_theme);
            setUpColorListPreference(this);
            setupDarkToggle(this);
        }
    }

    private static void setupExcludeFolders(final PreferenceFragment fragment) {
        Preference preference = fragment.findPreference(C.PREF_EXCLUDE_FOLDERS);
        if (preference != null) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    sActivity.startActivity(new Intent(sActivity, DirectoryListActivity.class));
                    return true;
                }
            });
        }
    }

    private static void setupDarkToggle(final PreferenceFragment fragment) {
        final SharedPreferences prefs = getShared();
        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) fragment.findPreference(C.PREF_TOGGLE_DARK);
        if (checkBoxPreference != null) {
            checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String curTheme = prefs.getString(C.PREF_THEME, C.DEFAULT_THEME);
                    String theme;
                    if (prefs.getBoolean(C.PREF_TOGGLE_DARK, false)) {
                        theme = "AppTheme" + curTheme.substring(12);
                    } else {
                        theme = "AppThemeDark" + curTheme.substring(8);
                    }
                    prefs.edit().putString(C.PREF_THEME, theme).commit();
                    refreshTheme(fragment.getActivity());
                    return true;
                }
            });
        }
    }

    private static void setUpColorListPreference(final PreferenceFragment fragment) {
        final SharedPreferences prefs = getShared();
        ListPreference listPreference = (ListPreference) fragment.findPreference("key_pref_color");
        if (listPreference != null) {
            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    int item = Integer.parseInt((String) newValue);

                    TypedArray ids = mContext.getResources()
                            .obtainTypedArray(R.array.theme_color_values);
                    String name = mContext.getResources().getStringArray(R.array.theme_color_names)[item];
                    if (ids != null) {
                        int icon = ids.getResourceId(item, -1);
                        preference.setIcon(mContext.getResources().getDrawable(icon));
                        ids.recycle();

                        prefs.edit().putInt(C.PREF_COLOR, icon).commit();
                    }

                    if (prefs.getBoolean(C.PREF_TOGGLE_DARK, false)) {
                        prefs.edit().putString(C.PREF_THEME, "AppThemeDark"+name).commit();
                    } else {
                        prefs.edit().putString(C.PREF_THEME, "AppTheme"+name).commit();
                    }

                    refreshTheme(fragment.getActivity());

                    return true;
                }
            });

            if (prefs != null) {
                int icon = prefs.getInt(C.PREF_COLOR, R.drawable.gray);
                listPreference.setIcon(mContext.getResources().getDrawable(icon));
            }
        }
    }

    private static void setupAbout(PreferenceFragment fragment) {
        Preference morep = fragment.findPreference("pref_more_by");
        if (morep != null) {
            morep.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/developer?id=LithidSW")));
                    return true;
                }
            });
        }
    }
}
