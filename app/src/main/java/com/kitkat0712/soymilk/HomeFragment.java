package com.kitkat0712.soymilk;


import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class HomeFragment extends Fragment {
    final private String APP_NAME = "SoyMilk";
    final private int REQUEST_IMAGE_PICKER = 1;
    static private Uri backgroundURI = null;
    private EditText numberET;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ((ImageView) view.findViewById(R.id.background)).setImageURI(backgroundURI);

        /*view.findViewById(R.id.picker).setOnClickListener(v -> {
            MainActivity.readyToPick = true;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICKER);
        });*/

        numberET = view.findViewById(R.id.number);
        numberET.setOnFocusChangeListener((v, b) -> {
            if (b) {
                numberET.setHint("");
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            MaskFragment.maskURI = data.getData();
            backgroundURI = data.getData();
            MainActivity.readyToPick = false;

            Uri uri = data.getData();

            try {
                File file = new File(getActivity().getFilesDir(), "config.txt");

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file);
                fw.write(uri.toString());
                fw.close();

            } catch (Exception ignored) {
            }
        }
    }

    public void hideKeyboard(IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    public void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }
}