package org.twinone.rubiksolver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * Created by xavier on 06/04/16.
 */
public class CubeWebView extends WebView {

    public CubeWebView(Context context) {
        this(context, null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public CubeWebView(Context context, String state) {
        super(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // So we can debug on chrome
            setWebContentsDebuggingEnabled(true);
        }
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(this, "Android");
        String url = "file:///android_asset/web/index.html";
        if (state != null) url += "?state="+state;
        loadUrl(url);
    }

    @JavascriptInterface
    public void setState(String state){

    }





}
