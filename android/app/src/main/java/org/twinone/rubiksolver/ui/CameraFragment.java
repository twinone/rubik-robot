package org.twinone.rubiksolver.ui;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import org.twinone.rubiksolver.R;
import org.twinone.rubiksolver.util.FaceCapturer;
import org.twinone.rubiksolver.util.StickerSorter;
import org.twinone.rubiksolver.model.CapturedFace;
import org.twinone.rubiksolver.model.Sticker;
import org.twinone.rubiksolver.robot.AlgorithmMove;
import org.twinone.rubiksolver.robot.RobotScheduler;
import org.twinone.rubiksolver.robot.SimpleRobotMapper;
import org.twinone.rubiksolver.robot.SlightlyMoreAdvancedMapper;
import org.twinone.rubiksolver.robot.comm.DetachRequest;
import org.twinone.rubiksolver.robot.comm.Request;
import org.twinone.rubiksolver.robot.comm.Response;
import org.twinone.rubiksolver.util.MatrixUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by twinone on 6/20/15.
 */
@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment implements View.OnClickListener, FaceCapturer.Callback, CubeWebView.SolveCallback {

    //TODO: separate button for gripping and scanning; also ungrip button
    //TODO: implement progress bar for solving and executing
    //TODO: verify state is solvable before attempting to solve
    //FIXME: preload webview
    //TODO: make lookAtFace and algorithm/setAnimationDuration work

    private static final boolean DBG = false;
    private static final String DBG_STATE = CubeWebView.STATE_SOLVED;
    private static final String TAG = "CameraFragment";
    private static final double COLOR_ERROR_GAMMA = 2.2;
    private static final int REQUEST_ID = 1;

    // Views
    private RelativeLayout mRootView;
    private TextView[][] mCapturedSquares = new TextView[MainActivity.SIZE][MainActivity.SIZE];
    private FaceCapturer mFaceCapturer;
    private Button mButtonGrip;
    private Button mButtonSolve;
    private Button mButtonFlash;
    private CubeWebView mCube;


    // Data
    private int mGrippedAxis = 0;
    private boolean mFlashEnabled = false;
    private int mCurrentCapturingFaceId = 0;

    private Handler mHandler;

    private String mState;
    private List<Integer> mFaceColors = new ArrayList<>();
    private CapturedFace[] mCapturedFaces = new CapturedFace[6];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mHandler = new Handler();
        mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_camera, null);

        mButtonGrip = (Button) mRootView.findViewById(R.id.button_grip);
        mButtonGrip.setOnClickListener(this);

        mButtonFlash = (Button) mRootView.findViewById(R.id.button_flash);
        mButtonFlash.setOnClickListener(this);

        setupSquares();

        mButtonSolve = (Button) mRootView.findViewById(R.id.button_solve);
        mButtonSolve.setOnClickListener(this);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                checkPermissions();
            }
        });

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

        onPermissionsGranted();

        return true;
    }

    private void onPermissionsGranted() {

        if (DBG) {
            mState = DBG_STATE;
            initCubeWebView();
            return;
        }

        mFaceCapturer = new FaceCapturer(this);
        mFaceCapturer.start();

        SimpleRobotMapper mapper = getMapper();
        if (mapper == null) return;
        List<Request> requests = new ArrayList<>();

        Collections.addAll(requests, mapper.rotateSide(0, 0, 0));
        Collections.addAll(requests, mapper.rotateSide(1, 0, 0));
        Collections.addAll(requests, mapper.rotateSide(2, 0, 0));
        Collections.addAll(requests, mapper.rotateSide(3, 0, 0));
        Collections.addAll(requests, mapper.gripSide(0, false, 0));
        Collections.addAll(requests, mapper.gripSide(1, false, 0));
        Collections.addAll(requests, mapper.gripSide(2, false, 0));
        Collections.addAll(requests, mapper.gripSide(3, false, 0));

        try {
            ((MainActivity) getActivity()).getRobotScheduler().put(requests, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                mCapturedSquares[i][j] = tv;
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

    private void initCubeWebView() {
        Log.d(TAG, "State: " + mState);
        if (mCube != null) return;

        mCube = new CubeWebView(this.getActivity(), mState);
        mCube.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mCube.callWhenReady(new Runnable() {
            @Override
            public void run() {
                // TODO
                // mCube.lookAtFace("U");
                mCube.setColors(mFaceColors);
                mCube.cubejsSolve(mState, CameraFragment.this);
            }
        });

        mRootView.addView(mCube);
    }

    private SimpleRobotMapper getMapper() {
        SlightlyMoreAdvancedMapper mapper = ((MainActivity) getActivity()).getMapper();
        if (mapper == null) return null;
        return mapper.mapper;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_grip:
                startGrip();
                break;
            case R.id.button_flash:
                mFaceCapturer.setFlash(!mFlashEnabled);
                mFlashEnabled = !mFlashEnabled;
                break;
            case R.id.button_solve:
                break;
        }
    }

    private void startGrip() {
        if (mGrippedAxis >= 2) return;

        SimpleRobotMapper mapper = getMapper();
        List<Request> requests = new ArrayList<>();

        Collections.addAll(requests, mapper.gripSide(mGrippedAxis, true, 0));
        Collections.addAll(requests, mapper.gripSide(mGrippedAxis + 2, true, 0));

        mGrippedAxis++;

        try {
            ((MainActivity) getActivity()).getRobotScheduler().put(requests, new RobotScheduler.ChunkAdapter() {
                @Override
                public void chunkComplete() {
                    if (mGrippedAxis >= 2) mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            startScan();
                        }
                    });
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static final String SCANNED_FACES = "FLDBRU";
    public static final int[] SCANNED_ROTATION = new int[]{0, 0, 3, 1, 1, 0};

    public void startScan() {
        mButtonGrip.setVisibility(View.GONE);

        SimpleRobotMapper mapper = getMapper();
        List<Request> requests = new ArrayList<>();

        // Ungrip Y
        Collections.addAll(requests, mapper.gripAxis(true, false));
        // Move X and Y to H (FIXME: parallellize)
        Collections.addAll(requests, mapper.rotateAxis(true, 1));
        Collections.addAll(requests, mapper.rotateAxis(false, 1));

        executeAndScan(requests);
    }

    public void executeAndScan(List<Request> requests) {
        try {
            ((MainActivity) getActivity()).getRobotScheduler().put(requests, new RobotScheduler.ChunkAdapter() {

                @Override
                public void chunkFailed(int i, Request req, Response res) {
                    Toast.makeText(getActivity(), "Scan failed", Toast.LENGTH_LONG).show();
                }

                @Override
                public void chunkComplete() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCurrentCapturingFaceId < 6)
                                mFaceCapturer.capture(mCurrentCapturingFaceId, CameraFragment.this);
                            else
                                onAllFacesScanned();
                        }
                    });
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFaceCaptured(int id, CapturedFace face) {
        if (mCurrentCapturingFaceId < 6) {
            mCapturedFaces[mCurrentCapturingFaceId++] = face;
            showColors(face);
        }

        SimpleRobotMapper mapper = getMapper();
        List<Request> requests = new ArrayList<>();

        boolean ungrippedAxis = mCurrentCapturingFaceId % 2 != 0;
        if (mCurrentCapturingFaceId < 6) {
            // Move non-gripped axis to V, and grip
            Collections.addAll(requests, mapper.rotateAxis(ungrippedAxis, 0));
            Collections.addAll(requests, mapper.gripAxis(ungrippedAxis, true));
            // Ungrip the other axis
            Collections.addAll(requests, mapper.gripAxis(!ungrippedAxis, false));
            // Move the (now gripped) axis to H
            Collections.addAll(requests, mapper.rotateAxis(ungrippedAxis, 1));
        } else {
            // Move non-gripped axis to V, and grip
            Collections.addAll(requests, mapper.rotateAxis(ungrippedAxis, 0));
            Collections.addAll(requests, mapper.gripAxis(ungrippedAxis, true));
            // Ungrip the other axis, move to V, grip
            Collections.addAll(requests, mapper.gripAxis(!ungrippedAxis, false));
            Collections.addAll(requests, mapper.rotateAxis(!ungrippedAxis, 0));
            Collections.addAll(requests, mapper.gripAxis(!ungrippedAxis, true));
        }

        executeAndScan(requests);
    }

    private void onAllFacesScanned() {
        mFaceCapturer.stop();


        List<Sticker> stickers = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            int currentFace = SCANNED_FACES.indexOf("ULFRBD".charAt(i));
            double[][][] matrix = mCapturedFaces[currentFace].m;
            for (int r = 0; r < SCANNED_ROTATION[currentFace]; r++)
                MatrixUtils.rotate(matrix, 3);
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    double[] c = matrix[y][x];
                    stickers.add(new Sticker(stickers.size(), Color.rgb((int) c[0], (int) c[1], (int) c[2])));
                }
            }
        }

        mState = StickerSorter.getState(stickers, mFaceColors);
        initCubeWebView();
    }

    private void showColors(CapturedFace f) {
        for (int i = 0; i < f.size; i++) {
            double[] p1 = f.m[0][i].clone();
            applyGamma(p1);
            for (int j = 0; j < f.size; j++) {
                mCapturedSquares[i][j].setBackgroundColor(f.getColor(i, j));

                // Calculate distance between this and target point
                double[] p2 = f.m[i][j].clone();
                applyGamma(p2);

                double[] ds = {p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
                double error = Math.sqrt((ds[0] * ds[0] + ds[1] * ds[1] + ds[2] * ds[2]) / 3);

                int color = f.getColor(i, j);
                float hsv[] = f.getHSV(i, j);

                //mCapturedSquares[i][j].setText(String.format("%.4f%%", error * 100));
                mCapturedSquares[i][j].setText("#" + colorToHex(color) + "\n" + "HSV:" + "\n" + hsv[0] + "\n" + hsv[1] + "\n" + hsv[2]);
                mCapturedSquares[i][j].setTextColor(getContrastyColor(color));
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

    @Override
    public void onSolved(String algorithm) {
        List<AlgorithmMove> moves = AlgorithmMove.parse(algorithm);
        Log.d(TAG, "Solve (" + moves.size() + "): " + algorithm);

        final List<SimpleRobotMapper.RequestTag> tags = new ArrayList<>();
        final List<Request> requests = ((MainActivity) getActivity()).getMapper().map(moves, true, tags);
        requests.addAll(DetachRequest.DETACH_ALL);

        boolean result = ((MainActivity) getActivity()).getRobotScheduler().offer(requests, new RobotScheduler.ChunkListener() {
            @Override
            public void requestComplete(final int i, Request req) {
                if (i + 1 >= requests.size()) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Request next = requests.get(i + 1);
                        for (SimpleRobotMapper.RequestTag tag : tags) {
                            if (tag.requests[0] == next) {
                                mCube.setAnimationDuration(tag.time);
                                mCube.executeAlgorithm(AlgorithmMove.format(tag.move));
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public void chunkFailed(int i, Request req, Response res) {
            }

            @Override
            public void chunkComplete() {
                getActivity().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(getActivity(), "Cube solved", Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
        });
    }

}
