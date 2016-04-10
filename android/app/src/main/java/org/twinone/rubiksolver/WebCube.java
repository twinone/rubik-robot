package org.twinone.rubiksolver;

import android.content.Context;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by xavier on 06/04/16.
 */
public class WebCube extends WebView {

    protected String cubeState;
    private final InjectedObject jsInterface = new InjectedObject();

    public WebCube(Context context, String state) {
        super(context);
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(jsInterface, "native");
            String url = "file:///android_asset/web/viewer.html";
        if (state != null) url += "?state="+state;
        loadUrl(url);
    }

    public WebCube(Context context) {
        this(context, null);
    }

    // FIXME: do things better than concatenating string on JS

    public void setCubeState(String state, ValueCallback<String> result) {
        evaluateJavascript("setState(\"" +state+"\")", result);
    }

    public void getCubeState(ValueCallback<String> result) {
        evaluateJavascript("getState()", result);
    }

    public void scramble(ValueCallback<String> result) {
        evaluateJavascript("scramble()", result);
    }

    public void solve(ValueCallback<String> result) {
        evaluateJavascript("solve()", result);
    }

    public void solve(String state, ValueCallback<String> result) {
        evaluateJavascript("solve(\""+state+"\")", result);
    }

    public void optimize(String alg, ValueCallback<String> result) {
        evaluateJavascript("optimize(\""+alg+"\")", result);
    }

    public void optimizedSolve(ValueCallback<String> result) {
        evaluateJavascript("optimizedSolve()", result);
    }

    public void optimizedSolve(String state, ValueCallback<String> result) {
        evaluateJavascript("optimizedSolve(\""+state+"\")", result);
    }

    public void invertAlgorithm(String alg, ValueCallback<String> result) {
        evaluateJavascript("invertAlgorithm(\""+alg+"\")", result);
    }

    public void doAlgorithm(String alg, ValueCallback<String> result) {
        evaluateJavascript("doAlgorithm(\""+alg+"\")", result);
    }

    public void toggleLabels(ValueCallback<String> result) {
        evaluateJavascript("toggleLabels()", result);
    }

    public void resetCamera(ValueCallback<String> result) {
        evaluateJavascript("resetCamera()", result);
    }

    // TODO: verify state

    class InjectedObject {
        @JavascriptInterface
        void pushState(String state) {
            cubeState = state;
        }
    }

    interface StringCallback {
    }

}
