package com.kitkat0712.soymilk;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {
    static public boolean readyToPick = false;
    public FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set layout & flag
        {
            Window window = getWindow();

            // enable fullscreen
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // secure flag
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

            // hide navigation & fullscreen
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            window.getDecorView().setSystemUiVisibility(uiOptions);
        }

        // set fragment
        {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, new HomeFragment()).add(R.id.fragment_container, new MaskFragment())
                    .commit();

            replaceFragment(new HomeFragment());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(readyToPick) return;
        replaceFragment(hasFocus? new HomeFragment(): new MaskFragment());
    }

    public void replaceFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}