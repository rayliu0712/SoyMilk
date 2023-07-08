package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.VERSION_SETTINT;
import static com.kitkat0712.soymilk.MainActivity.backgroundFile;
import static com.kitkat0712.soymilk.MainActivity.backgroundIV;
import static com.kitkat0712.soymilk.MainActivity.backgroundURI;
import static com.kitkat0712.soymilk.MainActivity.sauceConfig;
import static com.kitkat0712.soymilk.MainActivity.sauceConfigStringList;
import static com.kitkat0712.soymilk.MainActivity.switchConfig;
import static com.kitkat0712.soymilk.MainActivity.dndOnClick;
import static com.kitkat0712.soymilk.MainActivity.getJPG;
import static com.kitkat0712.soymilk.MainActivity.isDNDOn;
import static com.kitkat0712.soymilk.MainActivity.isLateEnough;
import static com.kitkat0712.soymilk.MainActivity.ma;
import static com.kitkat0712.soymilk.MainActivity.replaceFragment;
import static com.kitkat0712.soymilk.MainActivity.viewOriginalColor;

import static java.lang.String.valueOf;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
	private String[] sauceItems;
	private InputMethodManager imm;
	private EditText numberET;
	private TextView dndTV;



	private void setDNDVisual() {
		Drawable[] drawables = dndTV.getCompoundDrawables();
		Drawable tintedDrawable = drawables[0].mutate();

		if (isDNDOn()) {
			dndTV.setTextColor(Color.RED);

			if (isLateEnough(VERSION_SETTINT)) {
				tintedDrawable.setTint(Color.RED);
				dndTV.setCompoundDrawables(tintedDrawable, drawables[1], drawables[2], drawables[3]);
			}
		} else {
			dndTV.setTextColor(viewOriginalColor);

			if (isLateEnough(VERSION_SETTINT)) {
				tintedDrawable.setTint(viewOriginalColor);
				dndTV.setCompoundDrawables(tintedDrawable, drawables[1], drawables[2], drawables[3]);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		imm = (InputMethodManager) ma.getSystemService(Context.INPUT_METHOD_SERVICE);

		// set layout & flag
		try {
			Window window = ma.getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

			int uiOptions = 0;

			// hide status bar
			if (switchConfig.hideStatusBar) {
				uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
				window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}

			// hide navigation
			if (switchConfig.hideNavigationBar) {
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			}

			window.getDecorView().setSystemUiVisibility(uiOptions);

			// secure flag
			if (switchConfig.enableFlagSecure) {
				window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
			}
		} catch (NullPointerException e) {
			MainActivity.newSwitchConfigFile();
		}

		{
			sauceItems = new String[sauceConfig.size()];
			for (int i = 0; i < sauceConfig.size(); ++i) {
				sauceItems[i] = sauceConfig.get(i).name;
			}
			Toast.makeText(ma, valueOf(sauceItems.length), Toast.LENGTH_SHORT).show();
		}

		View view = inflater.inflate(R.layout.fragment_home, container, false);

		backgroundIV = view.findViewById(R.id.background);

		if (backgroundURI != null) {
			backgroundIV.setImageURI(backgroundURI);
			getJPG(backgroundIV.getDrawable(), backgroundFile);
		}

		numberET = view.findViewById(R.id.number);
		if (!switchConfig.useNumPad)
			numberET.setInputType(InputType.TYPE_CLASS_TEXT);
		numberET.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				numberET.setHint("");
			} else {
				if (numberET.getText().toString().equals("")) {
					numberET.setHint("SoyMilk");
				}
			}
		});

		view.findViewById(R.id.view).setOnClickListener(v -> {
			imm.hideSoftInputFromWindow(numberET.getWindowToken(), 0);
			numberET.clearFocus();
		});

		dndTV = view.findViewById(R.id.dnd);
		viewOriginalColor = dndTV.getCurrentTextColor();
		dndTV.setOnClickListener(v -> {
			if (dndOnClick()) {
				setDNDVisual();
			}
		});

		ListView lv = view.findViewById(R.id.listview);
		ArrayAdapter<String> ap = new ArrayAdapter<String>(ma, R.layout.list_sauce_item, sauceItems) {
			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
				View listItemView = convertView;
				if (listItemView == null) {
					listItemView = LayoutInflater.from(ma).inflate(R.layout.list_sauce_item, parent, false);
				}

				String currentItem = sauceItems[position];
				Button button = listItemView.findViewById(R.id.button);

				button.setText(currentItem);

				return listItemView;
			}
		};
		lv.setAdapter(ap);

		view.findViewById(R.id.setting).setOnClickListener(v -> replaceFragment(new SettingFragment()));
		view.findViewById(R.id.surfweb).setOnClickListener(v -> replaceFragment(new WebFragment()));

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setDNDVisual();
	}
}