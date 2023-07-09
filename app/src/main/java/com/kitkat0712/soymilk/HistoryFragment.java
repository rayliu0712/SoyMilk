package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.historyConfig;
import static com.kitkat0712.soymilk.MainActivity.ma;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        ((ListView) view.findViewById(R.id.listview)).setAdapter(
                new ArrayAdapter<HistoryConfig>(ma, R.layout.list_history_item, historyConfig) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View listItemView = convertView;
                        if (listItemView == null) {
                            listItemView = LayoutInflater.from(ma).inflate(R.layout.list_history_item, parent, false);
                        }

                        HistoryConfig currentItem = historyConfig[position];

                        ((TextView) listItemView.findViewById(R.id.date)).setText(currentItem.date);
                        ((TextView) listItemView.findViewById(R.id.url)).setText(currentItem.url);

                        return listItemView;
                    }
                }
        );

        return view;
    }
}