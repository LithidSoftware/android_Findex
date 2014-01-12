package com.lithidsw.findex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.utils.C;

public class IntroActivity extends FragmentActivity {

    private SharedPreferences mPrefs;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    Button mButtonSkip;
    ProgressBar mProgress;
    ImageButton mImageButton;
    Animation myFadeInAnimation;
    TextView mTextMessage;

    private String[] mMessages;
    private TypedArray mImages;
    private int mStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        if (mPrefs.getBoolean(C.PREF_TOGGLE_DARK, false)) {
            setTheme(R.style.AppThemeDarkNoActionBar);
        } else {
            setTheme(R.style.AppThemeNoActionBar);
        }
        setContentView(R.layout.intro);

        Resources res = getResources();

        mMessages = res.getStringArray(R.array.intro_message);
        mImages = res.obtainTypedArray(R.array.intro_images);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mProgress.setMax(100);
        mStep = (100 / mMessages.length);
        mProgress.setProgress(mStep);

        mTextMessage = (TextView) findViewById(R.id.intro_message);
        mTextMessage.setText(mMessages[0]);

        mImageButton = (ImageButton) findViewById(R.id.btn_next);
        myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.tween);
        if (myFadeInAnimation != null) {
            mImageButton.startAnimation(myFadeInAnimation);
        }
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });
        mButtonSkip = (Button) findViewById(R.id.btn_skip);
        mButtonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeIntro();
            }
        });

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == (mMessages.length - 1)) {
                    mButtonSkip.setText("Finish");
                } else {
                    mButtonSkip.setText("Skip");
                }

                if (i == (mMessages.length - 1)) {
                    mProgress.setProgress(100);
                } else {
                    mProgress.setProgress((i+1) * mStep);
                }

                if (i > 0) {
                    mImageButton.setVisibility(View.GONE);
                    mImageButton.clearAnimation();
                } else {
                    mImageButton.setVisibility(View.VISIBLE);
                    mImageButton.startAnimation(myFadeInAnimation);
                }

                mTextMessage.setText(mMessages[i]);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mViewPager.setCurrentItem(0);
    }

    private void closeIntro() {
        if (!mPrefs.getBoolean(C.PREF_FIRST_RUN, false)) {
            mPrefs.edit().putBoolean(C.PREF_FIRST_RUN, true).commit();
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new IntroFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("extra_image", mImages.getResourceId(position, -1));
            bundle.putString("extra_message", mMessages[position]);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mMessages.length;
        }
    }
}
