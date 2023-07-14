package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.ma;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_history, container, false);
		ListView lv = (ListView) view.findViewById(R.id.listview);
		lv.setAdapter(new CustomAdapter(ma.historyConfig));

		view.findViewById(R.id.back).setOnClickListener(v -> ma.replaceFragment(new HomeFragment()));
		view.findViewById(R.id.clear).setOnClickListener(v -> {
			ma.historyFile.delete();
			ma.historyConfig.clear();
			lv.setAdapter(new CustomAdapter(ma.historyConfig));
		});

		return view;
	}
}