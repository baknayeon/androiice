package com.example.dahae.myandroiice.Information.ComplexPlan;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dahae.myandroiice.R;

/**
 * Created by b_newyork on 2016-02-10.
 */
public class complex8 extends Fragment {

    int fragNum;

    @Override
    public  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragNum = getArguments() != null ? getArguments().getInt("val") : 1;
    }

    public complex8 init(int val) {
        complex8 fragment = new complex8();
        Bundle args = new Bundle();
        args.putInt("val", val);
        fragment.setArguments(args);
        return fragment;
    }
    /**
     * The Fragment's UI is a list.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.complex8, null);
        return layoutView;
    }

}