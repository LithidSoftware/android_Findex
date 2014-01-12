package com.lithidsw.findex;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.loader.ImageLoader;

public class IntroFragment extends Fragment {
    Activity mActivity;
    View mLayout;
    ImageLoader imageLoader;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = super.getActivity();
        imageLoader = new ImageLoader(mActivity, 350, R.drawable.loader);
        mLayout = inflater.inflate(R.layout.intro_frag, container, false);
        Bundle bundle = getArguments();
        if (mLayout != null && bundle != null) {
            int image = bundle.getInt("extra_image", 0);
            ImageView imageView = (ImageView) mLayout.findViewById(R.id.intro_image);
            imageLoader.DisplayImage(String.valueOf(image), imageView, null);
        }
        return mLayout;
    }
}
