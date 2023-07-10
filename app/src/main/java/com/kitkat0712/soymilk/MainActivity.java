package com.kitkat0712.soymilk;

import static android.app.NotificationManager.INTERRUPTION_FILTER_ALL;
import static android.app.NotificationManager.INTERRUPTION_FILTER_NONE;
import static android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.app.NotificationManager;
import android.content.Intent;
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
	private final Fragment testFragment = new HistoryFragment();
	public static HistoryConfig[] historyConfig;
	public static File historyFile;
	public static List<String> historyList = new ArrayList<>();




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
	public static List<SauceConfig> sauceConfig;
	public static File switchFile;
	public static File sauceFile;
	public static List<String> switchList = new ArrayList<>();
	public static List<String> sauceList = new ArrayList<>();

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

	public static void newSwitchConfigFile() {
		try {
			if (!switchFile.delete()) switchFile.createNewFile();

			InputStream is = ma.getResources().openRawResource(R.raw.switch_config);
			BufferedWriter bw = new BufferedWriter(new FileWriter(switchFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
				bw.write(line);
				bw.newLine();
				switchList.add(line);
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void newSauceConfigFile() {
		try {
			if (!sauceFile.delete()) sauceFile.createNewFile();

			InputStream is = ma.getResources().openRawResource(R.raw.sauce_config);
			BufferedWriter bw = new BufferedWriter(new FileWriter(sauceFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
				bw.write(line);
				bw.newLine();
				sauceList.add(line);
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void newHistoryConfigFile() {
		try {
			if (!historyFile.delete()) historyFile.createNewFile();

			InputStream is = ma.getResources().openRawResource(R.raw.history_config);
			BufferedWriter bw = new BufferedWriter(new FileWriter(historyFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
				bw.write(line);
				bw.newLine();
				historyList.add(line);
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			// init public variables
			nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			maskIV = findViewById(R.id.mask);

			// load switch_config.json
			switchFile = new File(getFilesDir(), "switch_config.json");

			try {
				if (switchFile.exists()) {
					BufferedReader br = new BufferedReader(new FileReader(switchFile));
					StringBuilder sb = new StringBuilder();
					String line;

					while ((line = br.readLine()) != null) {
						sb.append(line);
						switchList.add(line);
					}
					br.close();
				} else {
					newSwitchConfigFile();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			switchConfig = new Gson().fromJson(String.join("\n", switchList), SwitchConfig.class);

			// load sauce_config.json
			{
				sauceFile = new File(getFilesDir(), "sauce_config.json");

				try {
					if (sauceFile.exists()) {
						BufferedReader br = new BufferedReader(new FileReader(sauceFile));
						StringBuilder sb = new StringBuilder();
						String line;

						while ((line = br.readLine()) != null) {
							sb.append(line);
							sauceList.add(line);
						}
						br.close();
					} else {
						newSauceConfigFile();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				sauceConfig = new Gson().fromJson(String.join("\n", sauceList), new TypeToken<List<SauceConfig>>(){}.getType());
			}

			// load history_config.json
			{
				historyFile = new File(getFilesDir(), "history_config.json");

				try {
					if (historyFile.exists()) {
						BufferedReader br = new BufferedReader(new FileReader(historyFile));
						StringBuilder sb = new StringBuilder();
						String line;

						while ((line = br.readLine()) != null) {
							sb.append(line);
							historyList.add(line);
						}
						br.close();
					} else {
						newHistoryConfigFile();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				historyConfig = new Gson().fromJson(String.join("\n", historyList), HistoryConfig[].class);
			}

			// load image
			{
				backgroundFile = new File(getFilesDir(), "background.jpg");
				maskFile = new File(getFilesDir(), "mask.jpg");

				if (backgroundFile.exists())
					backgroundURI = Uri.fromFile(backgroundFile);

				if (maskFile.exists())
					maskIV.setImageURI(Uri.fromFile(maskFile));
			}

			// set fragment
			{
				fm.beginTransaction().add(R.id.fragment_container, testFragment).commit();
			}
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

	private void foo(File file, List<String> stringList) {
		try {
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				StringBuilder sb = new StringBuilder();
				String line;

				while ((line = br.readLine()) != null) {
					sb.append(line);
					stringList.add(line);
				}
				br.close();
			} else {
				newSwitchConfigFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}