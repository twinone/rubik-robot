package org.twinone.rubiksolver;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by twinone on 6/20/15.
 */
@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment implements View.OnClickListener, FaceCapturer.Callback {

    private static final String TAG = "CameraFragment";
    // The absolute difference between two colors such that we consider them to be different colors.
    private static final double COLOR_ERROR_THRESHOLD = 8.0F;

    private static final double COLOR_ERROR_GAMMA = 2.2;

    // We take square images
    private static final int IMAGE_SIZE = 100;

    private FaceCapturer mFaceCapturer;
    private Button mButtonCapture;

    private RelativeLayout mRootView;
    private CapturedFace[] mCapturedFaces = new CapturedFace[6];
    private TextView[][] mSquare = new TextView[MainActivity.SIZE][MainActivity.SIZE];


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_camera, null);

        setupSquares();
        mButtonCapture = (Button) mRootView.findViewById(R.id.button_capture);
        mButtonCapture.setOnClickListener(this);
        mFaceCapturer = new FaceCapturer(this);
//        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.frame_layout);
        return mRootView;
    }

    private void setupSquares() {
        // Setup squares at the top right corner
        int s = dpToPx(200) / MainActivity.SIZE;
        LinearLayout cpw = (LinearLayout) getRootView().findViewById(R.id.colorPreviewWrapper);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(s, s);
        int margin = (int) 0.1 * s;
        lp.setMargins(margin, margin, margin, margin);
        for (int i = 0; i < MainActivity.SIZE; i++) {
            LinearLayout l = new LinearLayout(getActivity());
            l.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < MainActivity.SIZE; j++) {
                TextView tv = new TextView(getActivity());
                tv.setTextSize(10);
                tv.setLayoutParams(lp);
                l.addView(tv);
                mSquare[i][j] = tv;
            }
            cpw.addView(l);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public ViewGroup getRootView() {
        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mFaceCapturer.start();
    }


    @Override
    public void onStop() {
        super.onStop();
        mFaceCapturer.stop();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_capture) {
            mFaceCapturer.capture(0, this);
        }
    }

    @Override
    public void onFaceCaptured(int id, CapturedFace f) {
        showColors(f);
    }

    private void showColors(CapturedFace f) {
        for (int j = 0; j < f.size; j++) {
            double[] p1 = f.m[0][j].clone();
            applyGamma(p1);
            for (int i = 0; i < f.size; i++) {
                mSquare[i][j].setBackgroundColor(f.getColor(i, j));

                // Calculate distance between this and target point
                double[] p2 = f.m[i][j].clone();
                applyGamma(p2);

                double[] ds = {p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
                double error = Math.sqrt((ds[0] * ds[0] + ds[1] * ds[1] + ds[2] * ds[2]) / 3);

                mSquare[i][j].setText(String.format("%.4f%%", error * 100));

            }
        }
    }

    void applyGamma(double[] d) {
        for (int i = 0; i < d.length; i++) d[i] = Math.pow(d[i] / 255.0, COLOR_ERROR_GAMMA);
    }
}
