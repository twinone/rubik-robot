package org.twinone.rubiksolver;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.twinone.rubiksolver.model.AlgorithmMove;
import org.twinone.rubiksolver.model.SimpleRobotMapper;
import org.twinone.rubiksolver.model.comm.DelayRequest;
import org.twinone.rubiksolver.model.comm.Request;
import org.twinone.rubiksolver.util.ColorUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private CubeWebView mCubeWebView;

    private RelativeLayout mRootView;
    private CapturedFace[] mCapturedFaces = new CapturedFace[6];
    private TextView[][] mSquare = new TextView[MainActivity.SIZE][MainActivity.SIZE];
    private int mCurrentCapturingFaceId = 0;

    private static final int REQUEST_ID = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        checkPermissions();
        mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_camera, null);

        setupSquares();
        mButtonCapture = (Button) mRootView.findViewById(R.id.button_capture);
        mButtonCapture.setOnClickListener(this);


        // TODO request runtime permissions
        //mFaceCapturer = new FaceCapturer(this);

//        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.frame_layout);
        mCubeWebView = new CubeWebView(this.getActivity());
        mCubeWebView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return mRootView;
    }


    /*
     * @return true if the permissions are granted
     */
    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_ID);
            return false;
        }

        //mFaceCapturer = new FaceCapturer(this);

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        checkPermissions();
    }

    private void setupSquares() {
        // Setup squares at the top right corner
        int s = dpToPx(200) / MainActivity.SIZE;
        LinearLayout cpw = (LinearLayout) getRootView().findViewById(R.id.colorPreviewWrapper);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(s, s);
        int margin = (int) (0.1 * s);
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
        if (mFaceCapturer != null) mFaceCapturer.start();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mFaceCapturer != null)
            mFaceCapturer.stop();
    }

    boolean a = false;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_capture) {
//           mFaceCapturer.capture(mCurrentCapturingFaceId, this);
            if (!a) {
                mRootView.addView(mCubeWebView);
                a = true;
            } else {


            }
        }
    }

    @Override
    public void onFaceCaptured(int id, CapturedFace face) {
        if (mCurrentCapturingFaceId < 6) {
            mCapturedFaces[mCurrentCapturingFaceId++] = face;
            showColors(face);
        }

        if (mCurrentCapturingFaceId == 6) {
            int colors[] = new int[face.size * face.size * 6];

            int p = 0;
            for (int i = 0; i < mCapturedFaces.length; i++) {
                CapturedFace f = mCapturedFaces[i];
                for (int j = 0; j < f.size; j++) {
                    for (int k = 0; k < f.size; k++) {
                        colors[p++] = f.getColor(j, k);
                    }
                }
            }
            char[] state = new char[6 * 9];
            for (int l = 0; l < 6; l++) {
                List<Integer> a = kNN(colors, l * 9 + 4, 6);
                a.add(l * 9 + 4);
                for (int piece : a)
                    state[piece] = "ULFRBD".charAt(l);
            }
            boolean invalidState = false;
            for (char piece : state) if (piece == 0) invalidState = true;
            if (invalidState) {
                Log.e(TAG, "Invalid state, scanning didn't work out well.");
                return;
            }
            Log.d(TAG, "State: https://twinone.github.io/rubik-solver/web/?state=" + String.valueOf(state));
            mRootView.addView(mCubeWebView);
//            mWebCube.optimizedSolve(String.valueOf(state), new ValueCallback<String>() {
//                @Override
//                public void onReceiveValue(String value) {
//                    if (value.equals("null")) {
//                        Log.e(TAG, "Unsolvable state, or scanning failed.");
//                        return;
//                    }
//
//                    String alg = value.substring(1,value.length()-1); //FIXME: crappy
//                    handleAlgorithm(alg);
//                }
//            });
        }
    }

    private static List<Integer> kNN(int[] colors, int index, int k) {
        class Pair {
            int index;
            double dst;
        }
        List<Pair> dsts = new ArrayList<>();
        for (int j = 0; j < colors.length; j++) {
            if (index == j) continue;
            Pair p = new Pair();
            p.dst = colorDistance(colors[j], colors[index]);
            p.index = j;
            dsts.add(p);
        }

        Collections.sort(dsts, new Comparator<Pair>() {
            @Override
            public int compare(Pair lhs, Pair rhs) {
                return (int) Math.signum(lhs.dst - rhs.dst);
            }
        });
        //double fmd = dsts.get(7).dst;
        //double next = dsts.get(8).dst;
        //Log.d(TAG, "group max: "+fmd +", next: "+next + ", diff="+(next-fmd));

        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            idx.add(dsts.get(i).index);
        Collections.sort(idx);
        return idx;
    }

    private void showColors(CapturedFace f) {
        for (int i = 0; i < f.size; i++) {
            double[] p1 = f.m[0][i].clone();
            applyGamma(p1);
            for (int j = 0; j < f.size; j++) {
                mSquare[i][j].setBackgroundColor(f.getColor(i, j));

                // Calculate distance between this and target point
                double[] p2 = f.m[i][j].clone();
                applyGamma(p2);

                double[] ds = {p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
                double error = Math.sqrt((ds[0] * ds[0] + ds[1] * ds[1] + ds[2] * ds[2]) / 3);

                int color = f.getColor(i, j);
                float hsv[] = f.getHSV(i, j);

                //mSquare[i][j].setText(String.format("%.4f%%", error * 100));
                mSquare[i][j].setText("#" + colorToHex(color) + "\n" + "HSV:" + "\n" + hsv[0] + "\n" + hsv[1] + "\n" + hsv[2]);
                mSquare[i][j].setTextColor(getContrastyColor(color));
//                Log.d(TAG, "Sticker " + i + " " + j);
//                Log.d(TAG, "RGB: " + Color.red(color) + " " + Color.green(color) + " " + Color.blue(color));
//                Log.d(TAG, "HSV: " + hsv[0] + " " + hsv[1] + " " + hsv[2]);

            }
        }
    }

    private int getContrastyColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        if (r + g + b > 3 * 255 / 2) return 0xFF000000;
        else if (true) return 0xFFFFFFFF;

        float hsv[] = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] = (hsv[0] + 180) % 360;
        hsv[1] = 1.0f;
        hsv[2] = 1.0f;
        return Color.HSVToColor(hsv);
    }

    public static String colorToHex(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        return String.format("%02X%02X%02X", r, g, b);
    }

    public static void applyGamma(double[] d) {
        for (int i = 0; i < d.length; i++) d[i] = Math.pow(d[i] / 255.0, COLOR_ERROR_GAMMA);
    }

    /**
     * Returns the distance between two colors
     */
    public static double colorDistance(int a, int b) {
        // see http://stackoverflow.com/a/26998429
        double[] lab1 = ColorUtil.ColorToLAB(a);
        double[] lab2 = ColorUtil.ColorToLAB(b);
        return Math.sqrt(Math.pow(lab2[0] - lab1[0], 2) + Math.pow(lab2[1] - lab1[1], 2) + Math.pow(lab2[2] - lab1[2], 2));
    }

    public void handleAlgorithm(String alg) {
        List<AlgorithmMove> moves = AlgorithmMove.parse(alg);
        Log.d(TAG, "Solve (" + moves.size() + "): " + alg);

        List<AlgorithmMove> preMappedMoves = new ArrayList<>();
        for (AlgorithmMove move : moves)
            Collections.addAll(preMappedMoves, SimpleRobotMapper.preMap(move));
        Log.d(TAG, "Pre-mapped moves (" + preMappedMoves.size() + "): " + AlgorithmMove.format(preMappedMoves));

        SimpleRobotMapper mapper = new SimpleRobotMapper();
        List<Request> requests = mapper.map(moves);
        int time = 0, delays = 0;
        for (Request request : requests) {
            if (request instanceof DelayRequest) {
                time += ((DelayRequest) request).getDelay();
                delays++;
            }
        }
        time /= 1000;
        Log.d(TAG, "Translated into " + requests.size() + " backend requests.\nTheoretical execution time is " + (time / 60) + ":" + (time % 60) + " across " + delays + " delays.");
    }

}
