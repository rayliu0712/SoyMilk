package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.VERSION_SETTINT;
import static com.kitkat0712.soymilk.MainActivity.backgroundFile;
import static com.kitkat0712.soymilk.MainActivity.backgroundIV;
import static com.kitkat0712.soymilk.MainActivity.backgroundURI;
import static com.kitkat0712.soymilk.MainActivity.loadConfig;
import static com.kitkat0712.soymilk.MainActivity.sauceConfig;
import static com.kitkat0712.soymilk.MainActivity.switchConfig;
import static com.kitkat0712.soymilk.MainActivity.dndOnClick;
import static com.kitkat0712.soymilk.MainActivity.getJPG;
import static com.kitkat0712.soymilk.MainActivity.isDNDOn;
import static com.kitkat0712.soymilk.MainActivity.isLateEnough;
import static com.kitkat0712.soymilk.MainActivity.ma;
import static com.kitkat0712.soymilk.MainActivity.replaceFragment;
import static com.kitkat0712.soymilk.MainActivity.switchFile;
import static com.kitkat0712.soymilk.MainActivity.switchList;
import static com.kitkat0712.soymilk.MainActivity.url;
import static com.kitkat0712.soymilk.MainActivity.viewOriginalColor;

import static java.lang.String.format;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    private String[] sauceItems;
    private InputMethodManager imm;

    private EditText numberET;
    private TextView dndTV;
    private EditText urlET;

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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        backgroundIV = view.findViewById(R.id.background);
        numberET = view.findViewById(R.id.number);
        dndTV = view.findViewById(R.id.dnd);
        urlET = view.findViewById(R.id.url);

        imm = (InputMethodManager) ma.getSystemService(Context.INPUT_METHOD_SERVICE);
        viewOriginalColor = dndTV.getCurrentTextColor();

        numberET.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                numberET.setHint("");
            } else {
                if (numberET.getText().toString().equals("")) {
                    numberET.setHint("SoyMilk");
                }
            }
        });
        dndTV.setOnClickListener(v -> {
            if (dndOnClick()) {
                setDNDVisual();
            }
        });

        // setter
        {
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
                loadConfig(switchFile, switchList, R.raw.switch_config);
            }

            sauceItems = new String[sauceConfig.size()];
            for (int i = 0; i < sauceConfig.size(); ++i) {
                sauceItems[i] = sauceConfig.get(i).name;
            }

            if (backgroundURI != null) {
                backgroundIV.setImageURI(backgroundURI);
                getJPG(backgroundIV.getDrawable(), backgroundFile);
            }

            if (!switchConfig.useNumPad) {
                numberET.setInputType(InputType.TYPE_CLASS_TEXT);
            }
        }

        view.findViewById(R.id.view).setOnClickListener(v -> {
            imm.hideSoftInputFromWindow(numberET.getWindowToken(), 0);
            numberET.clearFocus();
        });
        ((ListView) view.findViewById(R.id.listview)).setAdapter(new ArrayAdapter<String>(ma, R.layout.list_sauce_item, sauceItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(ma).inflate(R.layout.list_sauce_item, parent, false);
                }

                String currentItem = sauceItems[position];
                Button button = listItemView.findViewById(R.id.button);

                button.setOnClickListener(v -> {
                    urlET.setText(format("%s%s", sauceConfig.get(position).url, numberET.getText()));
                    url = format("https://www.%s", urlET.getText());
                });

                button.setText(currentItem);

                return listItemView;
            }
        });
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