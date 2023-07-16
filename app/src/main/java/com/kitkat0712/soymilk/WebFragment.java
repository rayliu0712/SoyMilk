package com.kitkat0712.soymilk;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static com.kitkat0712.soymilk.MainActivity.ma;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WebFragment extends Fragment {
	private StringBuilder blocklist;
	public WebView wv;
	private ImageView dndIV;

	private void setDNDVisual() {
		if (ma.isDNDOn()) {
			dndIV.setColorFilter(Color.RED);
		} else {
			dndIV.clearColorFilter();
		}
	}

	private String trimProtocol(String url) {
		return url.replaceAll("^(https?://)", "");
	}

	private String completeUrl(String url) {
		return String.format("https://%s", url);
	}

	private void writeHistory(String url) {
		if (!ma.switchConfig.enableRecord) return;

		String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
		url = trimProtocol(url);

		try {
			if (date.equals(ma.historyConfig.get(0).date)) {
				ma.historyConfig.get(0).url.add(0, url);
			} else {
				throw new IndexOutOfBoundsException();
			}
		} catch (IndexOutOfBoundsException e) {
			ArrayList<String> arrayList = new ArrayList<>();
			arrayList.add(url);

			HistoryConfig historyConfig = new HistoryConfig();
			historyConfig.date = date;
			historyConfig.url = arrayList;

			ma.historyConfig.add(0, historyConfig);
		}

		ma.writeListConfig(ma.historyFile, ma.historyConfig);
	}

	private void fuckWebViewClient() {
		wv.post(() -> {
			if (wv.getProgress() == 100) {
				try {
					if (!ma.historyConfig.get(0).url.get(0).equals(trimProtocol(wv.getUrl()))) {
						throw new IndexOutOfBoundsException();
					}
				} catch (IndexOutOfBoundsException e) {
					writeHistory(trimProtocol(wv.getUrl()));
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_web, container, false);
		ma.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		wv = view.findViewById(R.id.webview);
		WebSettings webSettings = wv.getSettings();

		dndIV = view.findViewById(R.id.dnd);
		if (SDK_INT >= M) {
			dndIV.setOnClickListener(v -> {
				if (ma.dndOnClick()) {
					setDNDVisual();
				}
			});
		} else {
			dndIV.setEnabled(false);
		}

		wv.clearHistory();
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setSaveFormData(false);

		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);

		// load simplehttpserver.txt
		{
			String strLine2;
			blocklist = new StringBuilder();

			InputStream fis2 = this.getResources().openRawResource(R.raw.adblockserverlist);//Storage location
			BufferedReader br2 = new BufferedReader(new InputStreamReader(fis2));
			if (fis2 != null) {
				try {
					while ((strLine2 = br2.readLine()) != null) {
						blocklist.append(strLine2);//if ":::::" exists in blocklist | Line for Line
						blocklist.append("\n");
					}
				} catch (IOException ignored) {
				}
			}
		}

		wv.loadUrl(completeUrl(ma.url));
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				ma.shouldIgnoreFocusChanged = true;
				new AlertDialog.Builder(ma)
						.setCancelable(false)
						.setTitle("Loading Error")
						.setMessage(description)
						.setPositiveButton("OK", null)
						.show();
			}

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				fuckWebViewClient();

				if (!ma.switchConfig.enableAdBlock)
					return super.shouldInterceptRequest(view, request);

				ByteArrayInputStream EMPTY3 = new ByteArrayInputStream("".getBytes());
				String kk53 = String.valueOf(blocklist);//Load blocklist
				if (SDK_INT >= LOLLIPOP) {
					if (kk53.contains(":::::" + request.getUrl().getHost())) {// If blocklist equals url = Block
						return new WebResourceResponse("text/plain", "utf-8", EMPTY3);//Block
					}
				}
				return super.shouldInterceptRequest(view, request);
			}
		});

		view.findViewById(R.id.close).setOnClickListener(v -> ma.replaceFragment(new HomeFragment()));
		view.findViewById(R.id.previous).setOnClickListener(v -> {
			if (wv.canGoBack()) {
				wv.goBack();
			}
		});
		view.findViewById(R.id.refresh).setOnClickListener(v -> {
			v.setRotation(v.getRotation() + 90);
			wv.reload();
		});
		view.findViewById(R.id.info).setOnClickListener(v -> {
			ma.shouldIgnoreFocusChanged = true;
			View dialogView = View.inflate(ma, R.layout.dialog_navigation, null);
			EditText navigationUrlET = dialogView.findViewById(R.id.url);
			navigationUrlET.setText(wv.getUrl());
			new AlertDialog.Builder(ma)
					.setView(dialogView)
					.setCancelable(false)
					.setTitle("Navigation")
					.setPositiveButton("OK", (dialogInterface, i) -> {
						wv.clearCache(true);
						wv.loadUrl(navigationUrlET.getText().toString());
					})
					.setNegativeButton("Cancel", null)
					.show();
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setDNDVisual();
	}

	@Override
	public void onPause() {
		super.onPause();
		ma.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}