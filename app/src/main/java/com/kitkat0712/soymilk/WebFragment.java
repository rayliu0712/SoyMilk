package com.kitkat0712.soymilk;

import static com.kitkat0712.soymilk.MainActivity.VERSION_REQUEST_GETURL;
import static com.kitkat0712.soymilk.MainActivity.config;
import static com.kitkat0712.soymilk.MainActivity.dndOnClick;
import static com.kitkat0712.soymilk.MainActivity.isDNDOn;
import static com.kitkat0712.soymilk.MainActivity.isLateEnough;
import static com.kitkat0712.soymilk.MainActivity.replaceFragment;
import static com.kitkat0712.soymilk.MainActivity.url;
import static com.kitkat0712.soymilk.MainActivity.viewOriginalColor;

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
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebFragment extends Fragment {
    private StringBuilder blocklist;
    private WebView webView;
    private ImageView dnd;

    private void setDNDVisual() {
        if (isDNDOn()) {
            dnd.setColorFilter(Color.RED);
        } else {
            dnd.setColorFilter(viewOriginalColor);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);

        view.findViewById(R.id.close).setOnClickListener(v -> replaceFragment(new HomeFragment()));
        view.findViewById(R.id.previous).setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        });
        view.findViewById(R.id.refresh).setOnClickListener(v -> webView.reload());
        dnd = view.findViewById(R.id.dnd);
        dnd.setOnClickListener(v -> {
            if (dndOnClick()) {
                setDNDVisual();
            }
        });

        webView = view.findViewById(R.id.web);
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        if(config.enableAdBlock) {
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    ByteArrayInputStream EMPTY3 = new ByteArrayInputStream("".getBytes());
                    String kk53 = String.valueOf(blocklist);//Load blocklist
                    if (isLateEnough(VERSION_REQUEST_GETURL)) {
                        if (kk53.contains(":::::" + request.getUrl().getHost())) {// If blocklist equals url = Block
                            return new WebResourceResponse("text/plain", "utf-8", EMPTY3);//Block
                        }
                    }
                    return super.shouldInterceptRequest(view, request);
                }
            });
        } else {
            webView.setWebViewClient(new WebViewClient());
        }

        webView.loadUrl(url);

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        setDNDVisual();
    }
}