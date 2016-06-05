package org.twinone.rubiksolver.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;

import org.twinone.rubiksolver.util.JSWebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * View that uses rubik-web to display a cube. Not thread-safe,
 * all methods should be called from the UI thread.
 */
public class CubeWebView extends JSWebView {
    public static final String SOLVED = "UUUUUUUUULLLLLLLLLFFFFFFFFFRRRRRRRRRBBBBBBBBBDDDDDDDDD";

    protected Handler mHandler = new Handler();

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

        if (state == null) state = SOLVED;
        loadUrl("file:///android_asset/index.html?state=" + state);
        this.mState = state;
        getSettings().setAllowFileAccessFromFileURLs(true);
    }

    // Move end listeners

    public interface MoveEndListener {
        void onMoveEnd(String move);
    }

    ;

    protected Set<MoveEndListener> mMoveEndListeners = new HashSet<>();

    public void addMoveEndListener(MoveEndListener listener) {
        mMoveEndListeners.add(listener);
    }

    public void removeMoveEndListener(MoveEndListener listener) {
        mMoveEndListeners.remove(listener);
    }

    @JavascriptInterface
    public void moveEnd(final String move, final String state) {
        // FIXME: is this necessary? isn't this run in the UI thread already?
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mState = state;
                for (MoveEndListener listener : mMoveEndListeners)
                    listener.onMoveEnd(move);
            }
        });
    }

    // Ready event

    protected Set<Runnable> mReadyCallbacks = new HashSet<>();
    protected boolean mReady = false;

    public boolean ismReady() {
        return mReady;
    }

    public void callWhenReady(Runnable callback) {
        if (mReady) callback.run();
        else mReadyCallbacks.add(callback);
    }

    @JavascriptInterface
    public void ready() {
        // FIXME: is this necessary? isn't this run in the UI thread already?
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mReady = true;
                for (Runnable callback : mReadyCallbacks)
                    callback.run();
                mReadyCallbacks = null;
            }
        });
    }

    // State set / get

    /**
     * Sets the cube state. The change is scheduled, not applied immediately.
     *
     * @param state New state
     */
    public void setState(String state) {
        loadUrl("javascript:" + JSWebView.constructFunction("cube.setState", state));
    }

    String mState;

    public String getState() {
        return mState;
    }

    // Cube.js solver

    Map<String, List<SolveCallback>> mSolveCallbacks = new HashMap<>();

    public interface SolveCallback {
        void onSolved(String algorithm);
    }

    public void cubejsSolve(String state, SolveCallback callback) {
        if (!mSolveCallbacks.containsKey(state)) {
            mSolveCallbacks.put(state, new ArrayList<SolveCallback>());
            loadUrl("javascript:" + constructFunction("cube.cubejsSolve", state));
        }
        mSolveCallbacks.get(state).add(callback);
    }

    @JavascriptInterface
    public void solved(final String state, final String algorithm) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SolveCallback callback : mSolveCallbacks.remove(state))
                    callback.onSolved(algorithm);
            }
        });
    }

    // UI

    public void lookAtFace(String face) {
        loadUrl("javascript:" + constructFunction("cube.lookAtFace", face));
    }

    public void setAnimationDuration(int duration) {
        loadUrl("javascript:cube.setAnimationDuration(" + duration + ")");
    }

    public void executeAlgorithm(String algorithm) {
        loadUrl("javascript:cube.algorithm(\"" + algorithm + "\")");
    }

    public void setColors(List<Integer> colors) {
        StringBuilder sb = new StringBuilder("[");
        boolean sep = false;
        for (int c : colors) {
            if (sep) sb.append(",");
            sb.append(c);
            sep = true;
        }
        sb.append("]");

        loadUrl("javascript:cube.setStickerColors(" + sb.toString() + ")");
    }

}
