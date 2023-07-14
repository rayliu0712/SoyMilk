package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.ma;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
	private final ArrayList<String> data = new ArrayList<>();
	private final ArrayList<Boolean> isUrl = new ArrayList<>();

	public CustomAdapter(ArrayList<HistoryConfig> al) {
		for (HistoryConfig hc : al) {
			this.data.add(hc.date);
			this.isUrl.add(false);
		}
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public String getItem(int i) {
		return data.get(i);
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int i, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(ma).inflate(R.layout.listitem_history, parent, false);
		}

		TextView tv = convertView.findViewById(R.id.textview);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, isUrl.get(i) ? 20 : 25);
		tv.setText(getItem(i));

		convertView.setOnClickListener(v -> {
			if (isUrl.get(i)) {
				ma.url = getItem(i);
				ma.replaceFragment(new WebFragment());
			} else {
				try {
					if (isUrl.get(i + 1)) {
						// collapse
						int x = 0;
						for (int j = 0; j < ma.historyConfig.size(); ++j) {
							if (tv.getText().toString().equals(ma.historyConfig.get(j).date)) {
								x = j;
								break;
							}
						}
						for (int j = 0; j < ma.historyConfig.get(x).url.size(); ++j) {
							data.remove(i + 1);
							isUrl.remove(i + 1);
						}
					} else {
						throw new IndexOutOfBoundsException();
					}
				} catch (IndexOutOfBoundsException e) {
					// expand
					int x = 0;
					for (int j = 0; j < ma.historyConfig.size(); ++j) {
						if (tv.getText().toString().equals(ma.historyConfig.get(j).date)) {
							x = j;
							break;
						}
					}
					for (int j = ma.historyConfig.get(x).url.size() - 1; j >= 0; --j) {
						data.add(i + 1, ma.historyConfig.get(x).url.get(j));
						isUrl.add(i + 1, true);
					}
				}

				notifyDataSetChanged();
			}
		});

		return convertView;
	}
}