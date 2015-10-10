package org.stepic.droid.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import org.stepic.droid.R;
import org.stepic.droid.view.fragments.VideoStepFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StepFragmentAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private ResHolder mResourceHolder;

    public StepFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        mResourceHolder = new ResHolder(context);

    }

    @Override
    public Fragment getItem(int position) {

        VideoStepFragment fragment = new VideoStepFragment();
        Bundle args = new Bundle();
        args.putString("test", position + 1 + "");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 10;
    }

    public View getTabView(int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tab_custom, null);
        TabHolder tabHolder = new TabHolder(view);

        tabHolder.stepIcon.setImageDrawable(mResourceHolder.videoIcon);

        return view;
    }

    public static class ResHolder {


        public Drawable videoIcon;
        public int notViewed;
        public int stepicPrimary;


        public ResHolder(Context context) {
            videoIcon = ContextCompat.getDrawable(context, R.drawable.ic_video);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notViewed = context.getColor(R.color.stepic_not_viewed);
            } else {
                notViewed = context.getResources().getColor(R.color.stepic_not_viewed);
            }



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                stepicPrimary = context.getColor(R.color.stepic_brand_primary);
            } else {
                stepicPrimary = context.getResources().getColor(R.color.stepic_brand_primary);
            }
        }


    }

    public static class TabHolder {
        //now this class is useless for performance, but in future we will tune this.

        @Bind(R.id.icon_for_step)
        ImageView stepIcon;

        public TabHolder(View v) {
            ButterKnife.bind(this, v);
        }

    }
}
