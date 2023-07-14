package com.kitkat0712.soymilk;

import static android.app.NotificationManager.INTERRUPTION_FILTER_ALL;
import static android.app.NotificationManager.INTERRUPTION_FILTER_NONE;
import static android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static java.lang.String.format;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class MainActivity extends AppCompatActivity {
	public static MainActivity ma;

	public final int VERSION_DND = M;
	public final int VERSION_REQUEST_GETURL = LOLLIPOP;
	public int HISTORY_ITEM_HEIGHT;
	public int SCREEN_HEIGHT;

	public int choosenSauce = 0;
	public String number = null;
	public String url = "777";
	public boolean shouldIgnoreFocusChanged = false;
	public int dndOrigin;
	public int dndNow;

	public SwitchConfig switchConfig;
	public ArrayList<SauceConfig> sauceConfig;
	public ArrayList<BookmarkConfig> bookmarkConfig;
	public ArrayList<HistoryConfig> historyConfig;

	public File switchFile;
	public File sauceFile;
	public File bookmarkFile;
	public File historyFile;

	public ArrayList<String> switchList = new ArrayList<>();
	public ArrayList<String> sauceList = new ArrayList<>();
	public ArrayList<String> bookmarkList = new ArrayList<>();
	public ArrayList<String> historyList = new ArrayList<>();

	public File backgroundFile;
	public File maskFile;
	public Uri backgroundURI = null;
	public ImageView backgroundIV;
	public ImageView maskIV;

	private final FragmentManager fm = getSupportFragmentManager();
	private NotificationManager nm;

	public void debugCaution(String msg) {
		Toast.makeText(ma, format("CAUTION %s", msg), Toast.LENGTH_SHORT).show();
	}

	public boolean isLateEnough(final int REQUIRED_VERSION) {
		return SDK_INT >= REQUIRED_VERSION;
	}

	public boolean dndOnClick() {
		if (isLateEnough(VERSION_DND)) {
			NotificationManager nm = (NotificationManager) ma.getSystemService(NOTIFICATION_SERVICE);

			if (nm.isNotificationPolicyAccessGranted()) {
				dndNow = isDNDOn() ? INTERRUPTION_FILTER_ALL : INTERRUPTION_FILTER_NONE;

				nm.setInterruptionFilter(dndNow);
				return true;
			}
		} else {
			Toast.makeText(ma, format("Required API Level %d\nYour API Level %d", VERSION_DND, SDK_INT), Toast.LENGTH_LONG).show();
		}
		return false;
	}

	public boolean isDNDOn() {
		return dndNow >= INTERRUPTION_FILTER_PRIORITY;
	}

	public void replaceFragment(Fragment fragment) {
		ma.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.commit();
	}

	public void getJPG(Drawable drawable, File file) {
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		try {
			OutputStream outputStream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			outputStream.close();
		} catch (IOException ignored) {
		}
	}

	public void loadConfig(File file, ArrayList<String> list, int resource) {
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
				throw new IOException();
			}
		} catch (IOException ignored) {
			try {
				debugCaution("load config");

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
			} catch (IOException ignoredA) {
			}
		}
	}

	public <T> void writeListConfig(File file, ArrayList<T> config) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(config));
			bw.close();
		} catch (IOException ignored) {
		}
	}

	public void ftsio() {
		switchFile.delete();
		sauceFile.delete();
		historyFile.delete();
		bookmarkFile.delete();
		loadConfig(switchFile, switchList, R.raw.switch_config);
		loadConfig(sauceFile, sauceList, R.raw.sauce_config);
		loadConfig(historyFile, historyList, R.raw.history_config);
		loadConfig(bookmarkFile, bookmarkList, R.raw.bookmark_config);
	}

	public void staticLayout() {
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
		} catch (NullPointerException ignored) {
			loadConfig(switchFile, switchList, R.raw.switch_config);
		}
	}

	private int dp2px(float dp) {
		return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
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
			HISTORY_ITEM_HEIGHT = dp2px(40);
			SCREEN_HEIGHT = getResources().getDisplayMetrics().heightPixels;
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
			bookmarkFile = new File(getFilesDir(), "bookmark_config.json");
			loadConfig(switchFile, switchList, R.raw.switch_config);
			loadConfig(sauceFile, sauceList, R.raw.sauce_config);
			loadConfig(historyFile, historyList, R.raw.history_config);
			loadConfig(bookmarkFile, bookmarkList, R.raw.bookmark_config);

			try {
				switchConfig = new Gson().fromJson(String.join("\n", switchList), SwitchConfig.class);
				sauceConfig = new Gson().fromJson(String.join("\n", sauceList), new TypeToken<ArrayList<SauceConfig>>() {
				}.getType());
				historyConfig = new Gson().fromJson(String.join("\n", historyList), new TypeToken<ArrayList<HistoryConfig>>() {
				}.getType());
				bookmarkConfig = new Gson().fromJson(String.join("\n", bookmarkList), new TypeToken<ArrayList<BookmarkConfig>>() {
				}.getType());
			} catch (Exception ignored) {
				debugCaution("Gson");
				ftsio();
				finish();
			}

			staticLayout();

			Fragment testFragment = new HomeFragment();
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
}