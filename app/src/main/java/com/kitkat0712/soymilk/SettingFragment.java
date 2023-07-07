package com.kitkat0712.soymilk;

import static android.app.Activity.RESULT_OK;
import static com.kitkat0712.soymilk.Config.boolLength;
import static com.kitkat0712.soymilk.Config.buttonItems;
import static com.kitkat0712.soymilk.Config.switchItems;
import static com.kitkat0712.soymilk.MainActivity.backgroundFile;
import static com.kitkat0712.soymilk.MainActivity.backgroundURI;
import static com.kitkat0712.soymilk.MainActivity.config;
import static com.kitkat0712.soymilk.MainActivity.configFile;
import static com.kitkat0712.soymilk.MainActivity.getJPG;
import static com.kitkat0712.soymilk.MainActivity.jsonStringList;
import static com.kitkat0712.soymilk.MainActivity.ma;
import static com.kitkat0712.soymilk.MainActivity.maskFile;
import static com.kitkat0712.soymilk.MainActivity.maskIV;
import static com.kitkat0712.soymilk.MainActivity.replaceFragment;
import static com.kitkat0712.soymilk.MainActivity.shouldIgnoreFocusChanged;
import static java.lang.String.valueOf;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SettingFragment extends Fragment {
    private static final int REQUEST_PICK_BACKGROUND_PIC = 6;
    private static final int REQUEST_PICK_MASK_PIC = 7;
    private boolean[] buffArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        buffArray = config.toBoolArray();

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        ListView switchLV = view.findViewById(R.id.switch_listview);
        ArrayAdapter<String> switchAP = new ArrayAdapter<String>(ma, R.layout.list_switch_item, switchItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(ma).inflate(R.layout.list_switch_item, parent, false);
                }

                String currentItem = switchItems[position];

                TextView textView = listItemView.findViewById(R.id.textview);
                Switch theSwitch = listItemView.findViewById(R.id.the_switch);
                theSwitch.setChecked(buffArray[position]);

                textView.setText(currentItem);
                textView.setOnClickListener(v -> theSwitch.setChecked(!theSwitch.isChecked()));

                return listItemView;
            }
        };
        switchLV.setAdapter(switchAP);

        ListView buttonLV = view.findViewById(R.id.button_listview);
        ArrayAdapter<String> buttonAP = new ArrayAdapter<String>(ma, R.layout.list_button_item, buttonItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(ma).inflate(R.layout.list_button_item, parent, false);
                }

                String currentItem = buttonItems[position];

                TextView textView = listItemView.findViewById(R.id.textview);
                textView.setText(currentItem);

                textView.setOnClickListener(v -> {
                    shouldIgnoreFocusChanged = true;

                    switch (position) {
                        case 0:
                            new AlertDialog.Builder(ma)
                                    .setTitle("Background Setting")
                                    .setMessage("Remove OR Update ?")
                                    .setPositiveButton("Update", (dialogInterface, i) -> {
                                        shouldIgnoreFocusChanged = true;
                                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_PICK_BACKGROUND_PIC);
                                    })
                                    .setNegativeButton("Remove", (dialogInterface, i) -> {
                                        backgroundURI = null;
                                        backgroundFile.delete();
                                        Toast.makeText(ma, "Removed", Toast.LENGTH_SHORT).show();
                                    })
                                    .create().show();
                            break;
                        case 1:
                            new AlertDialog.Builder(ma)
                                    .setTitle("Mask Setting")
                                    .setMessage("Remove OR Update ?")
                                    .setPositiveButton("Update", (dialogInterface, i) -> {
                                        shouldIgnoreFocusChanged = true;
                                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_PICK_MASK_PIC);
                                    })
                                    .setNegativeButton("Remove", (dialogInterface, i) -> {
                                        maskFile.delete();
                                        Toast.makeText(ma, "Removed", Toast.LENGTH_SHORT).show();
                                    })
                                    .create().show();
                            break;
                        case 2:
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/marcelbohland/Android-Webview-Adblock")));
                            break;
                        case 3:
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KitKat-0712/SoyMilk")));
                            break;
                    }
                });

                return listItemView;
            }
        };
        buttonLV.setAdapter(buttonAP);

        FloatingActionButton save = view.findViewById(R.id.save);
        save.setOnClickListener(v -> {
            for (int i = 0; i < boolLength; ++i) {
                buffArray[i] = ((Switch) switchLV.getChildAt(i).findViewById(R.id.the_switch)).isChecked();
            }

            config.updateBool(buffArray);

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));

                for (int i = 0; i < jsonStringList.size(); ++i) {
                    String line = jsonStringList.get(i);

                    if (i < buffArray.length) {
                        if (line.contains("true"))
                            line = line.replace("true", valueOf(buffArray[i]));
                        else if (line.contains("false"))
                            line = line.replace("false", valueOf(buffArray[i]));
                    }

                    bw.write(line);
                    bw.newLine();
                }
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(ma, "Saved", Toast.LENGTH_SHORT).show();
            replaceFragment(new HomeFragment());
        });
        save.setOnLongClickListener(v -> {
            Toast.makeText(ma, "Discarded", Toast.LENGTH_SHORT).show();
            replaceFragment(new HomeFragment());
            return true;
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            Toast.makeText(ma, "Update Failed", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == REQUEST_PICK_BACKGROUND_PIC) {
            backgroundURI = data.getData();
        } else if (requestCode == REQUEST_PICK_MASK_PIC) {
            maskIV.setImageURI(data.getData());
            getJPG(maskIV.getDrawable(), maskFile);
        }
        Toast.makeText(ma, "Update Successfully", Toast.LENGTH_SHORT).show();
    }
}