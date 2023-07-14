package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.ma;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
            @Nullable
            @Override
            public BookmarkConfig getItem(int position) {
                return ma.bookmarkConfig.get(position);
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                BookmarkConfig currentItem = getItem(position);
                if (convertView == null) {
                    convertView = LayoutInflater.from(ma).inflate(R.layout.listitem_library, parent, false);
                }
                ((TextView) convertView.findViewById(R.id.name)).setText(currentItem.name);
                ((TextView) convertView.findViewById(R.id.number)).setText(currentItem.number);

                convertView.setOnClickListener(v -> {
                    ma.url = currentItem.url;
                    ma.number = currentItem.number;
                    ma.replaceFragment(new WebFragment());
                });
                convertView.setOnLongClickListener(v -> {
                    ma.shouldIgnoreFocusChanged = true;
                    View dialogView = View.inflate(ma, R.layout.dialog_bookmark, null);
                    EditText nameET = dialogView.findViewById(R.id.name);
                    nameET.setText(currentItem.name);
                    ((EditText)dialogView.findViewById(R.id.number)).setText(currentItem.number);
                    ((EditText)dialogView.findViewById(R.id.url)).setText(currentItem.url);

                    new AlertDialog.Builder(ma)
                            .setView(dialogView)
                            .setCancelable(false)
                            .setTitle("Edit Bookmark")
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                ma.bookmarkConfig.set(position,
                                        new BookmarkConfig(
                                                nameET.getText().toString(),
                                                currentItem.number,
                                                currentItem.url
                                        )
                                );
                                ma.writeListConfig(ma.bookmarkFile, ma.bookmarkConfig);
                                notifyDataSetChanged();
                            })
                            .setNegativeButton("Cancel", null)
                            .setNeutralButton("Remove", (dialogInterface, i) -> {
                                ma.bookmarkConfig.remove(position);
                                ma.writeListConfig(ma.bookmarkFile, ma.bookmarkConfig);
                                notifyDataSetChanged();
                            })
                            .show();
                    return false;
                });
                return convertView;
            }
        });
        return view;
    }
}