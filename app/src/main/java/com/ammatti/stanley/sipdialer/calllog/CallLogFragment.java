package com.ammatti.stanley.sipdialer.calllog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ammatti.stanley.sipdialer.R;

/**
 * Created by 1310042 on 2016/1/20.
 */
public class CallLogFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calllog_fragment_layout, null);
        return view;
    }
}
