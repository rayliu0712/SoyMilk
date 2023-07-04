package com.kitkat0712.soymilk;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MaskFragment extends Fragment {
    public static Uri maskURI = null;

    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mask, container, false);

        ((ImageView)view.findViewById(R.id.mask)).setImageURI(maskURI);

        return view;
    }
}