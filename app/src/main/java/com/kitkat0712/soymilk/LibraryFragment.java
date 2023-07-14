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

public class LibraryFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_library, container, false);

		view.findViewById(R.id.back).setOnClickListener(v -> {
			ma.replaceFragment(new HomeFragment());
		});

		((ListView) view.findViewById(R.id.listview)).setAdapter(new ArrayAdapter<BookmarkConfig>(ma, R.layout.listitem_library, ma.bookmarkConfig) {
			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
				View listItemView = convertView;
				BookmarkConfig currentItem = ma.bookmarkConfig.get(position);
				if (listItemView == null) {
					listItemView = LayoutInflater.from(ma).inflate(R.layout.listitem_library, parent, false);
				}

				listItemView.setOnClickListener(v -> {
					ma.url = currentItem.url;
					ma.number = currentItem.number;
					ma.replaceFragment(new WebFragment());
				});
				((TextView) listItemView.findViewById(R.id.name)).setText(currentItem.name);
				((TextView) listItemView.findViewById(R.id.number)).setText(currentItem.number);

				return listItemView;
			}
		});

		return view;
	}
}