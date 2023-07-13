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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebFragment extends Fragment {
	private StringBuilder blocklist;
	private ImageView dndIV;

	private void setDNDVisual() {
		if (ma.isDNDOn()) {
			dndIV.setColorFilter(Color.RED);
		} else {
			dndIV.clearColorFilter();
		}
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
				public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
					String url = request.getUrl().toString();
					Toast.makeText(ma, url, Toast.LENGTH_SHORT).show();
					return false;
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
		String testUrl = String.format("https://www.%s", ma.url);
		wv.loadUrl(testUrl);
		Toast.makeText(ma, testUrl, Toast.LENGTH_SHORT).show();


		view.findViewById(R.id.close).setOnClickListener(v -> {
			HistoryConfig hc = new HistoryConfig();
			hc.date = "6969";
			hc.url = "666777888999";
			ma.writeListConfig(ma.historyFile, ma.historyConfig);
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