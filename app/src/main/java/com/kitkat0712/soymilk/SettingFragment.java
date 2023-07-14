package com.kitkat0712.soymilk;

import static android.app.Activity.RESULT_OK;
import static com.kitkat0712.soymilk.MainActivity.ma;
import static com.kitkat0712.soymilk.SwitchConfig.length;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private static final String[] switchItems = {"隱藏狀態列", "隱藏導覽列", "啟用flag_secure", "啟用AdBlock", "啟用歷史紀錄", "啟用遮罩", "使用數字鍵盤"};
    private static final String[] buttonItems = {"[ 設定主頁面背景圖片 ]", "[ 設定遮罩圖片 ]", "[ 新增醬汁 ]", "[ 關於AdBlock ]", "[ 關於... ]"};
    private static final int REQUEST_PICK_BACKGROUND_PIC = 6;
    private static final int REQUEST_PICK_MASK_PIC = 7;
    private boolean[] buffArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        buffArray = ma.switchConfig.toArray();
        ListView switchLV = view.findViewById(R.id.switch_listview);
        switchLV.setAdapter(new ArrayAdapter<String>(ma, R.layout.listitem_switch, switchItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(ma).inflate(R.layout.listitem_switch, parent, false);
                }
                String currentItem = switchItems[position];

                TextView textView = listItemView.findViewById(R.id.textview);
                Switch theSwitch = listItemView.findViewById(R.id.the_switch);
                theSwitch.setChecked(buffArray[position]);

                textView.setText(currentItem);
                textView.setOnClickListener(v -> theSwitch.setChecked(!theSwitch.isChecked()));

                return listItemView;
            }
        });

        ((ListView) view.findViewById(R.id.button_listview)).setAdapter(new ArrayAdapter<String>(ma, R.layout.listitem_button, buttonItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                String currentItem = buttonItems[position];
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(ma).inflate(R.layout.listitem_button, parent, false);
                }

                TextView textView = listItemView.findViewById(R.id.textview);
                textView.setText(currentItem);
                textView.setOnClickListener(v -> {
                    switch (position) {
                        case 0:
                            ma.shouldIgnoreFocusChanged = true;
                            new AlertDialog.Builder(ma)
                                    .setTitle("Background Setting")
                                    .setMessage("Remove OR Update ?")
                                    .setPositiveButton("Update", (dialogInterface, i) -> {
                                        ma.shouldIgnoreFocusChanged = true;
                                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_PICK_BACKGROUND_PIC);
                                    })
                                    .setNegativeButton("Remove", (dialogInterface, i) -> {
                                        ma.backgroundURI = null;
                                        ma.backgroundFile.delete();
                                        Toast.makeText(ma, "Removed", Toast.LENGTH_SHORT).show();
                                    })
                                    .create().show();
                            break;
                        case 1:
                            ma.shouldIgnoreFocusChanged = true;
                            new AlertDialog.Builder(ma)
                                    .setTitle("Mask Setting")
                                    .setMessage("Remove OR Update ?")
                                    .setPositiveButton("Update", (dialogInterface, i) -> {
                                        ma.shouldIgnoreFocusChanged = true;
                                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_PICK_MASK_PIC);
                                    })
                                    .setNegativeButton("Remove", (dialogInterface, i) -> {
                                        ma.maskFile.delete();
                                        Toast.makeText(ma, "Removed", Toast.LENGTH_SHORT).show();
                                    })
                                    .create().show();
                            break;
                        case 2:
                            ma.shouldIgnoreFocusChanged = true;
                            View dialogView = View.inflate(ma, R.layout.dialog_sauce, null);
                            EditText nameET = dialogView.findViewById(R.id.name);
                            EditText urlET = dialogView.findViewById(R.id.url);

                            new AlertDialog.Builder(ma)
                                    .setCancelable(false)
                                    .setView(dialogView)
                                    .setTitle("New Sauce")
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        ma.sauceConfig.add(0, new SauceConfig(nameET.getText().toString(), urlET.getText().toString()));
                                        ma.writeListConfig(ma.sauceFile, ma.sauceConfig);
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            break;
                        case 3:
                            ma.shouldIgnoreFocusChanged = true;
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/marcelbohland/Android-Webview-Adblock")));
                            break;
                        case 4:
                            ma.shouldIgnoreFocusChanged = true;
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KitKat-0712/SoyMilk")));
                            break;
                    }
                });

                return listItemView;
            }
        });

        FloatingActionButton save = view.findViewById(R.id.save);
        save.setOnClickListener(v -> {
            for (int i = 0; i < length; ++i) {
                buffArray[i] = ((Switch) switchLV.getChildAt(i).findViewById(R.id.the_switch)).isChecked();
            }

            ma.switchConfig.update(buffArray);

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(ma.switchFile));
                String head = ma.switchList.get(0);
                String tail = ma.switchList.get(ma.switchList.size() - 1);

                bw.write(head);
                bw.newLine();
                for (int i = 0; i < length; ++i) {
                    String line = ma.switchList.get(i + 1);

                    if (line.contains("true"))
                        line = line.replace("true", String.valueOf(buffArray[i]));
                    else if (line.contains("false"))
                        line = line.replace("false", String.valueOf(buffArray[i]));

                    bw.write(line);
                    bw.newLine();
                }
                bw.write(tail);
                bw.newLine();
                bw.close();
            } catch (IOException ignored) {
            }
            Toast.makeText(ma, "Saved", Toast.LENGTH_SHORT).show();
            ma.replaceFragment(new HomeFragment());
        });
        save.setOnLongClickListener(v -> {
            Toast.makeText(ma, "Discarded", Toast.LENGTH_SHORT).show();
            ma.replaceFragment(new HomeFragment());
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
            ma.backgroundURI = data.getData();
        } else if (requestCode == REQUEST_PICK_MASK_PIC) {
            ma.maskIV.setImageURI(data.getData());
            ma.getJPG(ma.maskIV.getDrawable(), ma.maskFile);
        }
        Toast.makeText(ma, "Update Successfully", Toast.LENGTH_SHORT).show();
    }
}