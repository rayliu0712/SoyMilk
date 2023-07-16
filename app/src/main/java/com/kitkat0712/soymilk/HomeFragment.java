package com.kitkat0712.soymilk;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static com.kitkat0712.soymilk.MainActivity.ma;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
	private InputMethodManager imm;
	private EditText numberET;
	private ImageView dndIV;
	private ImageView bookmarkIV;
	private EditText urlET;
	private boolean isOnCreateView = true;
	private int choosenBookmark = -1;

	private void setDNDVisual() {
		if (ma.isDNDOn()) {
			dndIV.setColorFilter(Color.RED);
		} else {
			dndIV.clearColorFilter();
		}
	}

	private void setBookmarkVisual(int index) {
		choosenBookmark = index;
		if (index == -1) {
			bookmarkIV.clearColorFilter();
		} else {
			bookmarkIV.setColorFilter(Color.YELLOW);
		}
	}

	private void checkBookmark() {
		for (int i = 0; i < ma.bookmarkConfig.size(); ++i) {
			if (ma.url.equals(ma.bookmarkConfig.get(i).url)) {
				setBookmarkVisual(i);
				break;
			} else {
				setBookmarkVisual(-1);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		imm = (InputMethodManager) ma.getSystemService(Context.INPUT_METHOD_SERVICE);
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		ma.backgroundIV = view.findViewById(R.id.background);
		dndIV = view.findViewById(R.id.dnd);
		if (SDK_INT >= M) {
			dndIV.setOnClickListener(v -> {
				if (ma.dndOnClick()) {
					setDNDVisual();
				}
			});
		} else {
			dndIV.setEnabled(false);
		}
		bookmarkIV = view.findViewById(R.id.bookmark);
		numberET = view.findViewById(R.id.number);
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

				ma.number = editable.toString();
				urlET.setText(String.format("%s%s", ma.sauceConfig.get(ma.choosenSauce).url, numberET.getText().toString()));
				checkBookmark();
			}
		});
		numberET.setText(ma.number);
		numberET.setOnLongClickListener(v -> {
			numberET.setText("");
			return true;
		});
		numberET.setOnFocusChangeListener((v, b) -> numberET.setHint(b ? "" : "SoyMilk"));
		urlET = view.findViewById(R.id.url);
		urlET.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (isOnCreateView) return;

				ma.url = editable.toString();
				checkBookmark();
			}
		});
		urlET.setText(ma.url);
		urlET.setOnLongClickListener(v -> {
			String paste = ma.pasteFromClipboard();
			if (paste != null) {
				urlET.setText(paste);
			}
			return true;
		});
		urlET.setOnFocusChangeListener((v, b) -> urlET.setHint(b ? "" : "URL"));

		// setter
		{
			ma.staticLayout();

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

			checkBookmark();
		}

		bookmarkIV.setOnClickListener(v -> {
			ma.shouldIgnoreFocusChanged = true;
			View dialogView = View.inflate(ma, R.layout.dialog_bookmark, null);
			((EditText) dialogView.findViewById(R.id.number)).setText(numberET.getText().toString());
			((EditText) dialogView.findViewById(R.id.url)).setText(ma.url.toString());

			AlertDialog.Builder builder = new AlertDialog.Builder(ma)
					.setView(dialogView)
					.setCancelable(false)
					.setTitle("Edit Bookmark")
					.setPositiveButton("OK", (dialogInterface, i) -> {
						setBookmarkVisual(choosenBookmark == -1 ? 0 : choosenBookmark);
						ma.bookmarkConfig.add(0,
								new BookmarkConfig(
										((EditText) dialogView.findViewById(R.id.name)).getText().toString(),
										numberET.getText().toString(),
										ma.url)
						);
						ma.writeListConfig(ma.bookmarkFile, ma.bookmarkConfig);
					})
					.setNegativeButton("Cancel", null);

			if (choosenBookmark != -1) {
				((TextView) dialogView.findViewById(R.id.name)).setText(ma.bookmarkConfig.get(choosenBookmark).name);
				builder.setView(dialogView);
				builder.setNeutralButton("Remove", (dialogInterface, i) -> {
					ma.bookmarkConfig.remove(choosenBookmark);
					setBookmarkVisual(-1);
					ma.writeListConfig(ma.bookmarkFile, ma.bookmarkConfig);
				});
			}
			builder.show();
		});
		view.findViewById(R.id.copy).setOnClickListener(v -> {
			ma.paste2Clipboard(ma.url);
			Toast.makeText(ma, "Pasted", Toast.LENGTH_SHORT).show();
		});
		view.findViewById(R.id.library).setOnClickListener(v -> ma.replaceFragment(new LibraryFragment()));
		view.findViewById(R.id.history).setOnClickListener(v -> ma.replaceFragment(new HistoryFragment()));
		view.findViewById(R.id.view).setOnClickListener(v -> {
			imm.hideSoftInputFromWindow(numberET.getWindowToken(), 0);
			numberET.clearFocus();
			urlET.clearFocus();
		});

		ListView lv = view.findViewById(R.id.listview);
		lv.setAdapter(new ArrayAdapter<SauceConfig>(ma, R.layout.listitem_sauce, ma.sauceConfig) {
			@Nullable
			@Override
			public SauceConfig getItem(int position) {
				return ma.sauceConfig.get(position);
			}

			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
				SauceConfig currentItem = getItem(position);
				if (convertView == null) {
					convertView = LayoutInflater.from(ma).inflate(R.layout.listitem_sauce, parent, false);
				}

				Button button = convertView.findViewById(R.id.button);
				button.setText(ma.choosenSauce == position ? String.format("[ %s ]", currentItem.name) : currentItem.name);
				button.setOnClickListener(v -> {
					button.setText(String.format("[ %s ]", currentItem.name));
					if (ma.choosenSauce != position) {
						Button previousBtn = lv.getChildAt(ma.choosenSauce).findViewById(R.id.button);
						previousBtn.setText(previousBtn.getText().toString().replace("[ ", "").replace(" ]", ""));
					}
					ma.choosenSauce = position;
					urlET.setText(String.format("%s%s", ma.sauceConfig.get(ma.choosenSauce).url, numberET.getText().toString()));
					checkBookmark();
				});
				button.setOnLongClickListener(v -> {
					ma.shouldIgnoreFocusChanged = true;
					View dialogView = View.inflate(ma, R.layout.dialog_sauce, null);
					EditText nameET = dialogView.findViewById(R.id.name);
					EditText sauceUrlET = dialogView.findViewById(R.id.url);
					nameET.setText(ma.sauceConfig.get(position).name);
					sauceUrlET.setText(ma.sauceConfig.get(position).url);

					new AlertDialog.Builder(ma)
							.setCancelable(false)
							.setView(dialogView)
							.setTitle("Edit Sauce")
							.setPositiveButton("OK", (dialogInterface, i) -> {
								currentItem.name = nameET.getText().toString();
								currentItem.url = sauceUrlET.getText().toString();
								ma.writeListConfig(ma.sauceFile, ma.sauceConfig);
								notifyDataSetChanged();
							})
							.setNeutralButton("Remove", (dialogInterface, i) -> {
								ma.sauceConfig.remove(position);
								notifyDataSetChanged();
							})
							.setNegativeButton("Cancel", null)
							.show();
					return true;
				});

				return convertView;
			}
		});
		view.findViewById(R.id.setting).setOnClickListener(v -> ma.replaceFragment(new SettingFragment()));
		view.findViewById(R.id.go_default).setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + ma.url));
			startActivity(intent);
		});
		view.findViewById(R.id.go_webview).setOnClickListener(v -> {
			ma.replaceFragment(new WebFragment());
		});

		isOnCreateView = false;
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setDNDVisual();
	}
}