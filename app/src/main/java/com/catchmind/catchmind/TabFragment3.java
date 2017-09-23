package com.catchmind.catchmind;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class TabFragment3 extends Fragment {

    Button NoticeBtn;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.tab_fragment_3, container, false);

        mPref = getActivity().getSharedPreferences("login",getActivity().MODE_PRIVATE);
        editor = mPref.edit();

        NoticeBtn = (Button) rootView.findViewById(R.id.settingNotice);

        if(mPref.getBoolean("aa",false)){
            NoticeBtn.setText("전체 알림 ON");
        }else{
            NoticeBtn.setText("전체 알림 OFF");
        }

        NoticeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mPref.getBoolean("aa",false)){
                    editor.putBoolean("aa",false);
                    editor.commit();
                    NoticeBtn.setText("전체 알림 OFF");
                }else{
                    editor.putBoolean("aa",true);
                    editor.commit();
                    NoticeBtn.setText("전체 알림 ON");
                }
            }

        });

        return rootView;

    }

}
