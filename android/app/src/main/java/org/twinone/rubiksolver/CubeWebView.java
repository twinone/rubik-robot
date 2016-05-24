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
public class CubeWebView extends WebView {

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
        if (state != null) url += "?state="+state;
        loadUrl(url);
        setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                js("setState", "UUUUUULLLLLDLLDLLDFFFFFFFFFURRURRURRBBBBBBBBBRRRDDDDDD");
            }
        });
    }


    /**
     * @See http://stackoverflow.com/questions/4325639/android-calling-javascript-functions-in-webview
     */
    public void js(String methodName, Object...params){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{cube.");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        String separator = "";
        for (Object param : params) {
            stringBuilder.append(separator);
            separator = ",";
            if(param instanceof String){
                stringBuilder.append("'");
            }
            stringBuilder.append(param);
            if(param instanceof String){
                stringBuilder.append("'");
            }

        }
        stringBuilder.append(")}catch(error){console.error(error.message);}");
        final String call = stringBuilder.toString();
        Log.i("CubeWebView", "callJavaScript: call="+call);


        loadUrl(call);
    }



}
