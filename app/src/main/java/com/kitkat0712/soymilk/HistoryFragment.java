package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.ma;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_history, container, false);

		ArrayAdapter<HistoryConfig> ap = new ArrayAdapter<HistoryConfig>(ma, R.layout.listitem_history, ma.historyConfig) {
			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
				HistoryConfig currentItem = ma.historyConfig.get(position);

				View listItemView = convertView;
				if (listItemView == null) {
					listItemView = LayoutInflater.from(ma).inflate(R.layout.listitem_history, parent, false);
				}
				listItemView.setOnClickListener(v -> {
					ma.url = currentItem.url;
					ma.replaceFragment(new WebFragment());
				});


				((TextView) listItemView.findViewById(R.id.date)).setText(currentItem.date);
				((TextView) listItemView.findViewById(R.id.url)).setText(currentItem.url);


				return listItemView;
			}
		};
		((ListView) view.findViewById(R.id.listview)).setAdapter(ap);

		view.findViewById(R.id.back).setOnClickListener(v -> ma.replaceFragment(new HomeFragment()));
		view.findViewById(R.id.clear).setOnClickListener(v -> {
			ma.historyConfig.clear();
			ma.historyFile.delete();
			ap.notifyDataSetChanged();
		});

		return view;
	}
}