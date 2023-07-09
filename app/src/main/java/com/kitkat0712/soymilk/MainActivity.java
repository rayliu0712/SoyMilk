package com.kitkat0712.soymilk;

import static android.app.NotificationManager.INTERRUPTION_FILTER_ALL;
import static android.app.NotificationManager.INTERRUPTION_FILTER_NONE;
import static android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	// test
	private Fragment testFragment;
	public static final String tag = "tag";

	public static final int VERSION_DND = M;
	public static final int VERSION_SETTINT = LOLLIPOP;
	public static final int VERSION_REQUEST_GETURL = LOLLIPOP;

	public static String url = "https://www.google.com";
	public static boolean shouldIgnoreFocusChanged = false;
	public static MainActivity ma;
	public static int dndOrigin;
	public static int dndNow;
	public static int viewOriginalColor;

	public static SwitchConfig switchConfig;
	public static ArrayList<SauceConfig> sauceConfig;
	public static ArrayList<HistoryConfig> historyConfig;

	public static File switchFile;
	public static File sauceFile;
	public static File historyFile;
	public static ArrayList<String> switchList = new ArrayList<>();
	public static ArrayList<String> sauceList = new ArrayList<>();
	public static ArrayList<String> historyList = new ArrayList<>();

	public static File backgroundFile;
	public static File maskFile;
	public static Uri backgroundURI = null;
	public static ImageView backgroundIV;
	public static ImageView maskIV;

	private final FragmentManager fm = getSupportFragmentManager();
	private NotificationManager nm;

	public static boolean isLateEnough(final int REQUIRED_VERSION) {
		return SDK_INT >= REQUIRED_VERSION;
	}

	public static boolean dndOnClick() {
		if (isLateEnough(VERSION_DND)) {
			NotificationManager nm = (NotificationManager) ma.getSystemService(NOTIFICATION_SERVICE);

			if (nm.isNotificationPolicyAccessGranted()) {
				dndNow = isDNDOn() ? INTERRUPTION_FILTER_ALL : INTERRUPTION_FILTER_NONE;

				nm.setInterruptionFilter(dndNow);
				return true;
			}
		} else {
			Toast.makeText(ma, String.format("Required API Level %d\nYour API Level %d", VERSION_DND, SDK_INT), Toast.LENGTH_LONG).show();
		}
		return false;
	}

	public static boolean isDNDOn() {
		return dndNow >= INTERRUPTION_FILTER_PRIORITY;
	}

	public static void replaceFragment(Fragment fragment) {
		ma.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.commit();
	}

	public static void getJPG(Drawable drawable, File file) {
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		try {
			OutputStream outputStream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MainActivity() {
		ma = this;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// setter
		{
			nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			if (SDK_INT >= VERSION_DND) {
				if (!nm.isNotificationPolicyAccessGranted()) {
					Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
					startActivity(intent);
				}
			}

			maskIV = findViewById(R.id.mask);
			backgroundFile = new File(getFilesDir(), "background.jpg");
			maskFile = new File(getFilesDir(), "mask.jpg");
			if (backgroundFile.exists()) {
				backgroundURI = Uri.fromFile(backgroundFile);
			}
			if (maskFile.exists()) {
				maskIV.setImageURI(Uri.fromFile(maskFile));
			}

			switchFile = new File(getFilesDir(), "switch_config.json");
			sauceFile = new File(getFilesDir(), "sauce_config.json");
			historyFile = new File(getFilesDir(), "history_config.json");
			loadConfig(switchFile, switchList, R.raw.switch_config);
			loadConfig(sauceFile, sauceList, R.raw.sauce_config);
			loadConfig(historyFile, historyList, R.raw.history_config);
			switchConfig = new Gson().fromJson(String.join("\n", switchList), SwitchConfig.class);
			sauceConfig = new Gson().fromJson(String.join("\n", sauceList), new TypeToken<ArrayList<SauceConfig>>(){}.getType());
			historyConfig = new Gson().fromJson(String.join("\n", historyList), new TypeToken<ArrayList<HistoryConfig>>(){}.getType());

			testFragment = new HomeFragment();
			fm.beginTransaction().add(R.id.fragment_container, testFragment).commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isLateEnough(VERSION_DND)) {
			dndOrigin = nm.getCurrentInterruptionFilter();
			dndNow = dndOrigin;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isLateEnough(VERSION_DND)) {
			nm.setInterruptionFilter(dndOrigin);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean b) {
		if (shouldIgnoreFocusChanged) {
			shouldIgnoreFocusChanged = false;
			return;
		}
		if (!switchConfig.enableMask) return;

		maskIV.setVisibility(b ? View.GONE : View.VISIBLE);
	}

	public static void loadConfig(File file, ArrayList<String> list, int resource) {
		BufferedReader br;
		BufferedWriter bw;
		StringBuilder sb;
		String line;
		InputStream is;

		try {
			if (file.exists()) {
				br = new BufferedReader(new FileReader(file));
				sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
					list.add(line);
				}
				br.close();
			} else {
				if (!file.delete()) file.createNewFile();

				is = ma.getResources().openRawResource(resource);
				bw = new BufferedWriter(new FileWriter(file));
				br = new BufferedReader(new InputStreamReader(is));
				sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
					bw.write(line);
					bw.newLine();
					list.add(line);
				}
				bw.close();
				br.close();
			}
		} catch (IOException ignored) {
		}
	}
}