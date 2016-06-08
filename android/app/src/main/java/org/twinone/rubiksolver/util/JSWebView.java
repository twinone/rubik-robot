package org.twinone.rubiksolver.util;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * @author twinone
 */
public class JSWebView extends WebView {

    private int mId;
    private final Object mIdLock = new Object();

    private final Map<Integer, String> mPendingCalls = new HashMap<>();

    public JSWebView(Context context) {
        super(context);
        addJavascriptInterface(this, "AndroidJavascriptCallback");

    }

    public void js(String function, Object... params) {
        String js = constructFunction(function, params);
        evaluateJavascript(js, null);
    }

    public synchronized Object jsSync(String function, Object... params) {
        final int id;
        synchronized (mIdLock) {
            id = mId++;
        }

        final String call = "javascript:" +
                "try {" +
                "var res = " + constructFunction(function, params) + ";" +
                "res = JSON.stringify(res);" +
                "AndroidJavascriptCallback.onResult(" + id + ", res);" +
                "} catch (e) {" +
                "console.error('Error running from java:', e)" +
                "}";
        loadUrl(call);

        synchronized (mPendingCalls) {
            while (!mPendingCalls.containsKey(id)) {
                try {
                    mPendingCalls.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            String res = mPendingCalls.get(id);
            Object o = new Gson().fromJson(res, Object.class);
            mPendingCalls.remove(id);
            return o;
        }
    }

    @JavascriptInterface
    public void onResult(int id, String res) {
        synchronized (mPendingCalls) {
            mPendingCalls.put(id, res);
            mPendingCalls.notifyAll();
        }
    }

    protected static String constructFunction(String function, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append(function);
        sb.append("(");
        String separator = "";
        for (Object param : params) {
            sb.append(separator);
            separator = ",";
            if (param instanceof String) sb.append(new Gson().toJson(param));
            else sb.append(param);
        }
        return sb.append(")").toString();
    }

}
