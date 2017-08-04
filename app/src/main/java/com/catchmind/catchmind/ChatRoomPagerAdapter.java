package com.catchmind.catchmind;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by sonsch94 on 2017-07-19.
 */

public class ChatRoomPagerAdapter extends FragmentPagerAdapter {

    public Fragment mf;
    public Fragment df;

    public ChatRoomPagerAdapter(FragmentManager fm,Fragment mf, Fragment df) {
        super(fm);
        this.mf = mf;
        this.df = df;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mf;
            case 1:
                return df;
            default:
                return mf;

        }
    }
}