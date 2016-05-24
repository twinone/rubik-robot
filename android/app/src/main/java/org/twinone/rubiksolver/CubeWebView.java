package org.twinone.rubiksolver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by xavier on 06/04/16.
 */
public class CubeWebView extends JSWebView {

    public CubeWebView(Context context) {
        this(context, null);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    public CubeWebView(Context context, String state) {
        super(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // So we can debug on chrome
            setWebContentsDebuggingEnabled(true);
        }
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(this, "Android");

        String url = "file:///android_asset/web/index.html";
        if (state != null) url += "?state=" + state;
        loadUrl(url);
    }

    public Object cube(String methodName, Object... params) {
        return javaScript("cube." + methodName, params);
    }


}
