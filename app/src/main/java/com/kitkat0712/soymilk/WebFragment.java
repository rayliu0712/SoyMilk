package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.ma;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
	private ImageView dndIV;
	private boolean isFirstLoaded = true;

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_web, container, false);
		WebView wv = view.findViewById(R.id.webview);
		WebSettings webSettings = wv.getSettings();

		dndIV = view.findViewById(R.id.dnd);
		dndIV.setOnClickListener(v -> {
			if (ma.dndOnClick()) {
				setDNDVisual();
			}
		});

		wv.clearHistory();
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setDomStorageEnabled(false);
		webSettings.setSaveFormData(false);
		CookieManager.getInstance().setAcceptCookie(false);

		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		if (ma.switchConfig.enableAdBlock) {
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

			wv.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					if (ma.url.equals(trimProtocol(view.getUrl())) && !isFirstLoaded) return;

					isFirstLoaded = false;
					ma.url = trimProtocol(view.getUrl());
					writeHistory(ma.url);
				}

				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
					ByteArrayInputStream EMPTY3 = new ByteArrayInputStream("".getBytes());
					String kk53 = String.valueOf(blocklist);//Load blocklist
					if (ma.isLateEnough(ma.VERSION_REQUEST_GETURL)) {
						if (kk53.contains(":::::" + request.getUrl().getHost())) {// If blocklist equals url = Block
							return new WebResourceResponse("text/plain", "utf-8", EMPTY3);//Block
						}
					}
					return super.shouldInterceptRequest(view, request);
				}
			});
		} else {
			wv.setWebViewClient(new WebViewClient());
		}
		wv.loadUrl(String.format("https://%s", ma.url));

		view.findViewById(R.id.close).setOnClickListener(v -> {
			// write
			ma.replaceFragment(new HomeFragment());
		});
		view.findViewById(R.id.previous).setOnClickListener(v -> {
			if (wv.canGoBack()) {
				wv.goBack();
			}
		});
		view.findViewById(R.id.refresh).setOnClickListener(v -> {
			v.setRotation(v.getRotation() + 90);
			wv.reload();
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setDNDVisual();
	}
}