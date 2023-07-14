package com.kitkat0712.soymilk;

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
    private String[] sauceItems;
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
            if (urlET.getText().toString().equals(ma.bookmarkConfig.get(i).url)) {
                setBookmarkVisual(i);
                break;
            } else {
                setBookmarkVisual(-1);
            }
        }
    }

    private void syncUrl() {
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
        urlET.setOnLongClickListener(v -> {
            urlET.setText(ma.pasteFromClipboard());
            return true;
        });
        urlET.setOnFocusChangeListener((v, b) -> urlET.setHint(b ? "" : "URL"));
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
        numberET.setOnLongClickListener(v -> {
            numberET.setText("");
            return true;
        });
        numberET.setOnFocusChangeListener((v, b) -> numberET.setHint(b ? "" : "SoyMilk"));
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

                syncUrl();
                checkBookmark();
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

            checkBookmark();
        }

        bookmarkIV.setOnClickListener(v -> {
            ma.shouldIgnoreFocusChanged = true;
            View dialogView = View.inflate(ma, R.layout.dialog_bookmark, null);
            ((EditText) dialogView.findViewById(R.id.number)).setText(numberET.getText().toString());
            ((EditText) dialogView.findViewById(R.id.url)).setText(urlET.getText().toString());

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
                                        urlET.getText().toString())
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
            ma.paste2Clipboard(urlET.getText().toString());
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
        lv.setAdapter(new ArrayAdapter<String>(ma, R.layout.listitem_sauce, sauceItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                String currentItem = sauceItems[position];
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(ma).inflate(R.layout.listitem_sauce, parent, false);
                }

                Button button = listItemView.findViewById(R.id.button);
                button.setText(ma.choosenSauce == position ? String.format("[ %s ]", currentItem) : currentItem);
                button.setOnClickListener(v -> {
                    button.setText(String.format("[ %s ]", currentItem));
                    if (ma.choosenSauce != position) {
                        Button previousBtn = lv.getChildAt(ma.choosenSauce).findViewById(R.id.button);
                        previousBtn.setText(previousBtn.getText().toString().replace("[ ", "").replace(" ]", ""));
                    }
                    ma.choosenSauce = position;
                    syncUrl();
                    checkBookmark();
                });
                button.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(ma)
                            .setCancelable(false)
                            .setView(View.inflate(ma, R.layout.dialog_sauce, null))
                            .setPositiveButton("OK", (dialogInterface, i) -> {

                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });

                return listItemView;
            }
        });
        view.findViewById(R.id.setting).setOnClickListener(v -> ma.replaceFragment(new SettingFragment()));
        view.findViewById(R.id.go_default).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + ma.url));
            startActivity(intent);
        });
        view.findViewById(R.id.go_webview).setOnClickListener(v -> ma.replaceFragment(new WebFragment()));

        isOnCreateView = false;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setDNDVisual();
    }
}