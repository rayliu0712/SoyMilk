package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.ma;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
	private String[] sauceItems;
	private InputMethodManager imm;
	private EditText numberET;
	private ImageView dndIV;
	private ImageView bookmarkIV;
	private EditText urlET;
	private boolean isOnCreateView = true;

	private void setDNDVisual() {
		if (ma.isDNDOn()) {
			dndIV.setColorFilter(Color.RED);
		} else {
			dndIV.clearColorFilter();
		}
	}

	private void setEditBookmarkVisual(boolean b) {
		if (b) {
			bookmarkIV.setColorFilter(Color.YELLOW);
		} else {
			bookmarkIV.clearColorFilter();
		}
	}

	private void setUrl() {
		urlET.setText(String.format("%s%s", ma.sauceConfig.get(ma.choosenSauce).url, numberET.getText().toString()));
		ma.url = urlET.getText().toString();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		imm = (InputMethodManager) ma.getSystemService(Context.INPUT_METHOD_SERVICE);
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		ma.backgroundIV = view.findViewById(R.id.background);
		numberET = view.findViewById(R.id.number);
		numberET.setText(ma.number);
		dndIV = view.findViewById(R.id.dnd);
		bookmarkIV = view.findViewById(R.id.bookmark);
		urlET = view.findViewById(R.id.url);
		urlET.setText(ma.url);
		numberET.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				numberET.setHint("");
			} else {
				if (numberET.getText().toString().equals("")) {
					numberET.setHint("SoyMilk");
				}
			}
		});
		numberET.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (isOnCreateView) return;

				setUrl();
				for (BookmarkConfig bc : ma.bookmarkConfig) {
					if (bc.url.equals(urlET.getText().toString())) {
						setEditBookmarkVisual(true);
						break;
					} else {
						setEditBookmarkVisual(false);
					}
				}
			}
		});
		dndIV.setOnClickListener(v -> {
			if (ma.dndOnClick()) {
				setDNDVisual();
			}
		});

		// setter
		{
			ma.staticLayout();

			sauceItems = new String[ma.sauceConfig.size()];
			for (int i = 0; i < ma.sauceConfig.size(); ++i) {
				sauceItems[i] = ma.sauceConfig.get(i).name;
			}

			if (!ma.switchConfig.enableRecord) {
				ma.historyFile.delete();
				ma.historyConfig.clear();
			}

			if (ma.backgroundURI != null) {
				ma.backgroundIV.setImageURI(ma.backgroundURI);
				ma.getJPG(ma.backgroundIV.getDrawable(), ma.backgroundFile);
			}

			if (!ma.switchConfig.useNumPad) {
				numberET.setInputType(InputType.TYPE_CLASS_TEXT);
			}
		}

		bookmarkIV.setOnClickListener(v -> {
			setEditBookmarkVisual(true);
			ma.shouldIgnoreFocusChanged = true;
			View dialogView = View.inflate(ma, R.layout.dialog_bookmark, null);
			new AlertDialog.Builder(ma)
					.setView(dialogView)
					.setPositiveButton("OK", (dialogInterface, i) -> {
						BookmarkConfig bookmarkConfig = new BookmarkConfig();
						bookmarkConfig.name = ((EditText) dialogView.findViewById(R.id.name)).getText().toString();
						bookmarkConfig.number = numberET.getText().toString();
						bookmarkConfig.url = urlET.getText().toString();
						ma.bookmarkConfig.add(0, bookmarkConfig);
						ma.writeListConfig(ma.bookmarkFile, ma.bookmarkConfig);
					})
					.setNegativeButton("Cancel", null)
					.show();
		});
		view.findViewById(R.id.library).setOnClickListener(v -> ma.replaceFragment(new LibraryFragment()));
		view.findViewById(R.id.history).setOnClickListener(v -> ma.replaceFragment(new HistoryFragment()));
		view.findViewById(R.id.view).setOnClickListener(v -> {
			imm.hideSoftInputFromWindow(numberET.getWindowToken(), 0);
			numberET.clearFocus();
		});
		((ListView) view.findViewById(R.id.listview)).setAdapter(new ArrayAdapter<String>(ma, R.layout.listitem_sauce, sauceItems) {
			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
				View listItemView = convertView;
				String currentItem = sauceItems[position];
				if (listItemView == null) {
					listItemView = LayoutInflater.from(ma).inflate(R.layout.listitem_sauce, parent, false);
				}

				Button button = listItemView.findViewById(R.id.button);
				button.setOnClickListener(v -> {
					ma.choosenSauce = position;
					setUrl();
				});

				button.setText(currentItem);

				return listItemView;
			}
		});
		view.findViewById(R.id.setting).setOnClickListener(v -> ma.replaceFragment(new SettingFragment()));
		view.findViewById(R.id.surfweb).setOnClickListener(v -> ma.replaceFragment(new WebFragment()));

		isOnCreateView = false;
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setDNDVisual();
	}
}